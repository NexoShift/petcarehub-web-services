package com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.AiCreditReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiCreditReservationRepository
        extends JpaRepository<AiCreditReservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM AiCreditReservation r
            WHERE r.id = :id
            """)
    Optional<AiCreditReservation> findByIdForUpdate(
            @Param("id") Long id
    );
}