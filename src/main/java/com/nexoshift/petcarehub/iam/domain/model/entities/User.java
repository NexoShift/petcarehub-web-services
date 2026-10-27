package com.nexoshift.petcarehub.iam.domain.model.entities;

import com.nexoshift.petcarehub.iam.domain.model.aggregates.Account;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccessRole;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.EmailAddress;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.PasswordHash;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.Roles;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.UserStatus;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

/** Authenticable identity. It belongs to the Account aggregate and is changed through it. */
@Entity
public class User extends AuditableModel {

  @Embedded
  @AttributeOverride(name = "address",
      column = @Column(name = "email", nullable = false, unique = true, length = 120))
  private EmailAddress email;

  @Embedded
  @AttributeOverride(name = "value",
      column = @Column(name = "password_hash", nullable = false, length = 120))
  private PasswordHash passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AccessRole accessRole;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserStatus status;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  protected User() {
  }

  public User(Account account, EmailAddress email, PasswordHash passwordHash,
      AccessRole accessRole, Set<Role> roles) {
    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("A user needs at least one role");
    }
    this.account = account;
    this.email = email;
    this.passwordHash = passwordHash;
    this.accessRole = accessRole;
    this.status = UserStatus.ACTIVE;
    this.roles = new HashSet<>(roles);
  }

  public void changePassword(PasswordHash newPasswordHash) {
    this.passwordHash = newPasswordHash;
  }

  public void activate() {
    this.status = UserStatus.ACTIVE;
  }

  public void deactivate() {
    this.status = UserStatus.INACTIVE;
  }

  public boolean isActive() {
    return status == UserStatus.ACTIVE && account.isActive();
  }

  public boolean isHolder() {
    return accessRole == AccessRole.HOLDER;
  }

  public boolean hasRole(Roles role) {
    return roles.stream().anyMatch(item -> item.getName() == role);
  }

  public boolean belongsTo(Long accountId) {
    return account.getId() != null && account.getId().equals(accountId);
  }

  public EmailAddress getEmail() {
    return email;
  }

  public String getEmailAddress() {
    return email.address();
  }

  public PasswordHash getPasswordHash() {
    return passwordHash;
  }

  public AccessRole getAccessRole() {
    return accessRole;
  }

  public UserStatus getStatus() {
    return status;
  }

  public Set<Role> getRoles() {
    return Set.copyOf(roles);
  }

  public Account getAccount() {
    return account;
  }

  public Long getAccountId() {
    return account.getId();
  }
}
