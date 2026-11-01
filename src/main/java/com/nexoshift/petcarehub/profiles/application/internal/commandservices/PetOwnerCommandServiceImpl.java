package com.nexoshift.petcarehub.profiles.application.internal.commandservices;

import com.nexoshift.petcarehub.profiles.domain.exceptions.PetOwnerNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileAlreadyExistsException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.PetOwner;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreatePetOwnerCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetOwnerCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetOwnerImageCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.profiles.domain.services.PetOwnerCommandService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetOwnerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of the pet owner command service.
 * Handles the creation and updating of pet owner profiles.
 *
 * @see PetOwnerCommandService
 * @see PetOwnerRepository
 */
@Service
public class PetOwnerCommandServiceImpl implements PetOwnerCommandService {

    private final PetOwnerRepository petOwnerRepository;

    /**
     * Constructs a pet owner command service.
     *
     * @param petOwnerRepository the pet owner repository
     */
    public PetOwnerCommandServiceImpl(PetOwnerRepository petOwnerRepository) {
        this.petOwnerRepository = petOwnerRepository;
    }

    /**
     * Creates a pet owner profile.
     *
     * @param command the create pet owner command
     * @return the created pet owner identifier
     */
    @Override
    public Long handle(CreatePetOwnerCommand command) {

        var userId = new UserId(command.userId());

        if (petOwnerRepository.existsByUserId(userId)) {
            throw new ProfileAlreadyExistsException(
                    "PetOwner", command.userId());
        }

        var petOwner = new PetOwner(command);

        var savedPetOwner = petOwnerRepository.save(petOwner);

        return savedPetOwner.getId();
    }

    /**
     * Updates a pet owner's personal information.
     *
     * @param command the update pet owner command
     * @return the updated pet owner
     */
    @Override
    public Optional<PetOwner> handle(UpdatePetOwnerCommand command) {

        var petOwner = petOwnerRepository.findById(command.petOwnerId());

        if (petOwner.isEmpty()) {
            throw new PetOwnerNotFoundException(command.petOwnerId());
        }

        var entity = petOwner.get();

        entity.updateProfile(
                command.firstName(),
                command.lastName(),
                command.phoneNumber(),
                command.sex()
        );

        petOwnerRepository.save(entity);

        return Optional.of(entity);
    }

    /**
     * Updates a pet owner's profile image.
     *
     * @param command the update pet owner image command
     * @return the updated pet owner
     */
    @Override
    public Optional<PetOwner> handle(UpdatePetOwnerImageCommand command) {

        var petOwner = petOwnerRepository.findById(command.petOwnerId())
                .orElseThrow(() ->
                        new PetOwnerNotFoundException(command.petOwnerId()));

        petOwner.updateProfileImage(command.profileImageKey());

        return Optional.of(petOwnerRepository.save(petOwner));
    }
}
