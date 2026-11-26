package com.nexoshift.petcarehub.clinicalexams.application.internal.access;

import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ClinicalExamAccessDeniedException;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ConsultationNotFoundException;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.VeterinarianNotRegisteredException;
import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.VeterinarianId;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Access rules of Clinical Exams.
 * <ul>
 *   <li>Request: a veterinarian of the clinic where the consultation takes place.</li>
 *   <li>Write (start, result, validate, files, complete, cancel): a veterinarian of the clinic
 *   of the exam. The exam keeps the clinic, so it does not depend on the consultation.</li>
 *   <li>Read: the owner of the pet or a veterinarian of the clinic of the exam. A veterinarian of
 *   ANOTHER clinic where the pet is a patient reads it only while the owner's access grant is
 *   active (permission by hours, in Profiles).</li>
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

    /** The owner, a veterinarian of the clinic of the exam, or another clinic with permission. */
    public void requireCanRead(Long userId, ClinicalExam exam) {
        if (!canReadRecordOf(userId, exam.getPetId(), exam.getClinicId())) {
            throw new ClinicalExamAccessDeniedException();
        }
    }

    /** Exams of a consultation: the owner of the pet or a veterinarian of the clinic. */
    public void requireCanReadConsultation(Long userId, Long consultationId) {
        var petId = clinicalAttention.fetchPetIdByConsultationId(consultationId)
                .orElseThrow(() -> new ConsultationNotFoundException(consultationId));
        requireCanReadConsultation(userId, consultationId, petId);
    }

    /**
     * Exams of a pet: its owner or a veterinarian of a clinic where the pet is a patient.
     *
     * @return which clinics' exams the user may see: all of them for the owner or with the
     *         owner's permission; otherwise only those of the veterinarian's clinic
     */
    public Predicate<Long> requireCanReadPet(Long userId, Long petId) {
        if (isOwner(userId, petId)) {
            return clinicId -> true;
        }
        var clinicId = veterinarianClinic(userId);
        if (clinicId.isEmpty() || !profiles.isPetPatientOfClinic(clinicId.get(), petId)) {
            throw new ClinicalExamAccessDeniedException();
        }
        if (profiles.hasActiveHistoryAccess(clinicId.get(), petId)) {
            return examClinicId -> true;
        }
        return clinicId.get()::equals;
    }

    private void requireCanReadConsultation(Long userId, Long consultationId, Long petId) {
        var consultationClinicId = clinicalAttention.fetchClinicIdByConsultationId(consultationId);
        if (consultationClinicId.isEmpty()
                || !canReadRecordOf(userId, petId, consultationClinicId.get())) {
            throw new ClinicalExamAccessDeniedException();
        }
    }

    /** Owner; veterinarian of the clinic that wrote the record; or another clinic with permission. */
    private boolean canReadRecordOf(Long userId, Long petId, Long recordClinicId) {
        if (isOwner(userId, petId)) {
            return true;
        }
        var clinicId = veterinarianClinic(userId);
        return clinicId.isPresent()
                && (clinicId.get().equals(recordClinicId)
                || profiles.hasActiveHistoryAccess(clinicId.get(), petId));
    }

    private Optional<Long> veterinarianClinic(Long userId) {
        return profiles.fetchVeterinarianIdByUserId(userId)
                .flatMap(profiles::fetchClinicIdByVeterinarianId);
    }

    private boolean isOwner(Long userId, Long petId) {
        return profiles.fetchOwnerUserIdByPetId(petId).map(userId::equals).orElse(false);
    }
}
