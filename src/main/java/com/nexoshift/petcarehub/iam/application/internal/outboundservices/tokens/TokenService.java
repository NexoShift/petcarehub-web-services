package com.nexoshift.petcarehub.iam.application.internal.outboundservices.tokens;

/** Port: access tokens. Implemented with JWT in infrastructure. */
public interface TokenService {

  String generateToken(String username);

  String getUsernameFromToken(String token);

  boolean validateToken(String token);
}
