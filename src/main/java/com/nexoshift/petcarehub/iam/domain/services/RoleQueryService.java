package com.nexoshift.petcarehub.iam.domain.services;

import com.nexoshift.petcarehub.iam.domain.model.entities.Role;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetAllRolesQuery;
import java.util.List;

public interface RoleQueryService {

  List<Role> handle(GetAllRolesQuery query);
}
