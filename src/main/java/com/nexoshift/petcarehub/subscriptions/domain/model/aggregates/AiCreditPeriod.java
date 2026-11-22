package com.nexoshift.petcarehub.subscriptions.domain.model.aggregates;

import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.ActiveSubscriptionRequiredException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.AiCreditLimitExceededException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.InvalidAiCreditReservationStateException;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.AiCreditType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "ai_credit_periods",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"subscription_id", "period_start"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiCreditPeriod
        extends AuditableAbstractAggregateRoot<AiCreditPeriod> {

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(nullable = false)
    private Long accountId;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(nullable = false)
    private Instant periodEnd;

    @Column(nullable = false)
    private int computerVisionLimit;

    @Column(nullable = false)
    private int computerVisionReserved;

    @Column(nullable = false)
    private int computerVisionConsumed;

    @Column(nullable = false)
    private int llmLimit;

    @Column(nullable = false)
    private int llmReserved;

    @Column(nullable = false)
    private int llmConsumed;

    public AiCreditPeriod(Subscription subscription) {

        if (subscription == null || !subscription.isActive()) {
            throw new ActiveSubscriptionRequiredException();
        }

        this.subscriptionId = subscription.getId();
        this.accountId = subscription.getAccountId();

        this.periodStart = subscription.getCurrentPeriodStart();
        this.periodEnd = subscription.getCurrentPeriodEnd();

        var limits = subscription.getPlan().getLimits();

        this.computerVisionLimit =
                limits.getMonthlyComputerVisionCredits();

        this.llmLimit = limits.getMonthlyLlmCredits();

        this.computerVisionReserved = 0;
        this.computerVisionConsumed = 0;

        this.llmReserved = 0;
        this.llmConsumed = 0;
    }

    public int getComputerVisionAvailable() {

        return computerVisionLimit
                - computerVisionReserved
                - computerVisionConsumed;
    }

    public int getLlmAvailable() {

        return llmLimit
                - llmReserved
                - llmConsumed;
    }

    public int getAvailableCredits(AiCreditType creditType) {

        return switch (creditType) {

            case COMPUTER_VISION -> getComputerVisionAvailable();

            case LLM -> getLlmAvailable();
        };
    }

    public void reserve(AiCreditType creditType) {

        if (getAvailableCredits(creditType) <= 0) {
            throw new AiCreditLimitExceededException(creditType);
        }

        switch (creditType) {

            case COMPUTER_VISION -> computerVisionReserved++;

            case LLM -> llmReserved++;
        }
    }

    public void confirm(AiCreditType creditType) {

        switch (creditType) {

            case COMPUTER_VISION -> {

                if (computerVisionReserved <= 0) {
                    throw new InvalidAiCreditReservationStateException();
                }

                computerVisionReserved--;
                computerVisionConsumed++;
            }

            case LLM -> {

                if (llmReserved <= 0) {
                    throw new InvalidAiCreditReservationStateException();
                }

                llmReserved--;
                llmConsumed++;
            }
        }
    }

    public void release(AiCreditType creditType) {

        switch (creditType) {

            case COMPUTER_VISION -> {

                if (computerVisionReserved <= 0) {
                    throw new InvalidAiCreditReservationStateException();
                }

                computerVisionReserved--;
            }

            case LLM -> {

                if (llmReserved <= 0) {
                    throw new InvalidAiCreditReservationStateException();
                }

                llmReserved--;
            }
        }
    }
}