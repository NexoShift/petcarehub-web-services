package com.nexoshift.petcarehub.iam.domain.model.aggregates;

import com.nexoshift.petcarehub.iam.domain.model.entities.Role;
import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import com.nexoshift.petcarehub.iam.domain.model.events.AccountRegisteredEvent;
import com.nexoshift.petcarehub.iam.domain.model.events.UserDeactivatedEvent;
import com.nexoshift.petcarehub.iam.domain.model.events.UserRegisteredEvent;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccessRole;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccountStatus;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccountType;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.EmailAddress;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.PasswordHash;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Aggregate Root of IAM (Cuenta). Groups the users of a pet owner or a clinic
 * and controls their life cycle.
 */
@Entity
public class Account extends AuditableAbstractAggregateRoot<Account> {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AccountType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AccountStatus status;

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
  private List<User> users = new ArrayList<>();

  protected Account() {
  }

  private Account(AccountType type) {
    this.type = type;
    this.status = AccountStatus.ACTIVE;
  }

  /** Creates an active account together with its holder user. */
  public static Account registerWithHolder(
      AccountType type, EmailAddress email, PasswordHash passwordHash, Set<Role> holderRoles) {
    if (type == null) {
      throw new IllegalArgumentException("Account type is required");
    }
    var account = new Account(type);
    account.users.add(new User(account, email, passwordHash, AccessRole.HOLDER, holderRoles));
    return account;
  }

  /** Registers the creation events. Call it after the first save, when ids exist. */
  public void notifyRegistered() {
    var holder = holder();
    var now = Instant.now();
    registerEvent(new AccountRegisteredEvent(getId(), type, holder.getId(), now));
    registerEvent(new UserRegisteredEvent(holder.getId(), getId(), holder.getEmailAddress(), now));
  }

  /** Adds a member user (for example, a veterinarian registered by a clinic). */
  public User addMember(EmailAddress email, PasswordHash passwordHash, Set<Role> memberRoles) {
    if (!type.allowsMembers()) {
      throw new IllegalStateException("Only clinic accounts can have members");
    }
    if (!isActive()) {
      throw new IllegalStateException("Members can only be added to active accounts");
    }
    var member = new User(this, email, passwordHash, AccessRole.MEMBER, memberRoles);
    users.add(member);
    return member;
  }

  /** Registers the creation event of a member. Call it after saving, when the id exists. */
  public void notifyMemberRegistered(User member) {
    registerEvent(new UserRegisteredEvent(
        member.getId(), getId(), member.getEmailAddress(), Instant.now()));
  }

  public User activateUser(Long userId) {
    var user = findUser(userId);
    user.activate();
    return user;
  }

  /** Deactivates a member. The holder cannot be deactivated. Users are never deleted. */
  public User deactivateUser(Long userId) {
    var user = findUser(userId);
    if (user.isHolder()) {
      throw new IllegalStateException("The account holder cannot be deactivated");
    }
    user.deactivate();
    registerEvent(new UserDeactivatedEvent(user.getId(), getId(), Instant.now()));
    return user;
  }

  public void activate() {
    if (status == AccountStatus.CANCELLED) {
      throw new IllegalStateException("A cancelled account cannot be activated again");
    }
    this.status = AccountStatus.ACTIVE;
  }

  public void suspend() {
    if (status != AccountStatus.ACTIVE) {
      throw new IllegalStateException("Only active accounts can be suspended");
    }
    this.status = AccountStatus.SUSPENDED;
  }

  public void cancel() {
    this.status = AccountStatus.CANCELLED;
  }

  public boolean isActive() {
    return status == AccountStatus.ACTIVE;
  }

  public User holder() {
    return users.stream()
        .filter(User::isHolder)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Account has no holder"));
  }

  public Optional<User> findUserByEmail(EmailAddress email) {
    return users.stream().filter(user -> user.getEmail().equals(email)).findFirst();
  }

  private User findUser(Long userId) {
    return users.stream()
        .filter(user -> user.getId().equals(userId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("User does not belong to this account"));
  }

  public AccountType getType() {
    return type;
  }

  public AccountStatus getStatus() {
    return status;
  }

  public List<User> getUsers() {
    return List.copyOf(users);
  }
}
