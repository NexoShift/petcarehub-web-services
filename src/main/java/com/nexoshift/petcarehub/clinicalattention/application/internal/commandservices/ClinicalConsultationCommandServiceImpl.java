package com.nexoshift.petcarehub.clinicalattention.application.internal.commandservices;

import com.nexoshift.petcarehub.clinicalattention.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.ClinicalConsultationNotFoundException;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.VeterinarianNotAuthorizedException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.AddClinicalFindingCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CancelClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CompleteClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CreateClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.ReevaluateDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterAnamnesisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPhysiologicalAssessmentCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.StartClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalattention.domain.repositories.ClinicalConsultationRepository;
import com.nexoshift.petcarehub.clinicalattention.domain.services.ClinicalConsultationCommandService;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates the use cases: finds the consultation, checks that the requester is the
 * veterinarian in charge, calls the aggregate and saves. The business rules live in the aggregate.
 */
@Service
public class ClinicalConsultationCommandServiceImpl implements ClinicalConsultationCommandService {

  private final ClinicalConsultationRepository consultationRepository;
  private final ExternalProfilesService externalProfilesService;

  public ClinicalConsultationCommandServiceImpl(
      ClinicalConsultationRepository consultationRepository,
      ExternalProfilesService externalProfilesService) {
    this.consultationRepository = consultationRepository;
    this.externalProfilesService = externalProfilesService;
  }

  @Override
  @Transactional
  public Optional<ClinicalConsultation> handle(CreateClinicalConsultationCommand command) {
    if (!externalProfilesService.existsPet(new PetId(command.petId()))) {
      throw new PetNotFoundException(command.petId());
    }
    var veterinarianId = fetchVeterinarianId(command.veterinarianUserId());
    var clinicId = new ClinicId(command.clinicId());
    if (!externalProfilesService.isVeterinarianActiveInClinic(veterinarianId, clinicId)) {
      throw new VeterinarianNotAuthorizedException(
          "The veterinarian is not active in the clinic of the consultation");
    }
    if (command.consultingRoomId() != null && !externalProfilesService.isConsultingRoomActive(
        clinicId, new ConsultingRoomId(command.consultingRoomId()))) {
      throw new IllegalArgumentException("The consulting room is not active in the clinic");
    }
    var consultation =
        consultationRepository.save(new ClinicalConsultation(command, veterinarianId));
    consultation.notifyCreated();
    return Optional.of(consultationRepository.save(consultation));
  }

  @Override
  @Transactional
  public Optional<ClinicalConsultation> handle(StartClinicalConsultationCommand command) {
    var consultation =
        findAttendedConsultation(command.consultationId(), command.requesterUserId());
    consultation.start();
    return Optional.of(consultationRepository.save(consultation));
  }

  @Override
  @Transactional
  public Optional<ClinicalConsultation> handle(RegisterAnamnesisCommand command) {
    var consultation =
        findAttendedConsultation(command.consultationId(), command.requesterUserId());
    consultation.registerAnamnesis(command);
    return Optional.of(consultationRepository.save(consultation));
  }

  @Override
  @Transactional
  public Optional<ClinicalConsultation> handle(RegisterPhysiologicalAssessmentCommand command) {
    var consultation =
        findAttendedConsultation(command.consultationId(), command.requesterUserId());
    consultation.registerPhysiologicalAssessment(command);
    return Optional.of(consultationRepository.save(consultation));
  }

  @Override
  @Transactional
  public Optional<ClinicalConsultation> handle(AddClinicalFindingCommand command) {
    var consultation =
        findAttendedConsultation(command.consultationId(), command.requesterUserId());
    consultation.addClinicalFinding(command);
    return Optional.of(consultationRepository.save(consultation));
  }

  @Override
  @Transactional
  public Optional<ClinicalConsultation> handle(RegisterDiagnosisCommand command) {
    var consultation =
        findAttendedConsultation(command.consultationId(), command.requesterUserId());
    consultation.registerDiagnosis(command);
    return Optional.of(consultationRepository.save(consultation));
  }

  @Override
  @Transactional
  public Optional<ClinicalConsultation> handle(ReevaluateDiagnosisCommand command) {
    var consultation =
        findAttendedConsultation(command.consultationId(), command.requesterUserId());
    consultation.reevaluateDiagnosis(command);
    return Optional.of(consultationRepository.save(consultation));
  }

  @Override
  @Transactional
  public Optional<ClinicalConsultation> handle(CompleteClinicalConsultationCommand command) {
    var consultation =
        findAttendedConsultation(command.consultationId(), command.requesterUserId());
    consultation.complete();
    return Optional.of(consultationRepository.save(consultation));
  }

  @Override
  @Transactional
  public Optional<ClinicalConsultation> handle(CancelClinicalConsultationCommand command) {
    var consultation =
        findAttendedConsultation(command.consultationId(), command.requesterUserId());
    consultation.cancel(command.reason());
    return Optional.of(consultationRepository.save(consultation));
  }

  private VeterinarianId fetchVeterinarianId(Long userId) {
    return externalProfilesService.fetchVeterinarianIdByUserId(userId)
        .orElseThrow(() -> new VeterinarianNotAuthorizedException(
            "The authenticated user is not a registered veterinarian"));
  }

  private ClinicalConsultation findAttendedConsultation(Long consultationId, Long requesterUserId) {
    var consultation = consultationRepository.findById(consultationId)
        .orElseThrow(() -> new ClinicalConsultationNotFoundException(consultationId));
    if (!consultation.isAttendedBy(fetchVeterinarianId(requesterUserId))) {
      throw new VeterinarianNotAuthorizedException(
          "Only the veterinarian in charge can change this clinical consultation");
    }
    return consultation;
  }
}
