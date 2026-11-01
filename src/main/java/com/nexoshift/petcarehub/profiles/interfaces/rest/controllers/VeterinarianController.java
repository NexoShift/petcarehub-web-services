package com.nexoshift.petcarehub.profiles.interfaces.rest.controllers;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.storage.ProfileImageStorageService;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdateVeterinarianImageCommand;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetVeterinarianByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.services.VeterinarianCommandService;
import com.nexoshift.petcarehub.profiles.domain.services.VeterinarianQueryService;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.CreateVeterinarianResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.UpdateVeterinarianResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.VeterinarianResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.CreateVeterinarianCommandFromResourceAssembler;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.UpdateVeterinarianCommandFromResourceAssembler;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.VeterinarianResourceFromEntityAssembler;
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
 * REST controller for managing veterinarian profiles.
 */
@RestController
@RequestMapping(value = "/api/v1/veterinarians", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Veterinarians", description = "Available Veterinarian Endpoints")
public class VeterinarianController {

    private static final String IMAGE_FOLDER = "profiles/veterinarians";

    private final VeterinarianCommandService veterinarianCommandService;
    private final VeterinarianQueryService veterinarianQueryService;
    private final ProfileImageStorageService profileImageStorageService;

    /**
     * Constructs the veterinarian controller.
     *
     * @param veterinarianCommandService the command service
     * @param veterinarianQueryService   the query service
     * @param profileImageStorageService the image storage service
     */
    public VeterinarianController(
            VeterinarianCommandService veterinarianCommandService,
            VeterinarianQueryService veterinarianQueryService,
            ProfileImageStorageService profileImageStorageService) {

        this.veterinarianCommandService = veterinarianCommandService;
        this.veterinarianQueryService = veterinarianQueryService;
        this.profileImageStorageService = profileImageStorageService;
    }

    /**
     * Creates a veterinarian profile with an optional image.
     *
     * @param resource the created veterinarian resource
     * @return the created veterinarian resource
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a veterinarian", description = "Create a veterinarian profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Veterinarian created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Clinic not found"),
            @ApiResponse(responseCode = "409", description = "Profile already exists")
    })
    public ResponseEntity<VeterinarianResource> createVeterinarian(
            @ModelAttribute CreateVeterinarianResource resource) {

        var profileImageKey = profileImageStorageService.upload(
                resource.image(), IMAGE_FOLDER
        );

        Long veterinarianId;

        try {
            var command = CreateVeterinarianCommandFromResourceAssembler.toCommandFromResource(resource, profileImageKey);

            veterinarianId = veterinarianCommandService.handle(command);

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

        if (veterinarianId == null || veterinarianId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        var veterinarian = veterinarianQueryService.handle(
                new GetVeterinarianByIdQuery(veterinarianId)
        );

        if (veterinarian.isEmpty()) return ResponseEntity.notFound().build();

        var entity = veterinarian.get();
        var profileImageUrl = profileImageStorageService.generatePresignedUrl(entity.getProfileImageKey());

        var veterinarianResource = VeterinarianResourceFromEntityAssembler.toResourceFromEntity(entity, profileImageUrl);

        return new ResponseEntity<>(veterinarianResource, HttpStatus.CREATED);
    }

    /**
     * Retrieves a veterinarian by its identifier.
     *
     * @param veterinarianId the veterinarian identifier
     * @return the veterinarian resource
     */
    @GetMapping("/{veterinarianId}")
    @Operation(summary = "Get veterinarian by ID", description = "Get veterinarian by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veterinarian found"),
            @ApiResponse(responseCode = "404", description = "Veterinarian not found")
    })
    public ResponseEntity<VeterinarianResource> getVeterinarianById(
            @PathVariable Long veterinarianId) {

        var veterinarian = veterinarianQueryService.handle(
                new GetVeterinarianByIdQuery(veterinarianId)
        );

        if (veterinarian.isEmpty()) return ResponseEntity.notFound().build();

        var entity = veterinarian.get();
        var profileImageUrl = profileImageStorageService.generatePresignedUrl(
                entity.getProfileImageKey()
        );

        var veterinarianResource = VeterinarianResourceFromEntityAssembler
                .toResourceFromEntity(entity, profileImageUrl);

        return ResponseEntity.ok(veterinarianResource);
    }

    /**
     * Updates a veterinarian's personal and professional information.
     *
     * @param veterinarianId the veterinarian identifier
     * @param resource the update veterinarian resource
     * @return the updated veterinarian resource
     */
    @PutMapping("/{veterinarianId}")
    @Operation(
            summary = "Update veterinarian",
            description = "Update veterinarian information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veterinarian updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Veterinarian not found")
    })
    public ResponseEntity<VeterinarianResource> updateVeterinarian(
            @PathVariable Long veterinarianId,
            @RequestBody UpdateVeterinarianResource resource) {

        var command = UpdateVeterinarianCommandFromResourceAssembler
                .toCommandFromResource(veterinarianId, resource);

        var updatedVeterinarian = veterinarianCommandService.handle(command);

        if (updatedVeterinarian.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var entity = updatedVeterinarian.get();
        var profileImageUrl = profileImageStorageService.generatePresignedUrl(
                entity.getProfileImageKey()
        );

        var veterinarianResource = VeterinarianResourceFromEntityAssembler
                .toResourceFromEntity(entity, profileImageUrl);

        return ResponseEntity.ok(veterinarianResource);
    }


    /**
     * Replaces a veterinarian's profile image.
     *
     * @param veterinarianId the veterinarian identifier
     * @param image the new profile image
     * @return the updated veterinarian resource
     */
    @PutMapping(value = "/{veterinarianId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update veterinarian image", description = "Replace veterinarian profile image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image updated"),
            @ApiResponse(responseCode = "400", description = "Invalid image"),
            @ApiResponse(responseCode = "404", description = "Veterinarian not found")
    })
    public ResponseEntity<VeterinarianResource> updateVeterinarianImage(
            @PathVariable Long veterinarianId,
            @RequestPart("image") MultipartFile image) {

        var veterinarian = veterinarianQueryService.handle(
                new GetVeterinarianByIdQuery(veterinarianId)
        );

        if (veterinarian.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var previousImageKey = veterinarian.get().getProfileImageKey();
        var newImageKey = profileImageStorageService.upload(image, IMAGE_FOLDER);

        if (newImageKey == null) {
            return ResponseEntity.badRequest().build();
        }

        var command = new UpdateVeterinarianImageCommand(veterinarianId, newImageKey);

        var updatedVeterinarian = veterinarianCommandService.handle(command);

        if(updatedVeterinarian.isEmpty()) return ResponseEntity.notFound().build();

        if (previousImageKey != null && !previousImageKey.equals(newImageKey)) {
            profileImageStorageService.delete(previousImageKey);
        }

        var entity = updatedVeterinarian.get();

        var profileImageUrl = profileImageStorageService.generatePresignedUrl(entity.getProfileImageKey());

        var veterinarianResource = VeterinarianResourceFromEntityAssembler.toResourceFromEntity(entity, profileImageUrl);

        return ResponseEntity.ok(veterinarianResource);
    }

}