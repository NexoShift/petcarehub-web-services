package com.nexoshift.petcarehub.iam.infrastructure.hashing.bcrypt;

import com.nexoshift.petcarehub.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

/** HashingService that Spring Security can also use as its PasswordEncoder. */
public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
