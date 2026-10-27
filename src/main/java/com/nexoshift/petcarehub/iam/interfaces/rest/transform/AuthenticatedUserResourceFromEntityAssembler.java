package com.nexoshift.petcarehub.iam.interfaces.rest.transform;

import com.nexoshift.petcarehub.iam.domain.model.entities.Role;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AuthenticatedSession;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.AuthenticatedUserResource;

public final class AuthenticatedUserResourceFromEntityAssembler {

  private AuthenticatedUserResourceFromEntityAssembler() {
  }

  public static AuthenticatedUserResource toResourceFromEntity(AuthenticatedSession session) {
    var user = session.user();
    var roles = user.getRoles().stream().map(Role::getStringName).sorted().toList();
    return new AuthenticatedUserResource(user.getId(), user.getAccountId(),
        user.getAccount().getType().name(), user.getEmailAddress(), roles, session.token());
  }
}
