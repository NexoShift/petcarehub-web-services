package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.controllers;

import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.ClinicalConsultationNotFoundException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CancelClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CompleteClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.StartClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.services.ClinicalConsultationCommandService;
import com.nexoshift.petcarehub.clinicalattention.domain.services.ClinicalConsultationQueryService;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.AddClinicalFindingResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.CancelClinicalConsultationResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.ClinicalConsultationResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.CreateClinicalConsultationResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.ReevaluateDiagnosisResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterAnamnesisResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterDiagnosisResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterPhysiologicalAssessmentResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform.AddClinicalFindingCommandFromResourceAssembler;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform.ClinicalConsultationResourceFromEntityAssembler;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform.CreateClinicalConsultationCommandFromResourceAssembler;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform.ReevaluateDiagnosisCommandFromResourceAssembler;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform.RegisterAnamnesisCommandFromResourceAssembler;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform.RegisterDiagnosisCommandFromResourceAssembler;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform.RegisterPhysiologicalAssessmentCommandFromResourceAssembler;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Clinical consultations. Every endpoint requires ROLE_VETERINARIAN. */
@RestController
@RequestMapping(value = "/api/v1/clinical-consultations",
    produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('VETERINARIAN')")
@Tag(name = "Clinical Consultations",
    description = "Consultation, anamnesis, physiological assessments, findings and diagnoses")
public class ClinicalConsultationsController {

  private final ClinicalConsultationCommandService commandService;
  private final ClinicalConsultationQueryService queryService;

  public ClinicalConsultationsController(ClinicalConsultationCommandService commandService,
      ClinicalConsultationQueryService queryService) {
    this.commandService = commandService;
    this.queryService = queryService;
  }

  @PostMapping
  @Operation(summary = "Create a clinical consultation for a pet (status CREATED)")
  public ResponseEntity<ClinicalConsultationResource> createClinicalConsultation(
      @Valid @RequestBody CreateClinicalConsultationResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    var command = CreateClinicalConsultationCommandFromResourceAssembler
        .toCommandFromResource(currentUser.getId(), resource);
    return commandService.handle(command)
        .map(consultation -> ResponseEntity
            .created(URI.create("/api/v1/clinical-consultations/" + consultation.getId()))
            .body(toResource(consultation)))
        .orElseGet(() -> ResponseEntity.badRequest().build());
  }

  @GetMapping("/{consultationId}")
  @Operation(summary = "Get a clinical consultation with all its records")
  public ResponseEntity<ClinicalConsultationResource> getClinicalConsultationById(
      @PathVariable Long consultationId) {
    var consultation = queryService.handle(new GetClinicalConsultationByIdQuery(consultationId))
        .orElseThrow(() -> new ClinicalConsultationNotFoundException(consultationId));
    return ResponseEntity.ok(toResource(consultation));
  }

  @PutMapping("/{consultationId}/start")
  @Operation(summary = "Start the consultation (CREATED to IN_PROGRESS)")
  public ResponseEntity<ClinicalConsultationResource> startClinicalConsultation(
      @PathVariable Long consultationId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return respond(HttpStatus.OK, commandService.handle(
        new StartClinicalConsultationCommand(consultationId, currentUser.getId())));
  }

  @PostMapping("/{consultationId}/anamnesis")
  @Operation(summary = "Register the anamnesis (once per consultation)")
  public ResponseEntity<ClinicalConsultationResource> registerAnamnesis(
      @PathVariable Long consultationId, @Valid @RequestBody RegisterAnamnesisResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return respond(HttpStatus.CREATED, commandService.handle(
        RegisterAnamnesisCommandFromResourceAssembler.toCommandFromResource(
            consultationId, currentUser.getId(), resource)));
  }

  @PostMapping("/{consultationId}/physiological-assessments")
  @Operation(summary = "Register a physiological assessment (each request adds a new one)")
  public ResponseEntity<ClinicalConsultationResource> registerPhysiologicalAssessment(
      @PathVariable Long consultationId,
      @Valid @RequestBody RegisterPhysiologicalAssessmentResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return respond(HttpStatus.CREATED, commandService.handle(
        RegisterPhysiologicalAssessmentCommandFromResourceAssembler.toCommandFromResource(
            consultationId, currentUser.getId(), resource)));
  }

  @PostMapping("/{consultationId}/findings")
  @Operation(summary = "Add a clinical finding by body system")
  public ResponseEntity<ClinicalConsultationResource> addClinicalFinding(
      @PathVariable Long consultationId, @Valid @RequestBody AddClinicalFindingResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return respond(HttpStatus.CREATED, commandService.handle(
        AddClinicalFindingCommandFromResourceAssembler.toCommandFromResource(
            consultationId, currentUser.getId(), resource)));
  }

  @PostMapping("/{consultationId}/diagnoses")
  @Operation(summary = "Register a diagnosis (at most one current primary diagnosis)")
  public ResponseEntity<ClinicalConsultationResource> registerDiagnosis(
      @PathVariable Long consultationId, @Valid @RequestBody RegisterDiagnosisResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return respond(HttpStatus.CREATED, commandService.handle(
        RegisterDiagnosisCommandFromResourceAssembler.toCommandFromResource(
            consultationId, currentUser.getId(), resource)));
  }

  @PostMapping("/{consultationId}/diagnoses/{diagnosisId}/reevaluations")
  @Operation(summary = "Reevaluate a diagnosis: creates a new one and keeps the previous one")
  public ResponseEntity<ClinicalConsultationResource> reevaluateDiagnosis(
      @PathVariable Long consultationId, @PathVariable Long diagnosisId,
      @Valid @RequestBody ReevaluateDiagnosisResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return respond(HttpStatus.CREATED, commandService.handle(
        ReevaluateDiagnosisCommandFromResourceAssembler.toCommandFromResource(
            consultationId, diagnosisId, currentUser.getId(), resource)));
  }

  @PutMapping("/{consultationId}/complete")
  @Operation(summary = "Complete the consultation (needs anamnesis, assessment and diagnosis)")
  public ResponseEntity<ClinicalConsultationResource> completeClinicalConsultation(
      @PathVariable Long consultationId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return respond(HttpStatus.OK, commandService.handle(
        new CompleteClinicalConsultationCommand(consultationId, currentUser.getId())));
  }

  @PutMapping("/{consultationId}/cancel")
  @Operation(summary = "Cancel the consultation with a reason")
  public ResponseEntity<ClinicalConsultationResource> cancelClinicalConsultation(
      @PathVariable Long consultationId,
      @Valid @RequestBody CancelClinicalConsultationResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return respond(HttpStatus.OK, commandService.handle(
        new CancelClinicalConsultationCommand(
            consultationId, currentUser.getId(), resource.reason())));
  }

  private static ResponseEntity<ClinicalConsultationResource> respond(
      HttpStatus status, Optional<ClinicalConsultation> consultation) {
    return consultation
        .map(value -> ResponseEntity.status(status).body(toResource(value)))
        .orElseGet(() -> ResponseEntity.badRequest().build());
  }

  private static ClinicalConsultationResource toResource(ClinicalConsultation consultation) {
    return ClinicalConsultationResourceFromEntityAssembler.toResourceFromEntity(consultation);
  }
}
