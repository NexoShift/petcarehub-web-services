package com.nexoshift.petcarehub.profiles.application.internal.queryservices;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Veterinarian;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetVeterinarianByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetVeterinarianByUserIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetVeterinariansByClinicIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.profiles.domain.services.VeterinarianQueryService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.VeterinarianRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the veterinarian query service.
 * Handles queries related to veterinarian profiles.
 *
 * @see VeterinarianQueryService
 * @see VeterinarianRepository
 */
@Service
public class VeterinarianQueryServiceImpl implements VeterinarianQueryService {

    private final VeterinarianRepository veterinarianRepository;

    /**
     * Constructs a veterinarian query service.
     *
     * @param veterinarianRepository the veterinarian repository
     */
    public VeterinarianQueryServiceImpl(
            VeterinarianRepository veterinarianRepository) {
        this.veterinarianRepository = veterinarianRepository;
    }

    /**
     * Retrieves a veterinarian by its profile identifier.
     *
     * @param query the veterinarian identifier query
     * @return the veterinarian, if found
     */
    @Override
    public Optional<Veterinarian> handle(GetVeterinarianByIdQuery query) {
        return veterinarianRepository.findById(
                query.veterinarianId()
        );
    }

    /**
     * Retrieves a veterinarian by its IAM user identifier.
     *
     * @param query the IAM user identifier query
     * @return the veterinarian, if found
     */
    @Override
    public Optional<Veterinarian> handle(GetVeterinarianByUserIdQuery query) {
        return veterinarianRepository.findByUserId(
                new UserId(query.userId())
        );
    }

    /**
     * Retrieves all veterinarians belonging to a clinic.
     *
     * @param query the clinic identifier query
     * @return the list of veterinarians
     */
    @Override
    public List<Veterinarian> handle(GetVeterinariansByClinicIdQuery query) {
        return veterinarianRepository.findByClinicId(
                new ClinicId(query.clinicId())
        );
    }
}
