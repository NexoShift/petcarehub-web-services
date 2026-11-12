package com.nexoshift.petcarehub.iam.infrastructure.tokens.jwt.services;

import com.nexoshift.petcarehub.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** JWT (HS256) implementation of the token port. */
@Service
public class TokenServiceImpl implements BearerTokenService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final int MIN_SECRET_BYTES = 32;

  private final SecretKey signingKey;
  private final Duration expiration;

  public TokenServiceImpl(
      @Value("${authorization.jwt.secret}") String secret,
      @Value("${authorization.jwt.expiration.days:7}") long expirationDays) {
    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (secretBytes.length < MIN_SECRET_BYTES) {
      throw new IllegalStateException("JWT secret must have at least 32 bytes");
    }
    this.signingKey = Keys.hmacShaKeyFor(secretBytes);
    this.expiration = Duration.ofDays(expirationDays);
  }

  @Override
  public String generateToken(String username) {
    var issuedAt = Instant.now();
    return Jwts.builder()
        .subject(username)
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(issuedAt.plus(expiration)))
        .signWith(signingKey)
        .compact();
  }

  @Override
  public String getUsernameFromToken(String token) {
    return Jwts.parser().verifyWith(signingKey).build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  @Override
  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException exception) {
      LOGGER.debug("Invalid JWT: {}", exception.getMessage());
      return false;
    }
  }

  @Override
  public String getBearerTokenFrom(HttpServletRequest request) {
    String header = request.getHeader(AUTHORIZATION_HEADER);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
