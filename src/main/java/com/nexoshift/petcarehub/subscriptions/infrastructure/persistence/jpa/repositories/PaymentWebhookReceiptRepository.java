package com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.entities.PaymentWebhookReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentWebhookReceiptRepository
        extends JpaRepository<PaymentWebhookReceipt, String> {
}