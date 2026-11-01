package com.nexoshift.petcarehub.profiles.interfaces.rest.controllers;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.storage.ProfileImageStorageService;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetOwnerImageCommand;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetOwnerByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.services.PetOwnerCommandService;
import com.nexoshift.petcarehub.profiles.domain.services.PetOwnerQueryService;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.CreatePetOwnerResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.PetOwnerResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.UpdatePetOwnerResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.CreatePetOwnerCommandFromResourceAssembler;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.PetOwnerResourceFromEntityAssembler;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.UpdatePetOwnerCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Objects;
import java.util.Set;

/**
 * REST controller for managing pet owner profiles.
 *
 * @see PetOwnerCommandService
 * @see PetOwnerQueryService
 */
@RestController
@RequestMapping(value = "/api/v1/pet-owners", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pet Owners", description = "Available Pet Owner Profiles endpoints")
public class PetOwnerController {

    private static final String IMAGE_FOLDER = "profiles/owners";

    //Command, Query and File Storage Services
    private final PetOwnerCommandService petOwnerCommandService;
    private final PetOwnerQueryService petOwnerQueryService;
    private final ProfileImageStorageService profileImageStorageService;

    /**
     * Constructs the pet owner controller.
     *
     * @param petOwnerCommandService the pet owner command service
     * @param petOwnerQueryService the pet owner query service
     */
    public PetOwnerController(
            PetOwnerCommandService petOwnerCommandService,
            PetOwnerQueryService petOwnerQueryService,
            ProfileImageStorageService profileImageStorageService) {

        this.petOwnerCommandService = petOwnerCommandService;
        this.petOwnerQueryService = petOwnerQueryService;
        this.profileImageStorageService= profileImageStorageService;
    }


    /**
     * Creates a new pet owner profile.
     *
     * @param resource the created pet owner resource
     * @return the created pet owner resource
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a pet owner", description = "Create a pet owner profile with its profile image (optional)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pet owner created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Pet owner profile already exists")
    })
    public ResponseEntity<PetOwnerResource> createPetOwner(@ModelAttribute CreatePetOwnerResource resource) {

        var profileImageKey = profileImageStorageService.upload(resource.image(), IMAGE_FOLDER);

        var createPetOwnerCommand = CreatePetOwnerCommandFromResourceAssembler.toCommandFromResource(resource, profileImageKey);

        Long petOwnerId;

        try {
            petOwnerId = petOwnerCommandService.handle(createPetOwnerCommand);
        }catch (RuntimeException e){
            if(profileImageKey != null){
                try {
                    profileImageStorageService.delete(profileImageKey);
                }catch (RuntimeException cleanupError){
                    e.addSuppressed(cleanupError);
                }
            }
            throw e;
        }

        var petOwner = petOwnerQueryService.handle(new GetPetOwnerByIdQuery(petOwnerId));
        if(petOwner.isEmpty()) return ResponseEntity.notFound().build();

        var entity = petOwner.get();
        var profileImageUrl = profileImageStorageService.generatePresignedUrl(entity.getProfileImageKey());

        var petOwnerResource = PetOwnerResourceFromEntityAssembler.toResourceFromEntity(petOwner.get(), profileImageUrl);

        return new ResponseEntity<>(petOwnerResource,HttpStatus.CREATED);
    }

    /**
     * Retrieves a pet owner profile by its identifier.
     *
     * @param petOwnerId the pet owner profile identifier
     * @return the pet owner resource
     */
    @GetMapping("/{petOwnerId}")
    @Operation(summary = "Get pet owner by ID", description = "Retrieve a pet owner profile by its identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet owner found"),
            @ApiResponse(responseCode = "404", description = "Pet owner not found")
    })
    public ResponseEntity<PetOwnerResource> getPetOwnerById(@PathVariable Long petOwnerId) {

        var query = new GetPetOwnerByIdQuery(petOwnerId);

        var petOwner = petOwnerQueryService.handle(query);

        if(petOwner.isEmpty()) return ResponseEntity.notFound().build();

        var entity = petOwner.get();
        var profileImageUrl = profileImageStorageService.generatePresignedUrl(entity.getProfileImageKey());

        var petOwnerResource = PetOwnerResourceFromEntityAssembler.toResourceFromEntity(petOwner.get(), profileImageUrl);

        return ResponseEntity.ok(petOwnerResource);
    }

    /**
     * Updates a pet owner's personal information.
     *
     * @param petOwnerId the pet owner profile identifier
     * @param resource the update pet owner resource
     * @return the updated pet owner resource
     */
    @PutMapping("/{petOwnerId}")
    @Operation(summary = "Update a pet owner", description = "Update a pet owner's personal information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet owner updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Pet owner not found")
    })
    public ResponseEntity<PetOwnerResource> updatePetOwner(
            @PathVariable Long petOwnerId,
            @RequestBody UpdatePetOwnerResource resource) {

        var command = UpdatePetOwnerCommandFromResourceAssembler.toCommandFromResource(petOwnerId, resource);

        var updatedPetOwner = petOwnerCommandService.handle(command);

        if(updatedPetOwner.isEmpty()) return ResponseEntity.notFound().build();

        var entity = updatedPetOwner.get();
        var profileImageUrl = profileImageStorageService.generatePresignedUrl(entity.getProfileImageKey());
        var petOwnerResource = PetOwnerResourceFromEntityAssembler.toResourceFromEntity(entity, profileImageUrl);

        return ResponseEntity.ok(petOwnerResource);
    }

    @PutMapping(value = "/{petOwnerId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetOwnerResource> updatePetOwnerImage(
            @PathVariable Long petOwnerId,
            @RequestPart("image") MultipartFile image){

        var petOwner = petOwnerQueryService.handle(new GetPetOwnerByIdQuery(petOwnerId));

        if(petOwner.isEmpty()) return ResponseEntity.notFound().build();

        var previousImageKey = petOwner.get().getProfileImageKey();

        var newImageKey = profileImageStorageService.upload(image,IMAGE_FOLDER);

        if(newImageKey == null) return ResponseEntity.badRequest().build();

        var command = new UpdatePetOwnerImageCommand(petOwnerId,newImageKey);

        var updatedPetOwner = petOwnerCommandService.handle(command);
        if(updatedPetOwner.isEmpty()) return ResponseEntity.notFound().build();

        if(previousImageKey != null && !previousImageKey.equalsIgnoreCase(newImageKey)) {
            profileImageStorageService.delete(previousImageKey);
        }

        var entity = updatedPetOwner.get();
        var profileImageUrl = profileImageStorageService.generatePresignedUrl(entity.getProfileImageKey());
        var petOwnerResource = PetOwnerResourceFromEntityAssembler.toResourceFromEntity(entity, profileImageUrl);
        return ResponseEntity.ok(petOwnerResource);
    }
}
