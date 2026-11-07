package com.nexoshift.petcarehub.iam.domain;

import com.nexoshift.petcarehub.iam.domain.model.aggregates.Account;
import com.nexoshift.petcarehub.iam.domain.model.entities.Role;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

  private static Set<Role> roles(Set<Roles> names) {
    return names.stream().map(Role::new).collect(Collectors.toSet());
  }

  private static Account clinic() {
    return Account.registerWithHolder(AccountType.CLINIC, new EmailAddress("admin@clinic.test"),
        new PasswordHash("hash"), roles(AccountType.CLINIC.holderRoles()));
  }

  @Test
  @DisplayName("El titular se registra como HOLDER y no tiene que cambiar su contraseña")
  void holderIsCreatedWithoutTemporaryPassword() {
    var holder = clinic().holder();

    assertThat(holder.getAccessRole()).isEqualTo(AccessRole.HOLDER);
    assertThat(holder.hasRole(Roles.ROLE_CLINIC_ADMIN)).isTrue();
    assertThat(holder.hasRole(Roles.ROLE_VETERINARIAN)).isTrue();
    assertThat(holder.mustChangePassword()).isFalse();
  }

  @Test
  @DisplayName("Un miembro nuevo es MEMBER, solo veterinario y debe cambiar su contraseña")
  void memberStartsWithTemporaryPassword() {
    var member = clinic().addMember(new EmailAddress("vet@clinic.test"),
        new PasswordHash("temp"), roles(AccountType.CLINIC.memberRoles()));

    assertThat(member.getAccessRole()).isEqualTo(AccessRole.MEMBER);
    assertThat(member.hasRole(Roles.ROLE_VETERINARIAN)).isTrue();
    assertThat(member.hasRole(Roles.ROLE_CLINIC_ADMIN)).isFalse();
    assertThat(member.mustChangePassword()).isTrue();
  }

  @Test
  @DisplayName("Al cambiar la contraseña, deja de ser temporal")
  void changingThePasswordClearsTheFlag() {
    var member = clinic().addMember(new EmailAddress("vet@clinic.test"),
        new PasswordHash("temp"), roles(AccountType.CLINIC.memberRoles()));

    member.changePassword(new PasswordHash("new-hash"));

    assertThat(member.mustChangePassword()).isFalse();
  }

  @Test
  @DisplayName("Una cuenta de dueño no puede tener miembros")
  void petOwnerAccountsCannotHaveMembers() {
    var owner = Account.registerWithHolder(AccountType.PET_OWNER,
        new EmailAddress("ana@mail.test"), new PasswordHash("hash"),
        roles(AccountType.PET_OWNER.holderRoles()));

    assertThatThrownBy(() -> owner.addMember(new EmailAddress("x@mail.test"),
        new PasswordHash("temp"), roles(Set.of(Roles.ROLE_VETERINARIAN))))
        .isInstanceOf(IllegalStateException.class);
  }
}
