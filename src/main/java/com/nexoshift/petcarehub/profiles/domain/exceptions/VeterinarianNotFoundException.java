package com.nexoshift.petcarehub.profiles.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class VeterinarianNotFoundException extends NotFoundException {

    public VeterinarianNotFoundException(Long veterinarianId) {
        super("Veterinarian with ID " + veterinarianId + " was not found.");
    }
}
