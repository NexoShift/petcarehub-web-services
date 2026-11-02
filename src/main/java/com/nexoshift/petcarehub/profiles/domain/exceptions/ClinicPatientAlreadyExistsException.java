package com.nexoshift.petcarehub.profiles.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

/**
 * Exception thrown when a pet is already registered in a clinic.
 */
public class ClinicPatientAlreadyExistsException extends ConflictException {

    public ClinicPatientAlreadyExistsException(
            Long clinicId, Long petId) {

        super("Pet " + petId
                + " is already registered in clinic " + clinicId);
    }
}
