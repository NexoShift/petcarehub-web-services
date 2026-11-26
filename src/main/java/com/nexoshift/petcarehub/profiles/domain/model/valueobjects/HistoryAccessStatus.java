package com.nexoshift.petcarehub.profiles.domain.model.valueobjects;

/**
 * State of a history access grant. It is calculated from the dates, it is not stored.
 */
public enum HistoryAccessStatus {
    ACTIVE,
    EXPIRED,
    REVOKED
}
