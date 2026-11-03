package com.nexoshift.petcarehub.profiles.interfaces.rest.controllers;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.storage.ProfileImageStorageService;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPatientClinicsByPetIdQuery;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicPatientQueryService;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.ClinicResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.ClinicResourceFromEntityAssembler;
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
 * REST controller for managing clinics associated with pets.
 */
@RestController
@RequestMapping(
        value = "/api/v1/pets/{petId}/clinics",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Pet Clinics", description = "Available Pet Clinic Endpoints")
public class PetClinicsController {

    private final ClinicPatientQueryService clinicPatientQueryService;
    private final ProfileImageStorageService profileImageStorageService;

    /**
     * Constructs the pet clinics controller.
     *
     * @param clinicPatientQueryService the clinic patient query service
     * @param profileImageStorageService the profile image storage service
     */
    public PetClinicsController(
            ClinicPatientQueryService clinicPatientQueryService,
            ProfileImageStorageService profileImageStorageService) {

        this.clinicPatientQueryService = clinicPatientQueryService;
        this.profileImageStorageService = profileImageStorageService;
    }

    /**
     * Retrieves clinics where a pet is registered as a patient.
     *
     * @param petId the pet identifier
     * @return the clinics associated with the pet
     */
    @GetMapping
    @Operation(summary = "Get pet clinics", description = "Retrieve clinics where a pet is registered as a patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clinics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    public ResponseEntity<List<ClinicResource>> getPetClinics(
            @PathVariable Long petId) {

        var query = new GetPatientClinicsByPetIdQuery(petId);

        var clinics = clinicPatientQueryService.handle(query);

        var resources = clinics.stream()
                .map(clinic -> {
                    var imageUrl = clinic.getProfileImageKey() == null
                            ? null
                            : profileImageStorageService.generatePresignedUrl(
                            clinic.getProfileImageKey()
                    );

                    return ClinicResourceFromEntityAssembler
                            .toResourceFromEntity(clinic, imageUrl);
                })
                .toList();

        return ResponseEntity.ok(resources);
    }
}
