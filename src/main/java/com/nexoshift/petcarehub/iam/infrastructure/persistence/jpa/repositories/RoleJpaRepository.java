package com.nexoshift.petcarehub.iam.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.iam.domain.model.entities.Role;
import com.nexoshift.petcarehub.iam.domain.repositories.RoleRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data adapter of the RoleRepository port. */
@Repository
public interface RoleJpaRepository extends JpaRepository<Role, Long>, RoleRepository {
}
