package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.entities.ConsultingRoom;
import com.nexoshift.petcarehub.profiles.domain.model.entities.ConsultingRoomAssignment;
import com.nexoshift.petcarehub.profiles.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling clinic, consulting room,
 * and veterinarian assignment queries.
 */
public interface ClinicQueryService {

    /**
     * Retrieves a clinic by its profile identifier.
     *
     * @param query the clinic identifier query
     * @return the clinic, if found
     */
    Optional<Clinic> handle(GetClinicByIdQuery query);

    /**
     * Retrieves a clinic by its IAM account identifier.
     *
     * @param query the IAM account identifier query
     * @return the clinic, if found
     */
    Optional<Clinic> handle(GetClinicByAccountIdQuery query);

    /**
     * Retrieves a consulting room belonging to a clinic.
     *
     * @param query the consulting room identifier query
     * @return the consulting room, if found
     */
    Optional<ConsultingRoom> handle(GetConsultingRoomByIdQuery query);

    /**
     * Retrieves all consulting rooms belonging to a clinic.
     *
     * @param query the clinic identifier query
     * @return the clinic's consulting rooms
     */
    List<ConsultingRoom> handle(GetConsultingRoomsByClinicIdQuery query);

    /**
     * Retrieves the assignment history of a clinic.
     *
     * @param query the clinic identifier query
     * @return the clinic's consulting room assignments
     */
    List<ConsultingRoomAssignment> handle(
            GetConsultingRoomAssignmentsByClinicIdQuery query);

    /**
     * Retrieves the assignment history of a consulting room.
     *
     * @param query the consulting room identifier query
     * @return the consulting room's assignment history
     */
    List<ConsultingRoomAssignment> handle(
            GetAssignmentsByConsultingRoomIdQuery query);

    /**
     * Retrieves the active assignment of a consulting room.
     *
     * @param query the consulting room identifier query
     * @return the active assignment, if present
     */
    Optional<ConsultingRoomAssignment> handle(
            GetActiveAssignmentByConsultingRoomIdQuery query);
}
