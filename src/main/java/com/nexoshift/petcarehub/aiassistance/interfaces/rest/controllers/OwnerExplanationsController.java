package com.nexoshift.petcarehub.aiassistance.interfaces.rest.controllers;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetOwnerExplanationsByPetIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.services.OwnerExplanationCommandService;
import com.nexoshift.petcarehub.aiassistance.domain.services.OwnerExplanationQueryService;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.OwnerExplanationResource;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.RequestOwnerExplanationResource;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform.OwnerExplanationResourceFromEntityAssembler;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform.RequestOwnerExplanationCommandFromResourceAssembler;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** "Explain with AI": the pet owner asks about their pet and gets a plain-language answer. */
@RestController
@RequestMapping(value = "/api/v1/ai/owner-explanations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "AI Owner Explanations", description = "Plain-language answers for pet owners")
@PreAuthorize("hasRole('PET_OWNER')")
public class OwnerExplanationsController {

  private final OwnerExplanationCommandService commandService;
  private final OwnerExplanationQueryService queryService;
  private final ExternalProfilesService profilesService;

  public OwnerExplanationsController(OwnerExplanationCommandService commandService,
      OwnerExplanationQueryService queryService, ExternalProfilesService profilesService) {
    this.commandService = commandService;
    this.queryService = queryService;
    this.profilesService = profilesService;
  }

  @PostMapping
  @Operation(summary = "Ask a question about your pet (write #fail with the fake LLM to get 503)")
  public ResponseEntity<OwnerExplanationResource> requestOwnerExplanation(
      @Valid @RequestBody RequestOwnerExplanationResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    var command = RequestOwnerExplanationCommandFromResourceAssembler
        .toCommandFromResource(resource, currentUser.getId());
    var explanation = commandService.handle(command);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(OwnerExplanationResourceFromEntityAssembler.toResourceFromEntity(explanation));
  }

  @GetMapping(params = "petId")
  @Operation(summary = "Chat history of a pet, oldest first")
  public List<OwnerExplanationResource> getOwnerExplanationsByPetId(@RequestParam Long petId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    if (profilesService.fetchPetSummary(petId).isEmpty()) {
      throw new NotFoundException("Pet " + petId + " was not found");
    }
    if (!profilesService.isPetOwnedBy(petId, currentUser.getId())) {
      throw new ForbiddenException("You can only see the chat of your own pets");
    }
    return queryService.handle(new GetOwnerExplanationsByPetIdQuery(petId, currentUser.getId()))
        .stream()
        .map(OwnerExplanationResourceFromEntityAssembler::toResourceFromEntity)
        .toList();
  }
}
