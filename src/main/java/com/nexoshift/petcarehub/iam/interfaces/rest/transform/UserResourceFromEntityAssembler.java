package com.nexoshift.petcarehub.iam.interfaces.rest.transform;

import com.nexoshift.petcarehub.iam.domain.model.entities.Role;
import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.UserResource;

public final class UserResourceFromEntityAssembler {

  private UserResourceFromEntityAssembler() {
  }

  public static UserResource toResourceFromEntity(User user) {
    var roles = user.getRoles().stream().map(Role::getStringName).sorted().toList();
    return new UserResource(user.getId(), user.getAccountId(), user.getAccount().getType().name(),
        user.getEmailAddress(), user.getAccessRole().name(), user.getStatus().name(), roles);
  }
}
