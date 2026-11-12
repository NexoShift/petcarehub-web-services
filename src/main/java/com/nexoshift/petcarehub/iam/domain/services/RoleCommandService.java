package com.nexoshift.petcarehub.iam.domain.services;

import com.nexoshift.petcarehub.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {

  void handle(SeedRolesCommand command);
}
