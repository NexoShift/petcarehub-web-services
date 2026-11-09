package com.nexoshift.petcarehub.profiles.interfaces.acl;

import java.util.Optional;

/**
 * Facade exposing profile information to other bounded contexts.
 */
public interface ProfileContextFacade {

    /**
     * Checks whether a pet exists.
     *
     * @param petId the pet identifier
     * @return true if the pet exists
     */
    boolean existsPet(Long petId);

    /**
     * Retrieves the basic profile of a pet (name, species, breed, sex and birth date).
     *
     * @param petId the pet identifier
     * @return the pet snapshot, or empty if the pet does not exist
     */
    Optional<PetProfileSnapshot> fetchPetProfileSnapshot(Long petId);

    /**
     * Retrieves the owner identifier of a pet.
     *
     * @param petId the pet identifier
     * @return the owner identifier, or zero if not found
     */
    Long fetchPetOwnerIdByPetId(Long petId);

    /**
     * Retrieves a pet identifier by its unique code.
     *
     * @param petCode the pet identification code
     * @return the pet identifier, or zero if not found
     */
    Long fetchPetIdByPetCode(String petCode);

    /**
     * Checks whether a pet owner exists.
     *
     * @param petOwnerId the pet owner identifier
     * @return true if the pet owner exists
     */
    boolean existsPetOwner(Long petOwnerId);

    /**
     * Retrieves the IAM user identifier of a pet owner.
     *
     * @param petOwnerId the pet owner identifier
     * @return the user identifier, or zero if not found
     */
    Long fetchUserIdByPetOwnerId(Long petOwnerId);

    /**
     * Checks whether a clinic exists.
     *
     * @param clinicId the clinic identifier
     * @return true if the clinic exists
     */
    boolean existsClinic(Long clinicId);

    /**
     * Retrieves the IAM account identifier of a clinic.
     *
     * @param clinicId the clinic identifier
     * @return the account identifier, or zero if not found
     */
    Long fetchAccountIdByClinicId(Long clinicId);

    /**
     * Checks whether a veterinarian exists.
     *
     * @param veterinarianId the veterinarian identifier
     * @return true if the veterinarian exists
     */
    boolean existsVeterinarian(Long veterinarianId);

    /**
     * Retrieves the clinic identifier of a veterinarian.
     *
     * @param veterinarianId the veterinarian identifier
     * @return the clinic identifier, or zero if not found
     */
    Long fetchClinicIdByVeterinarianId(Long veterinarianId);

    /**
     * Retrieves the IAM user identifier of a veterinarian.
     *
     * @param veterinarianId the veterinarian identifier
     * @return the user identifier, or zero if not found
     */
    Long fetchUserIdByVeterinarianId(Long veterinarianId);

    /**
     * Checks whether a consulting room is active in a clinic.
     *
     * @param clinicId the clinic identifier
     * @param consultingRoomId the consulting room identifier
     * @return true if the consulting room belongs to the clinic and is active
     */
    boolean existsActiveConsultingRoom(
            Long clinicId,
            Long consultingRoomId
    );

    /**
     * Checks whether a pet is registered as a patient of a clinic.
     *
     * @param clinicId the clinic identifier
     * @param petId the pet identifier
     * @return true if the pet is registered in the clinic
     */
    boolean isPetPatientOfClinic(Long clinicId, Long petId);
}
