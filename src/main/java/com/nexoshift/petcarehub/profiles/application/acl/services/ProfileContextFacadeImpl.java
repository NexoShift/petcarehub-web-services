package com.nexoshift.petcarehub.profiles.application.acl.services;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetCode;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.*;
import com.nexoshift.petcarehub.profiles.interfaces.acl.ProfileContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the profile context facade.
 *
 * @see ProfileContextFacade
 */
@Service
@Transactional(readOnly = true)
public class ProfileContextFacadeImpl implements ProfileContextFacade {

    private final PetRepository petRepository;
    private final PetOwnerRepository petOwnerRepository;
    private final ClinicRepository clinicRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final ClinicPatientRepository clinicPatientRepository;

    /**
     * Constructs the profile context facade.
     *
     * @param petRepository the pet repository
     * @param petOwnerRepository the pet owner repository
     * @param clinicRepository the clinic repository
     * @param veterinarianRepository the veterinarian repository
     * @param clinicPatientRepository the clinic patient repository
     */
    public ProfileContextFacadeImpl(
            PetRepository petRepository,
            PetOwnerRepository petOwnerRepository,
            ClinicRepository clinicRepository,
            VeterinarianRepository veterinarianRepository,
            ClinicPatientRepository clinicPatientRepository) {

        this.petRepository = petRepository;
        this.petOwnerRepository = petOwnerRepository;
        this.clinicRepository = clinicRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.clinicPatientRepository = clinicPatientRepository;
    }

    @Override
    public boolean existsPet(Long petId) {

        return petId != null && petId > 0
                && petRepository.existsById(petId);
    }

    @Override
    public Long fetchPetOwnerIdByPetId(Long petId) {

        if (petId == null || petId <= 0) {
            return 0L;
        }

        var pet = petRepository.findById(petId);

        if (pet.isEmpty()) {
            return 0L;
        }

        return pet.get().getOwnerId().ownerId();
    }

    @Override
    public Long fetchPetIdByPetCode(String petCode) {

        if (petCode == null || petCode.isBlank()) {
            return 0L;
        }

        PetCode code;

        try {
            code = new PetCode(petCode);
        } catch (IllegalArgumentException e) {
            return 0L;
        }

        var pet = petRepository.findByPetCode(code);

        if (pet.isEmpty()) {
            return 0L;
        }

        return pet.get().getId();
    }

    @Override
    public boolean existsPetOwner(Long petOwnerId) {

        return petOwnerId != null && petOwnerId > 0
                && petOwnerRepository.existsById(petOwnerId);
    }

    @Override
    public Long fetchUserIdByPetOwnerId(Long petOwnerId) {

        if (petOwnerId == null || petOwnerId <= 0) {
            return 0L;
        }

        var petOwner = petOwnerRepository.findById(petOwnerId);

        if (petOwner.isEmpty()) {
            return 0L;
        }

        return petOwner.get().getUserId().userId();
    }

    @Override
    public boolean existsClinic(Long clinicId) {

        return clinicId != null && clinicId > 0
                && clinicRepository.existsById(clinicId);
    }

    @Override
    public Long fetchAccountIdByClinicId(Long clinicId) {

        if (clinicId == null || clinicId <= 0) {
            return 0L;
        }

        var clinic = clinicRepository.findById(clinicId);

        if (clinic.isEmpty()) {
            return 0L;
        }

        return clinic.get().getAccountId().accountId();
    }

    @Override
    public boolean existsVeterinarian(Long veterinarianId) {

        return veterinarianId != null && veterinarianId > 0
                && veterinarianRepository.existsById(veterinarianId);
    }

    @Override
    public Long fetchClinicIdByVeterinarianId(Long veterinarianId) {

        if (veterinarianId == null || veterinarianId <= 0) {
            return 0L;
        }

        var veterinarian = veterinarianRepository.findById(veterinarianId);

        if (veterinarian.isEmpty()) {
            return 0L;
        }

        return veterinarian.get().getClinicId().clinicId();
    }

    @Override
    public Long fetchUserIdByVeterinarianId(Long veterinarianId) {

        if (veterinarianId == null || veterinarianId <= 0) {
            return 0L;
        }

        var veterinarian = veterinarianRepository.findById(veterinarianId);

        if (veterinarian.isEmpty()) {
            return 0L;
        }

        return veterinarian.get().getUserId().userId();
    }

    @Override
    public boolean existsActiveConsultingRoom(
            Long clinicId,
            Long consultingRoomId) {

        if (clinicId == null || clinicId <= 0
                || consultingRoomId == null || consultingRoomId <= 0) {
            return false;
        }

        var clinic = clinicRepository.findById(clinicId);

        if (clinic.isEmpty()) {
            return false;
        }

        return clinic.get().getConsultingRooms().stream()
                .anyMatch(room ->
                        room.getId().equals(consultingRoomId)
                                && room.isActive());
    }

    @Override
    public boolean isPetPatientOfClinic(Long clinicId, Long petId) {

        if (clinicId == null || clinicId <= 0
                || petId == null || petId <= 0) {
            return false;
        }

        return clinicPatientRepository.existsByClinicIdAndPetId(
                new ClinicId(clinicId),
                new PetId(petId)
        );
    }
}
