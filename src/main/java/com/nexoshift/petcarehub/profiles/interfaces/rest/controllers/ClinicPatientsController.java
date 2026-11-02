package com.nexoshift.petcarehub.profiles.interfaces.rest.controllers;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.storage.ProfileImageStorageService;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetClinicPatientByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPatientPetsByClinicIdQuery;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicPatientCommandService;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicPatientQueryService;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.ClinicPatientResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.CreateClinicPatientResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.PetResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.ClinicPatientResourceFromEntityAssembler;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.CreateClinicPatientCommandFromResourceAssembler;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.PetResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        value = "/api/v1/clinics/{clinicId}/patients",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Clinic Patients", description = "Available Clinic Patient Endpoints")
public class ClinicPatientsController {

    private final ClinicPatientCommandService clinicPatientCommandService;
    private final ClinicPatientQueryService clinicPatientQueryService;
    private final ProfileImageStorageService profileImageStorageService;

    public ClinicPatientsController(
            ClinicPatientCommandService clinicPatientCommandService,
            ClinicPatientQueryService clinicPatientQueryService,
            ProfileImageStorageService profileImageStorageService) {

        this.clinicPatientCommandService = clinicPatientCommandService;
        this.clinicPatientQueryService = clinicPatientQueryService;
        this.profileImageStorageService = profileImageStorageService;
    }

    /**
     * Registers a pet as a patient of a clinic.
     *
     * @param clinicId the clinic identifier
     * @param resource the create clinic patient resource
     * @return the created clinic patient resource
     */
    @PostMapping
    @Operation(
            summary = "Register clinic patient",
            description = "Register an existing pet as a clinic patient"
    )
    public ResponseEntity<ClinicPatientResource> createClinicPatient(
            @PathVariable Long clinicId,
            @RequestBody CreateClinicPatientResource resource) {

        var command = CreateClinicPatientCommandFromResourceAssembler
                .toCommandFromResource(clinicId, resource);

        var clinicPatientId = clinicPatientCommandService.handle(command);

        var query = new GetClinicPatientByIdQuery(clinicPatientId);
        var clinicPatient = clinicPatientQueryService.handle(query);

        if (clinicPatient.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var clinicPatientResource = ClinicPatientResourceFromEntityAssembler
                .toResourceFromEntity(clinicPatient.get());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicPatientResource);
    }

    /**
     * Retrieves the pets registered as patients of a clinic.
     *
     * @param clinicId the clinic identifier
     * @return the registered pet resources
     */
    @GetMapping
    @Operation(
            summary = "Get clinic patients",
            description = "Retrieve pets registered as patients of a clinic"
    )
    public ResponseEntity<List<PetResource>> getClinicPatients(
            @PathVariable Long clinicId) {

        var query = new GetPatientPetsByClinicIdQuery(clinicId);

        var pets = clinicPatientQueryService.handle(query);

        var resources = pets.stream()
                .map(pet -> {
                    var imageUrl = pet.getProfileImageKey() == null
                            ? null
                            : profileImageStorageService.generatePresignedUrl(
                            pet.getProfileImageKey());

                    return PetResourceFromEntityAssembler
                            .toResourceFromEntity(pet, imageUrl);
                })
                .toList();

        return ResponseEntity.ok(resources);
    }
}