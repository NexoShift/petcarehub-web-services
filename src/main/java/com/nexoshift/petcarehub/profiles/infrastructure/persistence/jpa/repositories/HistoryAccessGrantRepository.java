package com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.HistoryAccessGrant;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryAccessGrantRepository extends JpaRepository<HistoryAccessGrant, Long> {

    /** One grant per pet and clinic: giving access again renews it. */
    Optional<HistoryAccessGrant> findByPetIdAndClinicId(PetId petId, ClinicId clinicId);

    List<HistoryAccessGrant> findAllByPetIdOrderByGrantedAtDesc(PetId petId);
}
