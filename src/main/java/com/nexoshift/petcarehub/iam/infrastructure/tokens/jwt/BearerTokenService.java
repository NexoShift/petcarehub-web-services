package com.nexoshift.petcarehub.iam.infrastructure.tokens.jwt;

import com.nexoshift.petcarehub.iam.application.internal.outboundservices.tokens.TokenService;
import jakarta.servlet.http.HttpServletRequest;

/** TokenService that also knows how to read the token from the Authorization header. */
public interface BearerTokenService extends TokenService {

  String getBearerTokenFrom(HttpServletRequest request);
}
