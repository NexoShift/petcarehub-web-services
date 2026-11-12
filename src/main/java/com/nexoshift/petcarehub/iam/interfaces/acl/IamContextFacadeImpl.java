package com.nexoshift.petcarehub.iam.interfaces.acl;

import com.nexoshift.petcarehub.iam.domain.model.commands.CreateMemberUserCommand;
import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetAccountByIdQuery;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetUserByIdQuery;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.Roles;
import com.nexoshift.petcarehub.iam.domain.services.AccountCommandService;
import com.nexoshift.petcarehub.iam.domain.services.AccountQueryService;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class IamContextFacadeImpl implements IamContextFacade {

  private static final Logger LOGGER = LoggerFactory.getLogger(IamContextFacadeImpl.class);

  private final AccountCommandService accountCommandService;
  private final AccountQueryService accountQueryService;

  public IamContextFacadeImpl(AccountCommandService accountCommandService,
      AccountQueryService accountQueryService) {
    this.accountCommandService = accountCommandService;
    this.accountQueryService = accountQueryService;
  }

  @Override
  public Long createMemberUser(Long accountId, String email, String password) {
    try {
      return accountCommandService.handle(new CreateMemberUserCommand(accountId, email, password))
          .map(User::getId)
          .orElse(0L);
    } catch (RuntimeException exception) {
      LOGGER.warn("Member user could not be created: {}", exception.getMessage());
      return 0L;
    }
  }

  @Override
  public Long fetchAccountIdByUserId(Long userId) {
    return accountQueryService.handle(new GetUserByIdQuery(userId))
        .map(User::getAccountId)
        .orElse(0L);
  }

  @Override
  public boolean existsActiveUser(Long userId) {
    return accountQueryService.handle(new GetUserByIdQuery(userId))
        .map(User::isActive)
        .orElse(false);
  }

  @Override
  public String fetchEmailByUserId(Long userId) {
    return accountQueryService.handle(new GetUserByIdQuery(userId))
        .map(User::getEmailAddress)
        .orElse("");
  }

  @Override
  public String fetchAccountTypeByAccountId(Long accountId) {
    return accountQueryService.handle(new GetAccountByIdQuery(accountId))
        .map(account -> account.getType().name())
        .orElse("");
  }

  /**
   * Checks whether an account exists and is active
   * @param accountId The identifier of the account
   * @return True if the account exists and is active, false otherwise.
   */
  @Override
  public boolean existsActiveAccount(Long accountId) {
    var getAccountByIdQuery = new GetAccountByIdQuery(accountId);
    var result = accountQueryService.handle(getAccountByIdQuery);
    if(result.isEmpty()) return false;
    return result.get().isActive();
  }

  /**
   * Checks whether a user is active and has the specified role
   * @param userId The id of the user.
   * @param roleName The name of the role.
   * @return True if the user is active and has the specified role, false otherwise.
   */
  @Override
  public boolean userHasRole(Long userId, String roleName) {
    var getUserByIdQuery = new GetUserByIdQuery(userId);
    var result = accountQueryService.handle(getUserByIdQuery);
    if(result.isEmpty() || roleName == null) return false;

    for(var role: Roles.values()){
      if(role.name().equals(roleName)){
        return result.get().isActive() && result.get().hasRole(role);
      }
    }
    return false;
  }

  /**
   * Checks whether a user is the active holder of the specified account
   * @param userId The identifier of the user.
   * @param accountId The identifier of the account.
   * @return True if the user is active an is the holder of the account, false otherwise.
   */
  @Override
  public boolean isAccountHolder(Long userId, Long accountId) {
    var getUserByIdQuery = new GetUserByIdQuery(userId);
    var result = accountQueryService.handle(getUserByIdQuery);
    if(result.isEmpty()) return false;
    return result.get().isActive() && result.get().isHolder() && result.get().belongsTo(accountId);
  }

  @Override
  public Long fetchAuthenticatedUserId() {

    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if(authentication == null || !authentication.isAuthenticated()) return 0L;
    if(!(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) return 0L;
    if(!userDetails.isEnabled()) return 0L;

    return userDetails.getId();
  }
}
