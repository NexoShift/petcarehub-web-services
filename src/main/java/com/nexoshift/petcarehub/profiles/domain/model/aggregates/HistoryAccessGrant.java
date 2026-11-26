package com.nexoshift.petcarehub.profiles.domain.model.aggregates;

import com.nexoshift.petcarehub.profiles.domain.model.commands.GrantHistoryAccessCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.HistoryAccessStatus;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

/**
 * Aggregate root: the owner's permission for a clinic to see what OTHER clinics recorded
 * about the pet (consultations, exams, vaccinations and the timeline).
 * <p>
 * Without an active grant, a clinic only sees its own records. The grant lasts a few
 * hours (1 to 72) and the owner can revoke it at any time. Allergies are always visible to
 * the treating clinics, because hiding them could harm the pet.
 * </p>
 */
@Getter
@Entity
public class HistoryAccessGrant extends AuditableAbstractAggregateRoot<HistoryAccessGrant> {

    @Embedded
    @AttributeOverride(name = "petId", column = @Column(name = "pet_id", nullable = false))
    private PetId petId;

    @Embedded
    @AttributeOverride(name = "clinicId", column = @Column(name = "clinic_id", nullable = false))
    private ClinicId clinicId;

    @Column(nullable = false)
    private Instant grantedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    protected HistoryAccessGrant() {
        super();
    }

    public HistoryAccessGrant(GrantHistoryAccessCommand command, Instant now) {
        this();
        this.petId = new PetId(command.petId());
        this.clinicId = new ClinicId(command.clinicId());
        this.grantedAt = now;
        this.expiresAt = now.plus(Duration.ofHours(command.hours()));
    }

    /** Giving access again to the same clinic starts a new period from now. */
    public void renew(int hours, Instant now) {
        this.grantedAt = now;
        this.expiresAt = now.plus(Duration.ofHours(hours));
        this.revokedAt = null;
    }

    /** Ends the access now. Revoking twice changes nothing. */
    public void revoke(Instant now) {
        if (revokedAt == null && expiresAt.isAfter(now)) {
            this.revokedAt = now;
        }
    }

    public boolean isActiveAt(Instant now) {
        return statusAt(now) == HistoryAccessStatus.ACTIVE;
    }

    public HistoryAccessStatus statusAt(Instant now) {
        if (revokedAt != null) {
            return HistoryAccessStatus.REVOKED;
        }
        return expiresAt.isAfter(now) ? HistoryAccessStatus.ACTIVE : HistoryAccessStatus.EXPIRED;
    }
}
