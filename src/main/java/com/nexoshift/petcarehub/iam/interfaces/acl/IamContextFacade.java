package com.nexoshift.petcarehub.iam.interfaces.acl;

/**
 * Published contract (Open Host Service) of IAM. It is the only IAM type other
 * bounded contexts may use. It only exposes simple types.
 */
public interface IamContextFacade {

  /** Creates a member user in a clinic account. Returns the new user id, or 0 if it fails. */
  Long createMemberUser(Long accountId, String email, String password);

  /** Returns the account id of a user, or 0 if the user does not exist. */
  Long fetchAccountIdByUserId(Long userId);

  boolean existsActiveUser(Long userId);

  /** Returns the e-mail of a user, or an empty string if the user does not exist. */
  String fetchEmailByUserId(Long userId);

  /** Returns PET_OWNER or CLINIC, or an empty string if the account does not exist. */
  String fetchAccountTypeByAccountId(Long accountId);

  /** Returns true if the account exists and is active. */
  boolean existsActiveAccount(Long accountId);

  /** Returns true if the user is active and has the specified role. */
  boolean userHasRole(Long userId, String roleName);

  /** Returns true if the user is active and is the holder of the specified account. */
  boolean isAccountHolder(Long userId, Long accountId);

  /** Returns the authenticated user id, or 0 if no user is authenticated. */
  Long fetchAuthenticatedUserId();
}
