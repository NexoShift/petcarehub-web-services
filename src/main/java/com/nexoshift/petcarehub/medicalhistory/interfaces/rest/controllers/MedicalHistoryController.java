package com.nexoshift.petcarehub.medicalhistory.interfaces.rest.controllers;

import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordAllergyCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordVaccinationCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.queries.GetMedicalHistoryByPetIdQuery;
import com.nexoshift.petcarehub.medicalhistory.domain.services.MedicalHistoryCommandService;
import com.nexoshift.petcarehub.medicalhistory.domain.services.MedicalHistoryQueryService;
import com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources.MedicalHistoryResource;
import com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources.RecordAllergyResource;
import com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources.RecordVaccinationResource;
import com.nexoshift.petcarehub.medicalhistory.interfaces.rest.transform.MedicalHistoryResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/pets/{petId}/medical-history",
    produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('PET_OWNER', 'VETERINARIAN')")
@Tag(name = "Medical History",
    description = "Longitudinal record of a pet: allergies, vaccinations and timeline")
public class MedicalHistoryController {

  private final MedicalHistoryCommandService commandService;
  private final MedicalHistoryQueryService queryService;

  public MedicalHistoryController(MedicalHistoryCommandService commandService,
                                  MedicalHistoryQueryService queryService) {
    this.commandService = commandService;
    this.queryService = queryService;
  }

  @GetMapping
  @Operation(summary = "Get the medical history of a pet (owner or treating veterinarian)")
  public ResponseEntity<MedicalHistoryResource> getMedicalHistory(
      @PathVariable Long petId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
    var history = queryService.handle(
        new GetMedicalHistoryByPetIdQuery(petId, currentUser.getId()));
    return ResponseEntity.ok(MedicalHistoryResourceFromEntityAssembler.toResourceFromEntity(history));
  }

  @PostMapping(value = "/allergies", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Record an allergy (owner or treating veterinarian)")
  public ResponseEntity<MedicalHistoryResource> recordAllergy(
      @PathVariable Long petId, @Valid @RequestBody RecordAllergyResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    commandService.handle(new RecordAllergyCommand(petId, currentUser.getId(),
        resource.allergen(), resource.reaction(), resource.severity(), resource.notes()));
    // The answer shows only what this user may see (the same as GET).
    var history = queryService.handle(new GetMedicalHistoryByPetIdQuery(petId, currentUser.getId()));
    return new ResponseEntity<>(
        MedicalHistoryResourceFromEntityAssembler.toResourceFromEntity(history), HttpStatus.CREATED);
  }

  @PostMapping(value = "/vaccinations", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('VETERINARIAN')")
  @Operation(summary = "Record a vaccination (treating veterinarian only)")
  public ResponseEntity<MedicalHistoryResource> recordVaccination(
      @PathVariable Long petId, @Valid @RequestBody RecordVaccinationResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    commandService.handle(new RecordVaccinationCommand(petId, currentUser.getId(),
        resource.vaccineName(), resource.doseNumber(), resource.appliedOn(),
        resource.nextDoseOn(), resource.batchNumber(), resource.notes()));
    var history = queryService.handle(new GetMedicalHistoryByPetIdQuery(petId, currentUser.getId()));
    return new ResponseEntity<>(
        MedicalHistoryResourceFromEntityAssembler.toResourceFromEntity(history), HttpStatus.CREATED);
  }
}
