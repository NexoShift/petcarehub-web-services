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
        var veterinarian = veterinarianRepository.findById(query.veterinarianId());

        if (veterinarian.isEmpty()) return Optional.empty();

        var clinicId = veterinarian.get().getClinicId().clinicId();

        var clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ClinicNotFoundException(clinicId));

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();
        var authenticatedAccountId = iamExternalService.fetchAccountIdByUserId(
                authenticatedUserId);

        var clinicAccountId = clinic.getAccountId().accountId();

        var hasValidRole = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_VETERINARIAN")
                || iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN");

        if (!hasValidRole || !clinicAccountId.equals(authenticatedAccountId)) {
            throw new ProfileOperationNotAllowedException(
                    "You cannot access this veterinarian's profile");
        }

        return veterinarian;
    }

    /**
     * Retrieves a veterinarian by its IAM user identifier.
     *
     * @param query the IAM user identifier query
     * @return the veterinarian, if found
     */
    @Override
    public Optional<Veterinarian> handle(GetVeterinarianByUserIdQuery query) {

        var veterinarian = veterinarianRepository.findByUserId(
                new UserId(query.userId())
        );

        if (veterinarian.isEmpty()) return Optional.empty();

        var clinicId = veterinarian.get().getClinicId().clinicId();

        var clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ClinicNotFoundException(clinicId));

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();
        var authenticatedAccountId = iamExternalService.fetchAccountIdByUserId(
                authenticatedUserId);

        var clinicAccountId = clinic.getAccountId().accountId();

        var hasValidRole = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_VETERINARIAN")
                || iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN");

        if (!hasValidRole || !clinicAccountId.equals(authenticatedAccountId)) {
            throw new ProfileOperationNotAllowedException(
                    "You cannot access this veterinarian's profile");
        }

        return veterinarian;
    }

    /**
     * Retrieves all veterinarians belonging to a clinic.
     *
     * @param query the clinic identifier query
     * @return the list of veterinarians
     */
    @Override
    public List<Veterinarian> handle(GetVeterinariansByClinicIdQuery query) {
        var clinic = clinicRepository.findById(query.clinicId())
                .orElseThrow(() ->
                        new ClinicNotFoundException(query.clinicId()));

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();
        var authenticatedAccountId = iamExternalService.fetchAccountIdByUserId(
                authenticatedUserId);

        var clinicAccountId = clinic.getAccountId().accountId();

        var hasValidRole = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_VETERINARIAN")
                || iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN");

        if (!hasValidRole || !clinicAccountId.equals(authenticatedAccountId)) {
            throw new ProfileOperationNotAllowedException(
                    "You cannot access this clinic's veterinarians");
        }

        return veterinarianRepository.findByClinicId(
                new ClinicId(query.clinicId())
        );
    }
}
