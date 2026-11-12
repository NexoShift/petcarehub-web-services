package com.nexoshift.petcarehub.profiles.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class PetNotFoundException extends NotFoundException {

    public PetNotFoundException(Long petId) {
        super("Pet with ID " + petId + " was not found.");
    }
}
