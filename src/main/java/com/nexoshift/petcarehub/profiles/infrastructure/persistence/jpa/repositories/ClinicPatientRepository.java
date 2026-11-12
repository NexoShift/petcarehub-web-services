package com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.ClinicPatient;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPatientClinicsByPetIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPatientPetsByClinicIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing clinic patient registrations.
 */
@Repository
public interface ClinicPatientRepository extends JpaRepository<ClinicPatient, Long> {

    boolean existsByClinicIdAndPetId(ClinicId clinicId, PetId petId);

    List<ClinicPatient> findByClinicId(ClinicId clinicId);

    List<ClinicPatient> findByPetId(PetId petId);
}
