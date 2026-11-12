package com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.services;

import com.nexoshift.petcarehub.iam.domain.repositories.UserRepository;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("defaultUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) {
    var user = userRepository.findByEmailAddress(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    return UserDetailsImpl.build(user);
  }
}
