package com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.AiCreditPeriod;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface AiCreditPeriodRepository
        extends JpaRepository<AiCreditPeriod, Long> {

    Optional<AiCreditPeriod> findBySubscriptionIdAndPeriodStart(
            Long subscriptionId,
            Instant periodStart
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM AiCreditPeriod p
            WHERE p.subscriptionId = :subscriptionId
            AND p.periodStart = :periodStart
            """)
    Optional<AiCreditPeriod> findBySubscriptionIdAndPeriodStartForUpdate(
            @Param("subscriptionId") Long subscriptionId,
            @Param("periodStart") Instant periodStart
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM AiCreditPeriod p
            WHERE p.id = :id
            """)
    Optional<AiCreditPeriod> findByIdForUpdate(
            @Param("id") Long id
    );
}