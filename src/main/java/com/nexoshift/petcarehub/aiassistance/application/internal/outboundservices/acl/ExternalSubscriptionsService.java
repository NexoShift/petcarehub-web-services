package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl;

/**
 * Port to the Subscriptions context: AI credits of the requester's plan.
 *
 * <p>Flow: reserve a credit before calling the model, confirm it when the model answered,
 * release it when the call failed. Without an active subscription the reservation fails
 * with 403; when the period's credits are used up it fails with 409.
 */
public interface ExternalSubscriptionsService {

  /** Reserves one computer vision credit and returns the reservation id. */
  Long reserveComputerVisionCredit(Long requesterUserId);

  /** Reserves one LLM credit and returns the reservation id. */
  Long reserveLlmCredit(Long requesterUserId);

  /** The model answered: the reserved credit is spent. */
  void confirmCredit(Long reservationId);

  /** The model failed: the reserved credit goes back to the period. */
  void releaseCredit(Long reservationId);
}
