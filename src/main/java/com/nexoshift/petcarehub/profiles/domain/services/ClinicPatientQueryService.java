package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.ClinicPatient;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface ClinicPatientQueryService {

    Optional<ClinicPatient> handle(GetClinicPatientByIdQuery query);

    List<Pet> handle(GetPatientPetsByClinicIdQuery query);

    List<Clinic> handle(GetPatientClinicsByPetIdQuery query);
}