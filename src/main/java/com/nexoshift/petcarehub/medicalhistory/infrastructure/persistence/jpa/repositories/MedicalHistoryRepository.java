package com.nexoshift.petcarehub.medicalhistory.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.medicalhistory.domain.model.aggregates.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {

  @Query("select history from MedicalHistory history where history.petId.petId = :petId")
  Optional<MedicalHistory> findByPetId(@Param("petId") Long petId);
}
