package com.nexoshift.petcarehub.iam.domain.repositories;

import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import java.util.Optional;

/** Read port for users. Users are modified and saved through their Account. */
public interface UserRepository {

  Optional<User> findById(Long id);

  Optional<User> findByEmailAddress(String address);

  boolean existsByEmailAddress(String address);
}
