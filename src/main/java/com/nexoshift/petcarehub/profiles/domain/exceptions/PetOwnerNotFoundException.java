package com.nexoshift.petcarehub.profiles.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class PetOwnerNotFoundException extends NotFoundException {
    public PetOwnerNotFoundException(Long ownerId) {
        super("Pet owner with ID " + ownerId + " was not found.");
    }
}
