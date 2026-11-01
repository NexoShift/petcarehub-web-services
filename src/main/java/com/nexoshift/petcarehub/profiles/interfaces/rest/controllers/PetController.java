package com.nexoshift.petcarehub.profiles.interfaces.rest.controllers;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.storage.ProfileImageStorageService;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetImageCommand;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.services.PetCommandService;
import com.nexoshift.petcarehub.profiles.domain.services.PetQueryService;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.CreatePetResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.PetResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.UpdatePetResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.CreatePetCommandFromResourceAssembler;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.PetResourceFromEntityAssembler;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.UpdatePetCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for managing pet profiles.
 */
@RestController
@RequestMapping(value = "/api/v1/pets", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pets", description = "Available Pet Endpoints")
public class PetController {

    private static final String IMAGE_FOLDER = "profiles/pets";

    private final PetCommandService petCommandService;
    private final PetQueryService petQueryService;
    private final ProfileImageStorageService profileImageStorageService;

    /**
     * Constructs the pet controller.
     *
     * @param petCommandService the pet command service
     * @param petQueryService the pet query service
     * @param profileImageStorageService the image storage service
     */
    public PetController(
            PetCommandService petCommandService,
            PetQueryService petQueryService,
            ProfileImageStorageService profileImageStorageService) {

        this.petCommandService = petCommandService;
        this.petQueryService = petQueryService;
        this.profileImageStorageService = profileImageStorageService;
    }

    /**
     * Creates a new pet with an optional profile image.
     *
     * @param resource the create pet resource
     * @return the created pet resource
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a pet",
            description = "Create a pet profile with an optional image"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pet created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Pet owner not found")
    })
    public ResponseEntity<PetResource> createPet(
            @ModelAttribute CreatePetResource resource) {

        var profileImageKey = profileImageStorageService.upload(
                resource.image(),
                IMAGE_FOLDER
        );

        Long petId;

        try {
            var createPetCommand = CreatePetCommandFromResourceAssembler
                    .toCommandFromResource(resource, profileImageKey);

            petId = petCommandService.handle(createPetCommand);

        } catch (RuntimeException e) {
            if (profileImageKey != null) {
                try {
                    profileImageStorageService.delete(profileImageKey);
                } catch (RuntimeException cleanupError) {
                    e.addSuppressed(cleanupError);
                }
            }
            throw e;
        }

        if (petId == null || petId == 0L) {
            return ResponseEntity.badRequest().build();
        }

        var getPetByIdQuery = new GetPetByIdQuery(petId);
        var pet = petQueryService.handle(getPetByIdQuery);

        if (pet.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var petEntity = pet.get();

        var profileImageUrl = profileImageStorageService.generatePresignedUrl(
                petEntity.getProfileImageKey()
        );

        var petResource = PetResourceFromEntityAssembler
                .toResourceFromEntity(petEntity, profileImageUrl);

        return new ResponseEntity<>(petResource, HttpStatus.CREATED);
    }

    /**
     * Retrieves a pet by its identifier.
     *
     * @param petId the pet identifier
     * @return the pet resource
     */
    @GetMapping("/{petId}")
    @Operation(
            summary = "Get pet by ID",
            description = "Retrieve a pet profile by its identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet found"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    public ResponseEntity<PetResource> getPetById(
            @PathVariable Long petId) {

        var getPetByIdQuery = new GetPetByIdQuery(petId);
        var pet = petQueryService.handle(getPetByIdQuery);

        if (pet.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var petEntity = pet.get();

        var profileImageUrl = profileImageStorageService.generatePresignedUrl(
                petEntity.getProfileImageKey()
        );

        var petResource = PetResourceFromEntityAssembler
                .toResourceFromEntity(petEntity, profileImageUrl);

        return ResponseEntity.ok(petResource);
    }


    /**
     * Updates a pet's profile information.
     *
     * @param petId the pet identifier
     * @param resource the update pet resource
     * @return the updated pet resource
     */
    @PutMapping("/{petId}")
    @Operation(
            summary = "Update a pet",
            description = "Update a pet's profile information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    public ResponseEntity<PetResource> updatePet(
            @PathVariable Long petId,
            @RequestBody UpdatePetResource resource) {

        var updatePetCommand = UpdatePetCommandFromResourceAssembler.toCommandFromResource(petId, resource);

        var updatedPet = petCommandService.handle(updatePetCommand);

        if (updatedPet.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var petEntity = updatedPet.get();

        var profileImageUrl = profileImageStorageService.generatePresignedUrl(
                petEntity.getProfileImageKey()
        );

        var petResource = PetResourceFromEntityAssembler
                .toResourceFromEntity(petEntity, profileImageUrl);

        return ResponseEntity.ok(petResource);
    }

    /**
     * Updates a pet's profile image.
     *
     * @param petId the pet identifier
     * @param image the new profile image
     * @return the updated pet resource
     */
    @PutMapping(
            value = "/{petId}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Update pet image",
            description = "Replace a pet's profile image"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image updated"),
            @ApiResponse(responseCode = "400", description = "Invalid image"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    public ResponseEntity<PetResource> updatePetImage(
            @PathVariable Long petId,
            @RequestPart("image") MultipartFile image) {

        var getPetByIdQuery = new GetPetByIdQuery(petId);
        var pet = petQueryService.handle(getPetByIdQuery);

        if (pet.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var previousImageKey = pet.get().getProfileImageKey();

        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var newImageKey = profileImageStorageService.upload(
                image,
                IMAGE_FOLDER
        );

        var updatePetImageCommand = new UpdatePetImageCommand(
                petId,
                newImageKey
        );

        var updatedPet = petCommandService.handle(updatePetImageCommand);

        if (updatedPet.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (previousImageKey != null && !previousImageKey.equals(newImageKey)) {
            profileImageStorageService.delete(previousImageKey);
        }

        var petEntity = updatedPet.get();

        var profileImageUrl = profileImageStorageService.generatePresignedUrl(
                petEntity.getProfileImageKey()
        );

        var petResource = PetResourceFromEntityAssembler
                .toResourceFromEntity(petEntity, profileImageUrl);

        return ResponseEntity.ok(petResource);
    }
}
