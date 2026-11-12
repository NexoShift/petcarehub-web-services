package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.controllers;

import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByPetIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.services.ClinicalConsultationQueryService;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.ClinicalConsultationResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform.ClinicalConsultationResourceFromEntityAssembler;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
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

/**
 * Resources of a pet in Clinical Attention. This controller is shared with the treatments of the
 * pet ({@code GET /pets/{petId}/treatments}).
 */
@RestController
@RequestMapping(value = "/api/v1/pets/{petId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pet Clinical Attention", description = "Clinical records of a pet")
public class PetClinicalConsultationsController {

  private final ClinicalConsultationQueryService queryService;

  public PetClinicalConsultationsController(ClinicalConsultationQueryService queryService) {
    this.queryService = queryService;
  }

  @GetMapping("/clinical-consultations")
  @PreAuthorize("hasAnyRole('VETERINARIAN', 'PET_OWNER')")
  @Operation(summary = "List the consultations of a pet, newest first (veterinarian or owner)")
  public ResponseEntity<List<ClinicalConsultationResource>> getClinicalConsultationsByPetId(
      @PathVariable Long petId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
    var consultations = queryService.handle(
        new GetClinicalConsultationsByPetIdQuery(petId, currentUser.getId()));
    return ResponseEntity.ok(consultations.stream()
        .map(ClinicalConsultationResourceFromEntityAssembler::toResourceFromEntity).toList());
  }
}
