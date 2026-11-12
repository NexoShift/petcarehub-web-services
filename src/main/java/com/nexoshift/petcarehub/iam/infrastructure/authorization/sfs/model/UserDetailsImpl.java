package com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model;

import com.nexoshift.petcarehub.iam.domain.model.entities.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Authenticated principal. Controllers get it with @AuthenticationPrincipal. */
public class UserDetailsImpl implements UserDetails {

  private final Long id;
  private final Long accountId;
  private final String username;
  private final String password;
  private final boolean enabled;
  private final Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImpl(Long id, Long accountId, String username, String password,
      boolean enabled, Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.accountId = accountId;
    this.username = username;
    this.password = password;
    this.enabled = enabled;
    this.authorities = authorities;
  }

  public static UserDetailsImpl build(User user) {
    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority(role.getStringName()))
        .toList();
    return new UserDetailsImpl(user.getId(), user.getAccountId(), user.getEmailAddress(),
        user.getPasswordHash().value(), user.isActive(), authorities);
  }

  public Long getId() {
    return id;
  }

  public Long getAccountId() {
    return accountId;
  }

  public boolean hasRole(String role) {
    return authorities.stream().anyMatch(authority -> authority.getAuthority().equals(role));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
