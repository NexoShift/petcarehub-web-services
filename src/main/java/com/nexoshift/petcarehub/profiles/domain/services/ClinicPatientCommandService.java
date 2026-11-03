package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateClinicPatientCommand;

/**
 * Service for handling clinic patient commands.
 */
public interface ClinicPatientCommandService {

    Long handle(CreateClinicPatientCommand command);
}
