package com.nexoshift.petcarehub.clinicalexams.application.internal.commandservices;

import com.nexoshift.petcarehub.clinicalexams.application.internal.access.ClinicalExamAccessGuard;
import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ClinicalExamNotFoundException;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ConsultationNotAvailableException;
import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.CancelClinicalExamCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.CompleteClinicalExamCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.RegisterExamResultCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.RequestClinicalExamCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.StartClinicalExamCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.UploadClinicalFileCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.ValidateExamResultCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.entities.ClinicalFile;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalexams.domain.repositories.ClinicalExamRepository;
import com.nexoshift.petcarehub.clinicalexams.domain.services.ClinicalExamCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases of Clinical Exams. Every command checks who is asking with
 * {@link ClinicalExamAccessGuard}; the state rules live in the {@link ClinicalExam} aggregate.
 */
@Service
public class ClinicalExamCommandServiceImpl implements ClinicalExamCommandService {

    private final ClinicalExamRepository clinicalExamRepository;
    private final ExternalClinicalAttentionService clinicalAttention;
    private final ClinicalExamAccessGuard accessGuard;
    private final FileStorageService fileStorageService;

    public ClinicalExamCommandServiceImpl(ClinicalExamRepository clinicalExamRepository,
                                          ExternalClinicalAttentionService clinicalAttention,
                                          ClinicalExamAccessGuard accessGuard,
                                          FileStorageService fileStorageService) {
        this.clinicalExamRepository = clinicalExamRepository;
        this.clinicalAttention = clinicalAttention;
        this.accessGuard = accessGuard;
        this.fileStorageService = fileStorageService;
    }

    /** An exam can only be requested in a consultation IN_PROGRESS of the vet's own clinic. */
    @Override
    @Transactional
    public ClinicalExam handle(RequestClinicalExamCommand command) {
        var consultationId = command.consultationId();
        var veterinarianId = accessGuard.requireVeterinarianOfConsultation(
                command.requesterUserId(), consultationId);
        if (!clinicalAttention.isConsultationInProgress(consultationId)) {
            throw new ConsultationNotAvailableException(consultationId);
        }
        var petId = clinicalAttention.fetchPetIdByConsultationId(consultationId)
                .orElseThrow(() -> new ConsultationNotAvailableException(consultationId));
        var clinicId = clinicalAttention.fetchClinicIdByConsultationId(consultationId)
                .orElseThrow(() -> new ConsultationNotAvailableException(consultationId));

        var exam = clinicalExamRepository.save(new ClinicalExam(
                new ClinicalConsultationId(consultationId), new ClinicId(clinicId), new PetId(petId),
                veterinarianId, command.type(), command.indication()));
        exam.notifyRequested();
        return clinicalExamRepository.save(exam);
    }

    @Override
    @Transactional
    public ClinicalExam handle(StartClinicalExamCommand command) {
        var exam = findForWrite(command.examId(), command.requesterUserId());
        exam.start();
        return clinicalExamRepository.save(exam);
    }

    @Override
    @Transactional
    public ClinicalExam handle(RegisterExamResultCommand command) {
        var exam = findForWrite(command.examId(), command.requesterUserId());
        exam.registerResult(command.summary(), command.values(), command.interpretation(),
                command.resultDate());
        return clinicalExamRepository.save(exam);
    }

    /** The validator is the veterinarian who validates, not necessarily who requested it. */
    @Override
    @Transactional
    public ClinicalExam handle(ValidateExamResultCommand command) {
        var exam = findExam(command.examId());
        var validator = accessGuard.requireCanWrite(command.validatorUserId(), exam);
        exam.validateResult(validator);
        return clinicalExamRepository.save(exam);
    }

    /**
     * The state is checked before uploading, so no orphan file is left in the storage when the
     * exam does not accept files.
     */
    @Override
    @Transactional
    public ClinicalFile handle(UploadClinicalFileCommand command) {
        var exam = findForWrite(command.examId(), command.requesterUserId());
        exam.ensureCanAttachFile(command.contentType());
        var location = fileStorageService.upload(
                command.content(), command.fileName(), command.contentType());
        exam.attachFile(command.fileName(), command.contentType(), command.content().length,
                location);
        var saved = clinicalExamRepository.save(exam);
        return saved.findFileByStorageLocation(location)
                .orElseThrow(() -> new IllegalStateException("The clinical file was not saved"));
    }

    @Override
    @Transactional
    public ClinicalExam handle(CompleteClinicalExamCommand command) {
        var exam = findForWrite(command.examId(), command.requesterUserId());
        exam.complete();
        return clinicalExamRepository.save(exam);
    }

    @Override
    @Transactional
    public ClinicalExam handle(CancelClinicalExamCommand command) {
        var exam = findForWrite(command.examId(), command.requesterUserId());
        exam.cancel(command.reason());
        return clinicalExamRepository.save(exam);
    }

    private ClinicalExam findForWrite(Long examId, Long userId) {
        var exam = findExam(examId);
        accessGuard.requireCanWrite(userId, exam);
        return exam;
    }

    private ClinicalExam findExam(Long examId) {
        return clinicalExamRepository.findById(examId)
                .orElseThrow(() -> new ClinicalExamNotFoundException(examId));
    }
}
