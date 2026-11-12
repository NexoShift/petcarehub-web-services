package com.nexoshift.petcarehub.iam.application.internal.outboundservices.hashing;

/** Port: hashing of passwords. Implemented with BCrypt in infrastructure. */
public interface HashingService {

  String encode(CharSequence rawPassword);

  boolean matches(CharSequence rawPassword, String encodedPassword);
}
