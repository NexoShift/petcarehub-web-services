package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.commands.*;

import java.util.Optional;

/**
 * Service interface for handling clinic, consulting room,
 * and veterinarian assignment commands.
 */
public interface ClinicCommandService {

    /**
     * Creates a clinic profile.
     *
     * @param command the create clinic command
     * @return the created clinic identifier
     */
    Long handle(CreateClinicCommand command);

    /**
     * Updates a clinic's institutional information.
     *
     * @param command the update clinic command
     * @return the updated clinic, if found
     */
    Optional<Clinic> handle(UpdateClinicCommand command);

    /**
     * Updates a clinic's profile image.
     *
     * @param command the update clinic image command
     * @return the updated clinic, if found
     */
    Optional<Clinic> handle(UpdateClinicImageCommand command);

    /**
     * Creates a consulting room within a clinic.
     *
     * @param command the create consulting room command
     */
    void handle(CreateConsultingRoomCommand command);

    /**
     * Updates a consulting room.
     *
     * @param command the update consulting room command
     */
    void handle(UpdateConsultingRoomCommand command);

    /**
     * Activates a consulting room.
     *
     * @param command the activate consulting room command
     */
    void handle(ActivateConsultingRoomCommand command);

    /**
     * Deactivates a consulting room.
     *
     * @param command the deactivate consulting room command
     */
    void handle(DeactivateConsultingRoomCommand command);

    /**
     * Deletes a consulting room without assignment history.
     *
     * @param command the delete consulting room command
     */
    void handle(DeleteConsultingRoomCommand command);

    /**
     * Assigns a veterinarian to a consulting room,
     * automatically replacing its previous active assignment.
     *
     * @param command the assign veterinarian command
     */
    void handle(AssignVeterinarianCommand command);

    /**
     * Ends an active consulting room assignment.
     *
     * @param command the unassign veterinarian command
     */
    void handle(UnassignVeterinarianCommand command);
}
