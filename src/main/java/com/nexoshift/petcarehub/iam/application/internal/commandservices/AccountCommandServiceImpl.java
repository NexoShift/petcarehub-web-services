package com.nexoshift.petcarehub.iam.application.internal.commandservices;

import com.nexoshift.petcarehub.iam.application.internal.outboundservices.hashing.HashingService;
import com.nexoshift.petcarehub.iam.application.internal.outboundservices.tokens.TokenService;
import com.nexoshift.petcarehub.iam.domain.exceptions.AccountNotFoundException;
import com.nexoshift.petcarehub.iam.domain.exceptions.AccountOperationNotAllowedException;
import com.nexoshift.petcarehub.iam.domain.exceptions.EmailAlreadyRegisteredException;
import com.nexoshift.petcarehub.iam.domain.exceptions.InactiveUserException;
import com.nexoshift.petcarehub.iam.domain.exceptions.InvalidCredentialsException;
import com.nexoshift.petcarehub.iam.domain.exceptions.RoleNotFoundException;
import com.nexoshift.petcarehub.iam.domain.exceptions.UserNotFoundException;
import com.nexoshift.petcarehub.iam.domain.model.aggregates.Account;
import com.nexoshift.petcarehub.iam.domain.model.commands.ActivateUserCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.CreateMemberUserCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.DeactivateUserCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.SignInCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.SignUpCommand;
import com.nexoshift.petcarehub.iam.domain.model.entities.Role;
import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AuthenticatedSession;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.EmailAddress;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.PasswordHash;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.Roles;
import com.nexoshift.petcarehub.iam.domain.repositories.AccountRepository;
import com.nexoshift.petcarehub.iam.domain.repositories.RoleRepository;
import com.nexoshift.petcarehub.iam.domain.repositories.UserRepository;
import com.nexoshift.petcarehub.iam.domain.services.AccountCommandService;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountCommandServiceImpl implements AccountCommandService {

  private final AccountRepository accountRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final HashingService hashingService;
  private final TokenService tokenService;

  public AccountCommandServiceImpl(AccountRepository accountRepository,
      UserRepository userRepository, RoleRepository roleRepository,
      HashingService hashingService, TokenService tokenService) {
    this.accountRepository = accountRepository;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.hashingService = hashingService;
    this.tokenService = tokenService;
  }

  @Override
  @Transactional
  public Optional<User> handle(SignUpCommand command) {
    var email = new EmailAddress(command.email());
    ensureEmailIsAvailable(email);
    var passwordHash = new PasswordHash(hashingService.encode(command.password()));
    var account = Account.registerWithHolder(
        command.accountType(), email, passwordHash, resolveRoles(command.accountType().holderRoles()));
    account = accountRepository.save(account);
    account.notifyRegistered();
    account = accountRepository.save(account);
    return Optional.of(account.holder());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AuthenticatedSession> handle(SignInCommand command) {
    var user = userRepository.findByEmailAddress(command.email().trim().toLowerCase())
        .orElseThrow(InvalidCredentialsException::new);
    if (!hashingService.matches(command.password(), user.getPasswordHash().value())) {
      throw new InvalidCredentialsException();
    }
    if (!user.isActive()) {
      throw new InactiveUserException();
    }
    var token = tokenService.generateToken(user.getEmailAddress());
    return Optional.of(new AuthenticatedSession(user, token));
  }

  @Override
  @Transactional
  public Optional<User> handle(CreateMemberUserCommand command) {
    var account = accountRepository.findById(command.accountId())
        .orElseThrow(() -> new AccountNotFoundException(command.accountId()));
    var email = new EmailAddress(command.email());
    ensureEmailIsAvailable(email);
    var passwordHash = new PasswordHash(hashingService.encode(command.password()));
    account.addMember(email, passwordHash, resolveRoles(account.getType().memberRoles()));
    account = accountRepository.save(account);
    var member = account.findUserByEmail(email).orElseThrow();
    account.notifyMemberRegistered(member);
    accountRepository.save(account);
    return Optional.of(member);
  }

  @Override
  @Transactional
  public Optional<User> handle(ActivateUserCommand command) {
    var account = accountManagedBy(command.requesterUserId(), command.userId());
    var user = account.activateUser(command.userId());
    accountRepository.save(account);
    return Optional.of(user);
  }

  @Override
  @Transactional
  public Optional<User> handle(DeactivateUserCommand command) {
    var account = accountManagedBy(command.requesterUserId(), command.userId());
    var user = account.deactivateUser(command.userId());
    accountRepository.save(account);
    return Optional.of(user);
  }

  /** Returns the account of the target user if the requester is the holder of that account. */
  private Account accountManagedBy(Long requesterUserId, Long targetUserId) {
    var requester = userRepository.findById(requesterUserId)
        .orElseThrow(() -> new UserNotFoundException(requesterUserId));
    var target = userRepository.findById(targetUserId)
        .orElseThrow(() -> new UserNotFoundException(targetUserId));
    if (!requester.isHolder() || !target.belongsTo(requester.getAccountId())) {
      throw new AccountOperationNotAllowedException(
          "Only the holder of the account can manage its users");
    }
    return accountRepository.findById(requester.getAccountId())
        .orElseThrow(() -> new AccountNotFoundException(requester.getAccountId()));
  }

  private void ensureEmailIsAvailable(EmailAddress email) {
    if (userRepository.existsByEmailAddress(email.address())) {
      throw new EmailAlreadyRegisteredException(email.address());
    }
  }

  private Set<Role> resolveRoles(Set<Roles> roleNames) {
    return roleNames.stream()
        .map(name -> roleRepository.findByName(name)
            .orElseThrow(() -> new RoleNotFoundException(name.name())))
        .collect(Collectors.toSet());
  }
}
