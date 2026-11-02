package com.nexoshift.petcarehub.profiles.interfaces.rest.controllers;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.storage.ProfileImageStorageService;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetOwnerByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetsByOwnerIdQuery;
import com.nexoshift.petcarehub.profiles.domain.services.PetOwnerQueryService;
import com.nexoshift.petcarehub.profiles.domain.services.PetQueryService;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.PetResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.PetResourceFromEntityAssembler;
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
 * REST controller for managing pet owner and pet relationships.
 */
@RestController
@RequestMapping(value = "/api/v1/pet-owners/{petOwnerId}/pets", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pet Owners", description = "Available Pet Owner Endpoints")
public class PetOwnerPetsController {

    private final PetOwnerQueryService petOwnerQueryService;
    private final PetQueryService petQueryService;
    private final ProfileImageStorageService profileImageStorageService;

    /**
     * Constructs the pet owner pets controller.
     *
     * @param petOwnerQueryService the pet owner query service
     * @param petQueryService the pet query service
     * @param profileImageStorageService the image storage service
     */
    public PetOwnerPetsController(
            PetOwnerQueryService petOwnerQueryService,
            PetQueryService petQueryService,
            ProfileImageStorageService profileImageStorageService) {

        this.petOwnerQueryService = petOwnerQueryService;
        this.petQueryService = petQueryService;
        this.profileImageStorageService = profileImageStorageService;
    }

    /**
     * Retrieves all pets belonging to a pet owner.
     *
     * @param petOwnerId the pet owner profile identifier
     * @return the list of pet resources
     */
    @GetMapping
    @Operation(summary = "Get pets by pet owner", description = "Retrieve all pets belonging to a pet owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pets retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Pet owner not found")
    })
    public ResponseEntity<List<PetResource>> getPetsByPetOwnerId(@PathVariable Long petOwnerId) {

        var getPetOwnerByIdQuery = new GetPetOwnerByIdQuery(petOwnerId);
        var petOwner = petOwnerQueryService.handle(getPetOwnerByIdQuery);

        if (petOwner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var getPetsByOwnerIdQuery = new GetPetsByOwnerIdQuery(petOwnerId);
        var pets = petQueryService.handle(getPetsByOwnerIdQuery);

        var petResources = pets.stream()
                .map(pet -> {
                    var profileImageUrl =
                            profileImageStorageService.generatePresignedUrl(
                                    pet.getProfileImageKey()
                            );

                    return PetResourceFromEntityAssembler
                            .toResourceFromEntity(pet, profileImageUrl);
                })
                .toList();

        return ResponseEntity.ok(petResources);
    }
}