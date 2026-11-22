package com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "payment_webhook_receipts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentWebhookReceipt {

    @Id
    @Column(length = 255, nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private Instant processedAt;

    public PaymentWebhookReceipt(
            String eventId,
            String eventType) {

        this.eventId = eventId;
        this.eventType = eventType;
        this.processedAt = Instant.now();
    }
}