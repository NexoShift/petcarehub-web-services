package com.nexoshift.petcarehub.iam.application.internal.queryservices;

import com.nexoshift.petcarehub.iam.domain.model.entities.Role;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetAllRolesQuery;
import com.nexoshift.petcarehub.iam.domain.repositories.RoleRepository;
import com.nexoshift.petcarehub.iam.domain.services.RoleQueryService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoleQueryServiceImpl implements RoleQueryService {

  private final RoleRepository roleRepository;

  public RoleQueryServiceImpl(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  public List<Role> handle(GetAllRolesQuery query) {
    return roleRepository.findAll();
  }
}
