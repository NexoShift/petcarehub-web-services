package com.nexoshift.petcarehub.profiles.application.internal.queryservices;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl.IamExternalService;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ClinicNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Veterinarian;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetVeterinarianByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetVeterinarianByUserIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetVeterinariansByClinicIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.profiles.domain.services.VeterinarianQueryService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicRepository;
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
    private final ClinicRepository clinicRepository;
    private final IamExternalService iamExternalService;

    /**
     * Constructs a veterinarian query service.
     *
     * @param veterinarianRepository the veterinarian repository
     * @param clinicRepository the clinic repository
     * @param iamExternalService the IAM external service
     */
    public VeterinarianQueryServiceImpl(
            VeterinarianRepository veterinarianRepository,
            ClinicRepository clinicRepository,
            IamExternalService iamExternalService) {
        this.veterinarianRepository = veterinarianRepository;
        this.clinicRepository = clinicRepository;
        this.iamExternalService = iamExternalService;
    }

    /**
     * Retrieves a veterinarian by its profile identifier.
     *
     * @param query the veterinarian identifier query
     * @return the veterinarian, if found
     */
    @Override
    public Optional<Veterinarian> handle(GetVeterinarianByIdQuery query) {

        validateAuthenticatedUser();
        return veterinarianRepository.findById(query.veterinarianId());
    }

    /**
     * Retrieves a veterinarian by its IAM user identifier.
     *
     * @param query the IAM user identifier query
     * @return the veterinarian, if found
     */
    @Override
    public Optional<Veterinarian> handle(GetVeterinarianByUserIdQuery query) {

        validateAuthenticatedUser();
        return veterinarianRepository.findByUserId(new UserId(query.userId()));
    }

    /**
     * Retrieves veterinarians associated with a clinic.
     *
     * @param query the clinic identifier query
     * @return the veterinarians belonging to the clinic
     */
    @Override
    public List<Veterinarian> handle(GetVeterinariansByClinicIdQuery query) {

        validateAuthenticatedUser();

        if (!clinicRepository.existsById(query.clinicId())) {
            throw new ClinicNotFoundException(query.clinicId());
        }

        return veterinarianRepository.findByClinicId(
                new ClinicId(query.clinicId()));
    }

    /**
     * Validates that the authenticated user is active.
     */
    private void validateAuthenticatedUser() {

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        if (authenticatedUserId == null || authenticatedUserId <= 0
                || !iamExternalService.existsActiveUser(authenticatedUserId)) {
            throw new ProfileOperationNotAllowedException(
                    "You are not authorized to access veterinarian profiles");
        }
    }
}
