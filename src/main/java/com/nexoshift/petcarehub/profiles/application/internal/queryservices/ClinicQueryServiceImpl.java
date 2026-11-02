package com.nexoshift.petcarehub.profiles.application.internal.queryservices;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl.IamExternalService;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ClinicNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ConsultingRoomNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.entities.ConsultingRoom;
import com.nexoshift.petcarehub.profiles.domain.model.entities.ConsultingRoomAssignment;
import com.nexoshift.petcarehub.profiles.domain.model.queries.*;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.AccountId;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicQueryService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the clinic query service.
 * Handles queries related to clinics, consulting rooms,
 * and consulting room assignments.
 *
 * @see ClinicQueryService
 * @see ClinicRepository
 */
@Service
@Transactional(readOnly = true)
public class ClinicQueryServiceImpl implements ClinicQueryService {

    private final ClinicRepository clinicRepository;
    private final IamExternalService iamExternalService;

    /**
     * Constructs a clinic query service.
     *
     * @param clinicRepository the clinic repository
     * @param iamExternalService the IAM external service
     */
    public ClinicQueryServiceImpl(
            ClinicRepository clinicRepository,
            IamExternalService iamExternalService) {
        this.clinicRepository = clinicRepository;
        this.iamExternalService = iamExternalService;
    }

    /**
     * Retrieves a clinic by its profile identifier.
     *
     * @param query the clinic identifier query
     * @return the clinic, if found
     */
    @Override
    public Optional<Clinic> handle(GetClinicByIdQuery query) {
        var clinic = clinicRepository.findById(query.clinicId());

        if (clinic.isEmpty()) return Optional.empty();

        validateClinicAccess(clinic.get());

        return clinic;
    }

    /**
     * Retrieves a clinic by its IAM account identifier.
     *
     * @param query the IAM account identifier query
     * @return the clinic, if found
     */
    @Override
    public Optional<Clinic> handle(GetClinicByAccountIdQuery query) {

        var clinic = clinicRepository.findByAccountId(
                new AccountId(query.accountId())
        );

        if (clinic.isEmpty()) return Optional.empty();

        validateClinicAccess(clinic.get());

        return clinic;
    }

    /**
     * Retrieves a consulting room belonging to a clinic.
     *
     * @param query the consulting room identifier query
     * @return the consulting room, if found
     */
    @Override
    public Optional<ConsultingRoom> handle(GetConsultingRoomByIdQuery query) {
        var clinic = findClinic(query.clinicId());

        validateClinicAccess(clinic);

        return clinic.getConsultingRooms().stream()
                .filter(room ->
                        room.getId().equals(query.consultingRoomId()))
                .findFirst();
    }

    /**
     * Retrieves all consulting rooms belonging to a clinic.
     *
     * @param query the clinic identifier query
     * @return the list of consulting rooms
     */
    @Override
    public List<ConsultingRoom> handle(GetConsultingRoomsByClinicIdQuery query) {
        var clinic = findClinic(query.clinicId());

        validateClinicAccess(clinic);

        return List.copyOf(clinic.getConsultingRooms());
    }

    /**
     * Retrieves the assignment history of a clinic.
     *
     * @param query the clinic identifier query
     * @return the list of consulting room assignments
     */
    @Override
    public List<ConsultingRoomAssignment> handle(GetConsultingRoomAssignmentsByClinicIdQuery query) {

        var clinic = findClinic(query.clinicId());

        validateClinicAdmin(clinic);

        return List.copyOf(clinic.getAssignments());
    }

    /**
     * Retrieves the assignment history of a consulting room.
     *
     * @param query the consulting room identifier query
     * @return the list of consulting room assignments
     */
    @Override
    public List<ConsultingRoomAssignment> handle(
            GetAssignmentsByConsultingRoomIdQuery query) {

        var clinic = findClinic(query.clinicId());

        validateClinicAdmin(clinic);

        ensureConsultingRoomExists(
                clinic,
                query.consultingRoomId()
        );

        return clinic.getAssignments().stream()
                .filter(assignment ->
                        assignment.getConsultingRoomId()
                                .consultingRoomId()
                                .equals(query.consultingRoomId()))
                .toList();
    }

    /**
     * Retrieves the active assignment of a consulting room.
     *
     * @param query the consulting room identifier query
     * @return the active assignment, if present
     */
    @Override
    public Optional<ConsultingRoomAssignment> handle(
            GetActiveAssignmentByConsultingRoomIdQuery query) {

        var clinic = findClinic(query.clinicId());

        validateClinicAccess(clinic);

        ensureConsultingRoomExists(clinic, query.consultingRoomId());

        return clinic.getAssignments().stream()
                .filter(ConsultingRoomAssignment::isActive)
                .filter(assignment ->
                        assignment.getConsultingRoomId()
                                .consultingRoomId()
                                .equals(query.consultingRoomId()))
                .findFirst();
    }

    /**
     * Validates whether the authenticated user can access a clinic.
     *
     * @param clinic the clinic to validate
     */
    private void validateClinicAccess(Clinic clinic) {

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        var clinicAccountId = clinic.getAccountId().accountId();

        var authenticatedAccountId = iamExternalService.fetchAccountIdByUserId(
                authenticatedUserId
        );

        var hasValidRole = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN"
        ) || iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_VETERINARIAN"
        );

        if (!hasValidRole || !clinicAccountId.equals(authenticatedAccountId)) {
            throw new ProfileOperationNotAllowedException(
                    "You cannot access this clinic's information"
            );
        }
    }

    /**
     * Validates whether the authenticated user can access
     * a clinic's administrative information.
     *
     * @param clinic the clinic to validate
     */
    private void validateClinicAdmin(Clinic clinic) {

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        var clinicAccountId = clinic.getAccountId().accountId();

        var authenticatedAccountId = iamExternalService.fetchAccountIdByUserId(
                authenticatedUserId
        );

        var hasAdminRole = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN"
        );

        if (!hasAdminRole || !clinicAccountId.equals(authenticatedAccountId)) {
            throw new ProfileOperationNotAllowedException(
                    "You cannot access this clinic's administrative information"
            );
        }
    }

    /**
     * Retrieves a clinic or throws an exception if it does not exist.
     *
     * @param clinicId the clinic identifier
     * @return the clinic
     */
    private Clinic findClinic(Long clinicId) {
        return clinicRepository.findById(clinicId)
                .orElseThrow(() ->
                        new ClinicNotFoundException(clinicId));
    }

    /**
     * Ensures a consulting room belongs to the specified clinic.
     *
     * @param clinic the clinic
     * @param consultingRoomId the consulting room identifier
     */
    private void ensureConsultingRoomExists(
            Clinic clinic,
            Long consultingRoomId) {

        boolean exists = clinic.getConsultingRooms().stream()
                .anyMatch(room -> room.getId().equals(consultingRoomId));

        if (!exists) {
            throw new ConsultingRoomNotFoundException(consultingRoomId);
        }
    }
}
