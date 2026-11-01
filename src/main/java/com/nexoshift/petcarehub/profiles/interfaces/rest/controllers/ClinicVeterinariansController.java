package com.nexoshift.petcarehub.profiles.interfaces.rest.controllers;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.storage.ProfileImageStorageService;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetClinicByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetVeterinariansByClinicIdQuery;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicQueryService;
import com.nexoshift.petcarehub.profiles.domain.services.VeterinarianQueryService;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.VeterinarianResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.VeterinarianResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for retrieving veterinarians associated with a clinic.
 */
@RestController
@RequestMapping(value = "/api/v1/clinics/{clinicId}/veterinarians", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clinics", description = "Available Clinic Endpoints")
public class ClinicVeterinariansController {

    private final ClinicQueryService clinicQueryService;
    private final VeterinarianQueryService veterinarianQueryService;
    private final ProfileImageStorageService profileImageStorageService;

    /**
     * Constructs the clinic veterinarians controller.
     *
     * @param clinicQueryService the clinic query service
     * @param veterinarianQueryService the veterinarian query service
     * @param profileImageStorageService the image storage service
     */
    public ClinicVeterinariansController(
            ClinicQueryService clinicQueryService,
            VeterinarianQueryService veterinarianQueryService,
            ProfileImageStorageService profileImageStorageService) {

        this.clinicQueryService = clinicQueryService;
        this.veterinarianQueryService = veterinarianQueryService;
        this.profileImageStorageService = profileImageStorageService;
    }

    /**
     * Retrieves the veterinarians associated with a clinic.
     *
     * @param clinicId the clinic identifier
     * @return the list of veterinarian resources
     */
    @GetMapping
    @Operation(summary = "Get veterinarians by clinic", description = "Retrieve all veterinarians belonging to a clinic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veterinarians retrieved"),
            @ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    public ResponseEntity<List<VeterinarianResource>> getVeterinariansByClinicId(
            @PathVariable Long clinicId) {

        var clinic = clinicQueryService.handle(new GetClinicByIdQuery(clinicId));

        if (clinic.isEmpty()) return ResponseEntity.notFound().build();

        var query = new GetVeterinariansByClinicIdQuery(clinicId);

        var veterinarians = veterinarianQueryService.handle(query);

        var veterinarianResources = veterinarians.stream()
                .map(veterinarian -> {
                    var profileImageUrl =
                            profileImageStorageService.generatePresignedUrl(
                                    veterinarian.getProfileImageKey());

                    return VeterinarianResourceFromEntityAssembler
                            .toResourceFromEntity(veterinarian, profileImageUrl);
                })
                .toList();

        return ResponseEntity.ok(veterinarianResources);
    }
}
