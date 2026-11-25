package com.nexoshift.petcarehub.subscriptions.domain.model.aggregates;

import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.InvalidAiCreditReservationStateException;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.ReserveAiCreditCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.AiCreditReservationStatus;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.AiCreditType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiCreditReservation
        extends AuditableAbstractAggregateRoot<AiCreditReservation> {

    @Column(nullable = false)
    private Long creditPeriodId;

    @Column(nullable = false)
    private Long requesterUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiCreditType creditType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiCreditReservationStatus status;

    public AiCreditReservation(
            Long creditPeriodId,
            ReserveAiCreditCommand command) {

        if (creditPeriodId == null || creditPeriodId <= 0) {
            throw new IllegalArgumentException(
                    "Credit period ID must be positive");
        }

        this.creditPeriodId = creditPeriodId;
        this.requesterUserId = command.requesterUserId();
        this.creditType = command.creditType();

        this.status = AiCreditReservationStatus.RESERVED;
    }

    public boolean confirm() {

        if (status == AiCreditReservationStatus.CONSUMED) {
            return false;
        }

        if (status != AiCreditReservationStatus.RESERVED) {
            throw new InvalidAiCreditReservationStateException();
        }

        this.status = AiCreditReservationStatus.CONSUMED;

        return true;
    }

    public boolean release() {

        if (status == AiCreditReservationStatus.RELEASED) {
            return false;
        }

        if (status != AiCreditReservationStatus.RESERVED) {
            throw new InvalidAiCreditReservationStateException();
        }

        this.status = AiCreditReservationStatus.RELEASED;

        return true;
    }
}