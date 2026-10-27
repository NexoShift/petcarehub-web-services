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
}
