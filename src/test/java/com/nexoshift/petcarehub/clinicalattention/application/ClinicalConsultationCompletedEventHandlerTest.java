package com.nexoshift.petcarehub.clinicalattention.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexoshift.petcarehub.clinicalattention.application.internal.eventhandlers.ClinicalConsultationCompletedEventHandler;
import com.nexoshift.petcarehub.clinicalattention.domain.model.events.ClinicalConsultationCompletedEvent;
import com.nexoshift.petcarehub.clinicalattention.interfaces.acl.events.ClinicalConsultationCompletedIntegrationEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClinicalConsultationCompletedEventHandlerTest {

  @Test
  @DisplayName("The completed domain event is published again as the integration event")
  void republishesAsIntegrationEvent() {
    var published = new ArrayList<Object>();
    var handler = new ClinicalConsultationCompletedEventHandler(published::add);
    var completedAt = Instant.parse("2026-10-01T10:15:30Z");

    handler.on(new ClinicalConsultationCompletedEvent(1L, 5L, 100L, "Gastroenteritis", completedAt));

    assertThat(published).hasSize(1);
    assertThat(published.get(0)).isEqualTo(
        new ClinicalConsultationCompletedIntegrationEvent(
            1L, 5L, 100L, "Gastroenteritis", completedAt));
  }

  @Test
  @DisplayName("Every completed consultation produces its own integration event")
  void oneIntegrationEventPerConsultation() {
    List<Object> published = new ArrayList<>();
    var handler = new ClinicalConsultationCompletedEventHandler(published::add);

    handler.on(new ClinicalConsultationCompletedEvent(1L, 5L, 100L, "A", Instant.now()));
    handler.on(new ClinicalConsultationCompletedEvent(2L, 6L, 100L, "B", Instant.now()));

    assertThat(published).hasSize(2);
  }
}
