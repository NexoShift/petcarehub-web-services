package com.nexoshift.petcarehub.clinicalexams.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.entities.ClinicalFile;
import com.nexoshift.petcarehub.clinicalexams.domain.model.entities.ExamResult;
import com.nexoshift.petcarehub.clinicalexams.interfaces.rest.resources.ClinicalExamResource;
import com.nexoshift.petcarehub.clinicalexams.interfaces.rest.resources.ClinicalFileResource;
import com.nexoshift.petcarehub.clinicalexams.interfaces.rest.resources.ExamResultResource;
import org.springframework.security.core.userdetails.UserDetails;

public final class ClinicalExamResourceFromEntityAssembler {

    private ClinicalExamResourceFromEntityAssembler() {
    }

    /** Veterinarians see everything; any other reader (the pet owner) sees validated results. */
    public static boolean isVeterinarian(UserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_VETERINARIAN".equals(authority.getAuthority()));
    }

    public static ClinicalExamResource toResourceFromEntity(ClinicalExam exam) {
        return toResourceFromEntity(exam, true);
    }

    /**
     * The pet owner only sees a result once a veterinarian has validated it, so a draft result
     * never reaches them.
     */
    public static ClinicalExamResource toResourceFromEntity(ClinicalExam exam,
                                                            boolean includeUnvalidatedResult) {
        var cancellation = exam.getCancellation();
        var result = exam.getResult()
                .filter(found -> includeUnvalidatedResult || found.isValidated());
        return new ClinicalExamResource(
                exam.getId(), exam.getConsultationId(), exam.getClinicId(), exam.getPetId(), exam.getRequestedBy(),
                exam.getType().name(), exam.getIndication(), exam.getStatus().name(),
                exam.getRequestedAt(), exam.getStartedAt(), exam.getCompletedAt(),
                cancellation.map(c -> c.reason()).orElse(null),
                cancellation.map(c -> c.cancelledAt()).orElse(null),
                result.map(ClinicalExamResourceFromEntityAssembler::toResource).orElse(null),
                exam.getFiles().stream()
                        .map(ClinicalExamResourceFromEntityAssembler::toResourceFromEntity)
                        .toList());
    }

    public static ClinicalFileResource toResourceFromEntity(ClinicalFile file) {
        return new ClinicalFileResource(file.getId(), file.getFileName(), file.getType().name(),
                file.getContentType(), file.getSizeBytes(), file.getUploadedAt());
    }

    private static ExamResultResource toResource(ExamResult result) {
        return new ExamResultResource(result.getId(), result.getSummary(), result.getValues(),
                result.getInterpretation(), result.getResultDate(), result.isValidated(),
                result.getValidatedBy(), result.getValidatedAt());
    }
}
