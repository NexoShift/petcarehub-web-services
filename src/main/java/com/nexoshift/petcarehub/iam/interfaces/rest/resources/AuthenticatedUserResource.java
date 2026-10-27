package com.nexoshift.petcarehub.iam.interfaces.rest.resources;

import java.util.List;

public record AuthenticatedUserResource(
    Long userId, Long accountId, String accountType, String email, List<String> roles,
    String token) {
}
