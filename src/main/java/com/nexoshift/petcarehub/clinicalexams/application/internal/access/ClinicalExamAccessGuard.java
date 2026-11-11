package com.nexoshift.petcarehub.clinicalexams.application.internal.access;

import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ClinicalExamAccessDeniedException;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ConsultationNotFoundException;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.VeterinarianNotRegisteredException;
import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.VeterinarianId;
import org.springframework.stereotype.Component;

/**
 * Access rules of Clinical Exams.
 * <ul>
 *   <li>Request: a veterinarian of the clinic where the consultation takes place.</li>
 *   <li>Write (start, result, validate, files, complete, cancel): a veterinarian of the clinic
 *   of the exam. The exam keeps the clinic, so it does not depend on the consultation.</li>
 *   <li>Read: a veterinarian of that clinic, or the owner of the pet.</li>
 * </ul>
 */
@Component
public class ClinicalExamAccessGuard {

    private final ExternalClinicalAttentionService clinicalAttention;
    private final ExternalProfilesService profiles;

    public ClinicalExamAccessGuard(ExternalClinicalAttentionService clinicalAttention,
                                   ExternalProfilesService profiles) {
        this.clinicalAttention = clinicalAttention;
        this.profiles = profiles;
    }

    /** Veterinarian profile of the user; 403 if the user is not a veterinarian. */
    public VeterinarianId requireVeterinarian(Long userId) {
        return profiles.fetchVeterinarianIdByUserId(userId)
                .map(VeterinarianId::new)
                .orElseThrow(VeterinarianNotRegisteredException::new);
    }

    /** The user must be a veterinarian of the clinic of the consultation. */
    public VeterinarianId requireVeterinarianOfConsultation(Long userId, Long consultationId) {
        var veterinarianId = requireVeterinarian(userId);
        var consultationClinicId = clinicalAttention.fetchClinicIdByConsultationId(consultationId)
                .orElseThrow(() -> new ConsultationNotFoundException(consultationId));
        var veterinarianClinicId = profiles.fetchClinicIdByVeterinarianId(
                veterinarianId.veterinarianId());
        if (veterinarianClinicId.isEmpty() || !veterinarianClinicId.get().equals(consultationClinicId)) {
            throw new ClinicalExamAccessDeniedException();
        }
        return veterinarianId;
    }

    /** Clinic of the authenticated veterinarian; 403 if the user is not a veterinarian of one. */
    public Long requireVeterinarianClinic(Long userId) {
        var veterinarianId = requireVeterinarian(userId);
        return profiles.fetchClinicIdByVeterinarianId(veterinarianId.veterinarianId())
                .orElseThrow(ClinicalExamAccessDeniedException::new);
    }

    /** The exam belongs to a clinic: only its veterinarians change it. */
    public VeterinarianId requireCanWrite(Long userId, ClinicalExam exam) {
        var veterinarianId = requireVeterinarian(userId);
        var clinicId = profiles.fetchClinicIdByVeterinarianId(veterinarianId.veterinarianId());
        if (clinicId.isEmpty() || !clinicId.get().equals(exam.getClinicId())) {
            throw new ClinicalExamAccessDeniedException();
        }
        return veterinarianId;
    }

    /** The owner of the pet or a veterinarian of the clinic of the exam. */
    public void requireCanRead(Long userId, ClinicalExam exam) {
        if (isOwner(userId, exam.getPetId())) {
            return;
        }
        var clinicId = profiles.fetchVeterinarianIdByUserId(userId)
                .flatMap(profiles::fetchClinicIdByVeterinarianId);
        if (clinicId.isEmpty() || !clinicId.get().equals(exam.getClinicId())) {
            throw new ClinicalExamAccessDeniedException();
        }
    }

    /** Exams of a consultation: the owner of the pet or a veterinarian of the clinic. */
    public void requireCanReadConsultation(Long userId, Long consultationId) {
        var petId = clinicalAttention.fetchPetIdByConsultationId(consultationId)
                .orElseThrow(() -> new ConsultationNotFoundException(consultationId));
        requireCanReadConsultation(userId, consultationId, petId);
    }

    /** Exams of a pet: its owner or a veterinarian of a clinic where the pet is a patient. */
    public void requireCanReadPet(Long userId, Long petId) {
        if (isOwner(userId, petId)) {
            return;
        }
        var clinicId = profiles.fetchVeterinarianIdByUserId(userId)
                .flatMap(profiles::fetchClinicIdByVeterinarianId);
        if (clinicId.isEmpty() || !profiles.isPetPatientOfClinic(clinicId.get(), petId)) {
            throw new ClinicalExamAccessDeniedException();
        }
    }

    private void requireCanReadConsultation(Long userId, Long consultationId, Long petId) {
        if (isOwner(userId, petId)) {
            return;
        }
        var consultationClinicId = clinicalAttention.fetchClinicIdByConsultationId(consultationId);
        var veterinarianClinicId = profiles.fetchVeterinarianIdByUserId(userId)
                .flatMap(profiles::fetchClinicIdByVeterinarianId);
        if (consultationClinicId.isEmpty() || !consultationClinicId.equals(veterinarianClinicId)) {
            throw new ClinicalExamAccessDeniedException();
        }
    }

    private boolean isOwner(Long userId, Long petId) {
        return profiles.fetchOwnerUserIdByPetId(petId).map(userId::equals).orElse(false);
    }
}
