package com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    boolean existsByAccountId(Long accountId);

    @EntityGraph(attributePaths = "plan")
    Optional<Subscription> findByAccountId(Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM Subscription s
            WHERE s.accountId = :accountId
            """)
    Optional<Subscription> findByAccountIdForUpdate(
            @Param("accountId") Long accountId
    );
}