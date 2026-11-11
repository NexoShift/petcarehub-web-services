package com.nexoshift.petcarehub.clinicalexams.interfaces.rest.controllers;

import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.GetClinicalExamsByPetIdQuery;
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
@RequestMapping(value = "/api/v1/pets/{petId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clinical Exams")
public class PetClinicalExamsController {

    private final ClinicalExamQueryService queryService;

    public PetClinicalExamsController(ClinicalExamQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/clinical-exams")
    @PreAuthorize("hasAnyRole('VETERINARIAN', 'PET_OWNER')")
    @Operation(summary = "List the exams of a pet (owner or veterinarian of a clinic where it is a patient)")
    public ResponseEntity<List<ClinicalExamResource>> getClinicalExams(
            @PathVariable Long petId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var exams = queryService.handle(new GetClinicalExamsByPetIdQuery(petId, currentUser.getId()));
        var veterinarian = ClinicalExamResourceFromEntityAssembler.isVeterinarian(currentUser);
        return ResponseEntity.ok(exams.stream()
                .map(exam -> ClinicalExamResourceFromEntityAssembler.toResourceFromEntity(
                        exam, veterinarian))
                .toList());
    }
}
