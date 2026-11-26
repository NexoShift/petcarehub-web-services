package com.nexoshift.petcarehub.profiles.domain;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.HistoryAccessGrant;
import com.nexoshift.petcarehub.profiles.domain.model.commands.GrantHistoryAccessCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.HistoryAccessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HistoryAccessGrantTest {

  private static final Instant NOW = Instant.parse("2026-11-25T10:00:00Z");

  private static HistoryAccessGrant grantFor(int hours) {
    return new HistoryAccessGrant(new GrantHistoryAccessCommand(1L, 2L, hours), NOW);
  }

  @Test
  @DisplayName("Active until the hours pass, then EXPIRED on its own")
  void expires() {
    var grant = grantFor(3);

    assertThat(grant.statusAt(NOW.plus(Duration.ofHours(2)))).isEqualTo(HistoryAccessStatus.ACTIVE);
    assertThat(grant.statusAt(NOW.plus(Duration.ofHours(3)))).isEqualTo(HistoryAccessStatus.EXPIRED);
  }

  @Test
  @DisplayName("Revoking ends it now; renewing starts a new period")
  void revokeAndRenew() {
    var grant = grantFor(3);
    grant.revoke(NOW.plus(Duration.ofMinutes(30)));
    assertThat(grant.isActiveAt(NOW.plus(Duration.ofHours(1)))).isFalse();
    assertThat(grant.statusAt(NOW.plus(Duration.ofHours(1)))).isEqualTo(HistoryAccessStatus.REVOKED);

    var later = NOW.plus(Duration.ofHours(10));
    grant.renew(24, later);
    assertThat(grant.isActiveAt(later.plus(Duration.ofHours(23)))).isTrue();
  }

  @Test
  @DisplayName("Hours must be between 1 and 72")
  void hoursRange() {
    assertThatThrownBy(() -> new GrantHistoryAccessCommand(1L, 2L, 0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new GrantHistoryAccessCommand(1L, 2L, 73))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(new GrantHistoryAccessCommand(1L, 2L, 72).hours()).isEqualTo(72);
  }
}
