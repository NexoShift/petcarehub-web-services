package com.nexoshift.petcarehub.iam.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import com.nexoshift.petcarehub.iam.domain.repositories.UserRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data adapter of the UserRepository port. */
@Repository
public interface UserJpaRepository extends JpaRepository<User, Long>, UserRepository {

  @Override
  @Query("select user from User user where user.email.address = :address")
  Optional<User> findByEmailAddress(@Param("address") String address);

  @Override
  @Query("select count(user) > 0 from User user where user.email.address = :address")
  boolean existsByEmailAddress(@Param("address") String address);
}
