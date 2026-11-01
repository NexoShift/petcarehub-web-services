package com.nexoshift.petcarehub.profiles.application.internal.commandservices;

import com.nexoshift.petcarehub.profiles.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.PetOwnerNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreatePetCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetImageCommand;
import com.nexoshift.petcarehub.profiles.domain.services.PetCommandService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetOwnerRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of the pet command service.
 * Handles the creation and updating of pet profiles.
 *
 * @see PetCommandService
 * @see PetRepository
 */
@Service
public class PetCommandServiceImpl implements PetCommandService {

    private final PetRepository petRepository;
    private final PetOwnerRepository petOwnerRepository;

    /**
     * Constructs a pet command service.
     *
     * @param petRepository the pet repository
     * @param petOwnerRepository the pet owner repository
     */
    public PetCommandServiceImpl(
            PetRepository petRepository,
            PetOwnerRepository petOwnerRepository) {

        this.petRepository = petRepository;
        this.petOwnerRepository = petOwnerRepository;
    }

    /**
     * Creates a pet profile associated with an existing pet owner.
     *
     * @param command the create pet command
     * @return the created pet identifier
     */
    @Override
    public Long handle(CreatePetCommand command) {

        if (!petOwnerRepository.existsById(command.ownerId())) {
            throw new PetOwnerNotFoundException(command.ownerId());
        }

        var pet = new Pet(command);

        var savedPet = petRepository.save(pet);

        return savedPet.getId();
    }

    /**
     * Updates a pet's profile information.
     *
     * @param command the update pet command
     * @return the updated pet
     */
    @Override
    public Optional<Pet> handle(UpdatePetCommand command) {

        var pet = petRepository.findById(command.petId())
                .orElseThrow(() ->
                        new PetNotFoundException(command.petId()));

        pet.updateProfile(
                command.name(),
                command.species(),
                command.breed(),
                command.birthDate(),
                command.sex()
        );

        return Optional.of(petRepository.save(pet));
    }

    /**
     * Updates a pet's profile image.
     *
     * @param command the update pet image command
     * @return the updated pet
     */
    @Override
    public Optional<Pet> handle(UpdatePetImageCommand command) {

        var pet = petRepository.findById(command.petId())
                .orElseThrow(() ->
                        new PetNotFoundException(command.petId()));

        pet.updateProfileImage(command.profileImageKey());

        return Optional.of(petRepository.save(pet));
    }
}
