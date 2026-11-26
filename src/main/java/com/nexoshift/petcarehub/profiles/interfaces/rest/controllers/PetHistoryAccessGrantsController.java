package com.nexoshift.petcarehub.profiles.interfaces.rest.controllers;

import com.nexoshift.petcarehub.profiles.domain.model.commands.GrantHistoryAccessCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.RevokeHistoryAccessCommand;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetHistoryAccessGrantsByPetIdQuery;
import com.nexoshift.petcarehub.profiles.domain.services.HistoryAccessGrantCommandService;
import com.nexoshift.petcarehub.profiles.domain.services.HistoryAccessGrantQueryService;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.GrantHistoryAccessResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.HistoryAccessGrantResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.HistoryAccessGrantResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The owner decides, for a few hours, which clinic can see what other clinics recorded
 * about the pet. Without an active grant a clinic only sees its own records.
 */
@RestController
@RequestMapping(
        value = "/api/v1/pets/{petId}/history-access-grants",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "History Access", description = "Owner consent to share the history between clinics")
public class PetHistoryAccessGrantsController {

    private final HistoryAccessGrantCommandService commandService;
    private final HistoryAccessGrantQueryService queryService;

    public PetHistoryAccessGrantsController(HistoryAccessGrantCommandService commandService,
                                            HistoryAccessGrantQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PET_OWNER')")
    @Operation(summary = "Give a clinic access to the whole history for 1 to 72 hours (owner)")
    public ResponseEntity<HistoryAccessGrantResource> grant(
            @PathVariable Long petId, @RequestBody GrantHistoryAccessResource resource) {
        var grant = commandService.handle(
                new GrantHistoryAccessCommand(petId, resource.clinicId(), resource.hours()));
        return new ResponseEntity<>(
                HistoryAccessGrantResourceFromEntityAssembler.toResourceFromEntity(grant),
                HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List the access grants (owner: all; veterinarian: their clinic)")
    public ResponseEntity<List<HistoryAccessGrantResource>> list(@PathVariable Long petId) {
        var grants = queryService.handle(new GetHistoryAccessGrantsByPetIdQuery(petId));
        return ResponseEntity.ok(grants.stream()
                .map(HistoryAccessGrantResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @PutMapping("/{grantId}/revoke")
    @PreAuthorize("hasRole('PET_OWNER')")
    @Operation(summary = "End an access before it expires (owner)")
    public ResponseEntity<HistoryAccessGrantResource> revoke(
            @PathVariable Long petId, @PathVariable Long grantId) {
        var grant = commandService.handle(new RevokeHistoryAccessCommand(petId, grantId));
        return ResponseEntity.ok(
                HistoryAccessGrantResourceFromEntityAssembler.toResourceFromEntity(grant));
    }
}
