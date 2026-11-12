package com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.pipeline;

import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.nexoshift.petcarehub.iam.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

/** Reads the Bearer token of each request and authenticates the user if it is valid. */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);

  private final BearerTokenService tokenService;
  private final UserDetailsService userDetailsService;

  public BearerAuthorizationRequestFilter(BearerTokenService tokenService,
      UserDetailsService userDetailsService) {
    this.tokenService = tokenService;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String token = tokenService.getBearerTokenFrom(request);
    if (token != null && tokenService.validateToken(token)) {
      try {
        var username = tokenService.getUsernameFromToken(token);
        var userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails.isEnabled()) {
          SecurityContextHolder.getContext().setAuthentication(
              UsernamePasswordAuthenticationTokenBuilder.build(userDetails, request));
        }
      } catch (UsernameNotFoundException exception) {
        LOGGER.debug("Token of a user that no longer exists");
      }
    }
    filterChain.doFilter(request, response);
  }
}
