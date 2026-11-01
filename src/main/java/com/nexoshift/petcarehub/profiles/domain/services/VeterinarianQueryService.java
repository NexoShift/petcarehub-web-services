package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Veterinarian;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetVeterinarianByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetVeterinarianByUserIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetVeterinariansByClinicIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling veterinarian queries.
 */
public interface VeterinarianQueryService {

    /**
     * Retrieves a veterinarian by its profile identifier.
     *
     * @param query the veterinarian identifier query
     * @return the veterinarian, if found
     */
    Optional<Veterinarian> handle(GetVeterinarianByIdQuery query);

    /**
     * Retrieves a veterinarian by its IAM user identifier.
     *
     * @param query the IAM user identifier query
     * @return the veterinarian, if found
     */
    Optional<Veterinarian> handle(GetVeterinarianByUserIdQuery query);

    /**
     * Retrieves all veterinarians belonging to a clinic.
     *
     * @param query the clinic identifier query
     * @return the clinic's veterinarians
     */
    List<Veterinarian> handle(GetVeterinariansByClinicIdQuery query);
}
