package com.nexoshift.petcarehub.clinicalexams.interfaces.rest.controllers;

import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.GetClinicalExamsByConsultationIdQuery;
import com.nexoshift.petcarehub.clinicalexams.domain.services.ClinicalExamQueryService;
import com.nexoshift.petcarehub.clinicalexams.interfaces.rest.resources.ClinicalExamResource;
import com.nexoshift.petcarehub.clinicalexams.interfaces.rest.transform.ClinicalExamResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/clinical-consultations/{consultationId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clinical Exams")
public class ConsultationClinicalExamsController {

    private final ClinicalExamQueryService queryService;

    public ConsultationClinicalExamsController(ClinicalExamQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/clinical-exams")
    @PreAuthorize("hasAnyRole('VETERINARIAN', 'PET_OWNER')")
    @Operation(summary = "List the exams of a consultation (veterinarian of the clinic or owner of the pet)")
    public ResponseEntity<List<ClinicalExamResource>> getClinicalExams(
            @PathVariable Long consultationId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var exams = queryService.handle(new GetClinicalExamsByConsultationIdQuery(consultationId, currentUser.getId()));
        var veterinarian = ClinicalExamResourceFromEntityAssembler.isVeterinarian(currentUser);
        return ResponseEntity.ok(exams.stream()
                .map(exam -> ClinicalExamResourceFromEntityAssembler.toResourceFromEntity(
                        exam, veterinarian))
                .toList());
    }
}
