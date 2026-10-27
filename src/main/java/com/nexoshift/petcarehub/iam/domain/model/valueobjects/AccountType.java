package com.nexoshift.petcarehub.iam.domain.model.valueobjects;

import java.util.Set;

/** Commercial segment of an account (Cap. V: plans for owners and for clinics). */
public enum AccountType {
  PET_OWNER,
  CLINIC;

  /** Roles granted to the holder of an account of this type. */
  public Set<Roles> holderRoles() {
    return switch (this) {
      case PET_OWNER -> Set.of(Roles.ROLE_PET_OWNER);
      case CLINIC -> Set.of(Roles.ROLE_CLINIC_ADMIN, Roles.ROLE_VETERINARIAN);
    };
  }

  /** Roles granted to members invited to an account of this type. */
  public Set<Roles> memberRoles() {
    return switch (this) {
      case PET_OWNER -> Set.of();
      case CLINIC -> Set.of(Roles.ROLE_VETERINARIAN);
    };
  }

  public boolean allowsMembers() {
    return this == CLINIC;
  }
}
