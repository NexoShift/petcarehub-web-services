package com.nexoshift.petcarehub.subscriptions.application.internal.commandservices;

import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.acl.ExternalIamService;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.ActiveSubscriptionRequiredException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.AiCreditReservationNotFoundException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.InvalidAiCreditReservationStateException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.SubscriptionAccessDeniedException;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.AiCreditPeriod;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.AiCreditReservation;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.InitializeAiCreditPeriodCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.ReserveAiCreditCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.ConfirmAiCreditReservationCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.ReleaseAiCreditReservationCommand;
import com.nexoshift.petcarehub.subscriptions.domain.services.AiCreditCommandService;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.AiCreditPeriodRepository;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.AiCreditReservationRepository;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AiCreditCommandServiceImpl
        implements AiCreditCommandService {

    private final SubscriptionRepository subscriptionRepository;

    private final AiCreditPeriodRepository aiCreditPeriodRepository;

    private final AiCreditReservationRepository aiCreditReservationRepository;

    private final ExternalIamService externalIamService;

    @Override
    @Transactional
    public void handle(InitializeAiCreditPeriodCommand command) {

        var subscription = subscriptionRepository
                .findByAccountIdForUpdate(command.accountId())
                .orElseThrow(ActiveSubscriptionRequiredException::new);

        if (!subscription.isActive()
                || !Objects.equals(
                subscription.getId(),
                command.subscriptionId())

                || !Objects.equals(
                subscription.getCurrentPeriodStart(),
                command.periodStart())

                || !Objects.equals(
                subscription.getCurrentPeriodEnd(),
                command.periodEnd())) {

            return;
        }

        getOrCreateCreditPeriod(subscription);
    }

    @Override
    @Transactional
    public Long handle(ReserveAiCreditCommand command) {

        Long accountId = resolveAccountId(
                command.requesterUserId());

        var subscription = subscriptionRepository
                .findByAccountIdForUpdate(accountId)
                .orElseThrow(ActiveSubscriptionRequiredException::new);

        if (!subscription.isActive()) {
            throw new ActiveSubscriptionRequiredException();
        }

        var creditPeriod = getOrCreateCreditPeriod(subscription);

        creditPeriod.reserve(command.creditType());

        aiCreditPeriodRepository.save(creditPeriod);

        var reservation = new AiCreditReservation(
                creditPeriod.getId(),
                command
        );

        return aiCreditReservationRepository
                .save(reservation)
                .getId();
    }

    @Override
    @Transactional
    public void handle(ConfirmAiCreditReservationCommand command) {

        var reservation = findReservationForUpdate(
                command.reservationId());

        if (!reservation.confirm()) {
            return;
        }

        var creditPeriod = findCreditPeriodForUpdate(
                reservation.getCreditPeriodId());

        creditPeriod.confirm(reservation.getCreditType());

        aiCreditPeriodRepository.save(creditPeriod);

        aiCreditReservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void handle(ReleaseAiCreditReservationCommand command) {

        var reservation = findReservationForUpdate(
                command.reservationId());

        if (!reservation.release()) {
            return;
        }

        var creditPeriod = findCreditPeriodForUpdate(
                reservation.getCreditPeriodId());

        creditPeriod.release(reservation.getCreditType());

        aiCreditPeriodRepository.save(creditPeriod);

        aiCreditReservationRepository.save(reservation);
    }

    private AiCreditPeriod getOrCreateCreditPeriod(
            Subscription subscription) {

        return aiCreditPeriodRepository
                .findBySubscriptionIdAndPeriodStartForUpdate(
                        subscription.getId(),
                        subscription.getCurrentPeriodStart())

                .orElseGet(() -> aiCreditPeriodRepository.save(
                        new AiCreditPeriod(subscription)));
    }

    private AiCreditReservation findReservationForUpdate(
            Long reservationId) {

        return aiCreditReservationRepository
                .findByIdForUpdate(reservationId)
                .orElseThrow(() ->
                        new AiCreditReservationNotFoundException(
                                reservationId));
    }

    private AiCreditPeriod findCreditPeriodForUpdate(
            Long creditPeriodId) {

        return aiCreditPeriodRepository
                .findByIdForUpdate(creditPeriodId)
                .orElseThrow(
                        InvalidAiCreditReservationStateException::new);
    }

    private Long resolveAccountId(Long requesterUserId) {

        Long authenticatedUserId =
                externalIamService.fetchAuthenticatedUserId();

        if (!Objects.equals(
                authenticatedUserId, requesterUserId)) {

            throw new SubscriptionAccessDeniedException();
        }

        Long accountId = externalIamService
                .fetchAccountIdByUserId(requesterUserId);

        if (accountId == null || accountId <= 0
                || !externalIamService.existsActiveAccount(accountId)) {

            throw new SubscriptionAccessDeniedException();
        }

        return accountId;
    }
}