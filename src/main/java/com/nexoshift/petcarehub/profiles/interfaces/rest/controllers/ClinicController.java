package com.nexoshift.petcarehub.profiles.interfaces.rest.controllers;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.storage.ProfileImageStorageService;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdateClinicImageCommand;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetClinicByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicCommandService;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicQueryService;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.ClinicResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.CreateClinicResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.UpdateClinicResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.ClinicResourceFromEntityAssembler;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.CreateClinicCommandFromResourceAssembler;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.UpdateClinicCommandFromResourceAssembler;
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
 * REST controller for managing clinic profiles.
 */
@RestController
@RequestMapping(value = "/api/v1/clinics", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clinics", description = "Available Clinic Endpoints")
public class ClinicController {

    private static final String IMAGE_FOLDER = "profiles/clinics";

    private final ClinicCommandService clinicCommandService;
    private final ClinicQueryService clinicQueryService;
    private final ProfileImageStorageService profileImageStorageService;

    /**
     * Constructs the clinic controller.
     *
     * @param clinicCommandService the clinic command service
     * @param clinicQueryService the clinic query service
     * @param profileImageStorageService the image storage service
     */
    public ClinicController(
            ClinicCommandService clinicCommandService,
            ClinicQueryService clinicQueryService,
            ProfileImageStorageService profileImageStorageService) {

        this.clinicCommandService = clinicCommandService;
        this.clinicQueryService = clinicQueryService;
        this.profileImageStorageService = profileImageStorageService;
    }

    /**
     * Creates a clinic profile with an optional image.
     *
     * @param resource the create clinic resource
     * @return the created clinic resource
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a clinic", description = "Create a clinic profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Clinic created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Clinic already exists")
    })
    public ResponseEntity<ClinicResource> createClinic(
            @ModelAttribute CreateClinicResource resource) {

        var profileImageKey = profileImageStorageService.upload(
                resource.image(), IMAGE_FOLDER
        );

        Long clinicId;

        try {
            var command = CreateClinicCommandFromResourceAssembler
                    .toCommandFromResource(resource, profileImageKey);

            clinicId = clinicCommandService.handle(command);

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

        if (clinicId == null || clinicId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        var clinic = clinicQueryService.handle(new GetClinicByIdQuery(clinicId));

        if (clinic.isEmpty()) return ResponseEntity.notFound().build();

        var clinicEntity = clinic.get();
        var profileImageUrl = profileImageStorageService.generatePresignedUrl(
                clinicEntity.getProfileImageKey()
        );

        var clinicResource = ClinicResourceFromEntityAssembler
                .toResourceFromEntity(clinicEntity, profileImageUrl);

        return new ResponseEntity<>(clinicResource, HttpStatus.CREATED);
    }

    /**
     * Retrieves a clinic by its identifier.
     *
     * @param clinicId the clinic identifier
     * @return the clinic resource
     */
    @GetMapping("/{clinicId}")
    @Operation(summary = "Get clinic by ID", description = "Get clinic by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clinic found"),
            @ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    public ResponseEntity<ClinicResource> getClinicById(
            @PathVariable Long clinicId) {

        var clinic = clinicQueryService.handle(new GetClinicByIdQuery(clinicId));

        if (clinic.isEmpty()) return ResponseEntity.notFound().build();

        var clinicEntity = clinic.get();
        var profileImageUrl = profileImageStorageService.generatePresignedUrl(
                clinicEntity.getProfileImageKey()
        );

        var clinicResource = ClinicResourceFromEntityAssembler
                .toResourceFromEntity(clinicEntity, profileImageUrl);

        return ResponseEntity.ok(clinicResource);
    }


    /**
     * Updates a clinic's information.
     *
     * @param clinicId the clinic identifier
     * @param resource the update clinic resource
     * @return the updated clinic resource
     */
    @PutMapping("/{clinicId}")
    @Operation(summary = "Update clinic", description = "Update clinic information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clinic updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    public ResponseEntity<ClinicResource> updateClinic(
            @PathVariable Long clinicId,
            @RequestBody UpdateClinicResource resource) {

        var command = UpdateClinicCommandFromResourceAssembler
                .toCommandFromResource(clinicId, resource);

        var updatedClinic = clinicCommandService.handle(command);

        if (updatedClinic.isEmpty()) return ResponseEntity.notFound().build();

        var clinicEntity = updatedClinic.get();
        var profileImageUrl = profileImageStorageService.generatePresignedUrl(
                clinicEntity.getProfileImageKey()
        );

        var clinicResource = ClinicResourceFromEntityAssembler
                .toResourceFromEntity(clinicEntity, profileImageUrl);

        return ResponseEntity.ok(clinicResource);
    }

    /**
     * Replaces a clinic's profile image.
     *
     * @param clinicId the clinic identifier
     * @param image the new image
     * @return the updated clinic resource
     */
    @PutMapping(
            value = "/{clinicId}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Update clinic image", description = "Replace clinic image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image updated"),
            @ApiResponse(responseCode = "400", description = "Invalid image"),
            @ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    public ResponseEntity<ClinicResource> updateClinicImage(
            @PathVariable Long clinicId,
            @RequestPart("image") MultipartFile image) {

        var query = new GetClinicByIdQuery(clinicId);
        var clinic = clinicQueryService.handle(query);

        if (clinic.isEmpty()) return ResponseEntity.notFound().build();

        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var previousImageKey = clinic.get().getProfileImageKey();
        var newImageKey = profileImageStorageService.upload(image, IMAGE_FOLDER);

        if (newImageKey == null) return ResponseEntity.badRequest().build();

        var command = new UpdateClinicImageCommand(clinicId, newImageKey);

        var updatedClinic = clinicCommandService.handle(command);

        if(updatedClinic.isEmpty()) return ResponseEntity.badRequest().build();

        if(previousImageKey != null && !previousImageKey.equals(newImageKey)) {
            profileImageStorageService.delete(previousImageKey);
        }

        var clinicEntity = updatedClinic.get();
        var profileImageUrl = profileImageStorageService.generatePresignedUrl(
                clinicEntity.getProfileImageKey()
        );

        var clinicResource = ClinicResourceFromEntityAssembler
                .toResourceFromEntity(clinicEntity, profileImageUrl);

        return ResponseEntity.ok(clinicResource);
    }
}
