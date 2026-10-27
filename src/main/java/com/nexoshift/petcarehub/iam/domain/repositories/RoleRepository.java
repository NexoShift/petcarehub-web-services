package com.nexoshift.petcarehub.iam.domain.repositories;

import com.nexoshift.petcarehub.iam.domain.model.entities.Role;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.Roles;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {

  Role save(Role role);

  List<Role> findAll();

  Optional<Role> findByName(Roles name);

  boolean existsByName(Roles name);
}
