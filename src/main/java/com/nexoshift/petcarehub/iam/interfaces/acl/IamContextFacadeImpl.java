package com.nexoshift.petcarehub.iam.interfaces.acl;

import com.nexoshift.petcarehub.iam.domain.model.commands.CreateMemberUserCommand;
import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetAccountByIdQuery;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetUserByIdQuery;
import com.nexoshift.petcarehub.iam.domain.services.AccountCommandService;
import com.nexoshift.petcarehub.iam.domain.services.AccountQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
}
