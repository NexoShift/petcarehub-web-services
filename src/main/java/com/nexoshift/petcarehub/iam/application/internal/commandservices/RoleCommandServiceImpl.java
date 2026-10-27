package com.nexoshift.petcarehub.iam.application.internal.commandservices;

import com.nexoshift.petcarehub.iam.domain.model.commands.SeedRolesCommand;
import com.nexoshift.petcarehub.iam.domain.model.entities.Role;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.Roles;
import com.nexoshift.petcarehub.iam.domain.repositories.RoleRepository;
import com.nexoshift.petcarehub.iam.domain.services.RoleCommandService;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleCommandServiceImpl implements RoleCommandService {

  private final RoleRepository roleRepository;

  public RoleCommandServiceImpl(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  @Transactional
  public void handle(SeedRolesCommand command) {
    Arrays.stream(Roles.values())
        .filter(role -> !roleRepository.existsByName(role))
        .forEach(role -> roleRepository.save(new Role(role)));
  }
}
