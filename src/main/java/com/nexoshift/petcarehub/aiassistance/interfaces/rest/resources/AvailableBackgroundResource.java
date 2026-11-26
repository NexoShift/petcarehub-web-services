package com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources;

import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.BackgroundType;
import java.time.LocalDate;

/** A background the web shows as a checkbox. */
public record AvailableBackgroundResource(BackgroundType type, Long referenceId,
    LocalDate date, String title, String summary) {
}
