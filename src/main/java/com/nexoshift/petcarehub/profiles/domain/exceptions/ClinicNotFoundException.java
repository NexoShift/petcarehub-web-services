package com.nexoshift.petcarehub.profiles.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class ClinicNotFoundException extends NotFoundException {

    public ClinicNotFoundException(Long clinicId) {
        super("Clinic with ID " + clinicId + " was not found.");
    }
}
