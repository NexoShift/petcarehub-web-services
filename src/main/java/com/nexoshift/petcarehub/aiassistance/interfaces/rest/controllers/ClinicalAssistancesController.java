package com.nexoshift.petcarehub.aiassistance.interfaces.rest.controllers;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.ClinicalAssistanceNotFoundException;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.ClinicalAssistance;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RegenerateClinicalAssistanceCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetClinicalAssistanceByConsultationIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetClinicalAssistanceByIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.services.ClinicalAssistanceCommandService;
import com.nexoshift.petcarehub.aiassistance.domain.services.ClinicalAssistanceQueryService;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.ClinicalAssistanceResource;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.CreateClinicalAssistanceResource;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.DraftDecisionResource;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform.ClinicalAssistanceResourceFromEntityAssembler;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform.CreateClinicalAssistanceCommandFromResourceAssembler;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform.DecideOnSoapDraftCommandFromResourceAssembler;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/ai/clinical-assistances",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "AI Clinical Assistance", description = "Preliminary SOAP drafts for veterinarians")
@PreAuthorize("hasRole('VETERINARIAN')")
public class ClinicalAssistancesController {

  private final ClinicalAssistanceCommandService commandService;
  private final ClinicalAssistanceQueryService queryService;
  private final FileStorageService fileStorageService;

  public ClinicalAssistancesController(ClinicalAssistanceCommandService commandService,
      ClinicalAssistanceQueryService queryService, FileStorageService fileStorageService) {
    this.commandService = commandService;
    this.queryService = queryService;
    this.fileStorageService = fileStorageService;
  }

  @PostMapping
  @Operation(summary = "Generate a preliminary SOAP draft from the selected backgrounds")
  public ResponseEntity<ClinicalAssistanceResource> requestClinicalAssistance(
      @Valid @RequestBody CreateClinicalAssistanceResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    var command = CreateClinicalAssistanceCommandFromResourceAssembler
        .toCommandFromResource(resource, currentUser.getId());
    var assistance = commandService.handle(command);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ClinicalAssistanceResourceFromEntityAssembler.toResourceFromEntity(assistance));
  }

  @GetMapping("/{clinicalAssistanceId}")
  @Operation(summary = "Get a clinical assistance with its drafts")
  public ResponseEntity<ClinicalAssistanceResource> getClinicalAssistanceById(
      @PathVariable Long clinicalAssistanceId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    var assistance = findOwned(clinicalAssistanceId, currentUser);
    return ResponseEntity.ok(
        ClinicalAssistanceResourceFromEntityAssembler.toResourceFromEntity(assistance));
  }

  @GetMapping(params = "consultationId")
  @Operation(summary = "Get the latest clinical assistance of a consultation (204 if none)")
  public ResponseEntity<ClinicalAssistanceResource> getClinicalAssistanceByConsultationId(
      @RequestParam Long consultationId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return queryService.handle(new GetClinicalAssistanceByConsultationIdQuery(consultationId))
        .filter(assistance -> assistance.belongsTo(currentUser.getId()))
        .map(ClinicalAssistanceResourceFromEntityAssembler::toResourceFromEntity)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.noContent().build());
  }

  @PostMapping("/{clinicalAssistanceId}/regenerations")
  @Operation(summary = "Generate another draft with the same backgrounds")
  public ResponseEntity<ClinicalAssistanceResource> regenerate(
      @PathVariable Long clinicalAssistanceId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    var assistance = commandService.handle(
        new RegenerateClinicalAssistanceCommand(clinicalAssistanceId, currentUser.getId()));
    return ResponseEntity.ok(
        ClinicalAssistanceResourceFromEntityAssembler.toResourceFromEntity(assistance));
  }

  @PutMapping("/{clinicalAssistanceId}/drafts/{draftId}/decision")
  @Operation(summary = "Accept, edit or discard a draft. Accept or edit also creates the PDF")
  public ResponseEntity<ClinicalAssistanceResource> decideOnDraft(
      @PathVariable Long clinicalAssistanceId, @PathVariable Long draftId,
      @Valid @RequestBody DraftDecisionResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    var command = DecideOnSoapDraftCommandFromResourceAssembler.toCommandFromResource(
        clinicalAssistanceId, draftId, currentUser.getId(), resource);
    var assistance = commandService.handle(command);
    return ResponseEntity.ok(
        ClinicalAssistanceResourceFromEntityAssembler.toResourceFromEntity(assistance));
  }

  @GetMapping(value = "/{clinicalAssistanceId}/drafts/{draftId}/report",
      produces = MediaType.APPLICATION_PDF_VALUE)
  @Operation(summary = "Download the confirmed report as PDF")
  public ResponseEntity<byte[]> getReport(@PathVariable Long clinicalAssistanceId,
      @PathVariable Long draftId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
    var assistance = findOwned(clinicalAssistanceId, currentUser);
    var draft = assistance.findDraft(draftId);
    if (!draft.hasReport()) {
      throw new NotFoundException("Draft " + draftId + " has no confirmed report");
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "inline; filename=informe-consulta-" + assistance.getConsultationId() + ".pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(fileStorageService.load(draft.getReport().storageKey()));
  }

  private ClinicalAssistance findOwned(Long clinicalAssistanceId, UserDetailsImpl currentUser) {
    var assistance = queryService.handle(new GetClinicalAssistanceByIdQuery(clinicalAssistanceId))
        .orElseThrow(() -> new ClinicalAssistanceNotFoundException(clinicalAssistanceId));
    if (!assistance.belongsTo(currentUser.getId())) {
      throw new ForbiddenException("This clinical assistance belongs to another veterinarian");
    }
    return assistance;
  }
}
