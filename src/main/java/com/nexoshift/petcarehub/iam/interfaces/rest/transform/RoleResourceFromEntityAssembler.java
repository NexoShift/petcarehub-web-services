package com.nexoshift.petcarehub.iam.interfaces.rest.transform;

import com.nexoshift.petcarehub.iam.domain.model.entities.Role;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.RoleResource;

public final class RoleResourceFromEntityAssembler {

  private RoleResourceFromEntityAssembler() {
  }

  public static RoleResource toResourceFromEntity(Role role) {
    return new RoleResource(role.getId(), role.getStringName());
  }
}
