package com.nexoshift.petcarehub.iam.interfaces.rest.resources;

import java.util.List;

public record UserResource(
    Long id, Long accountId, String accountType, String email, String accessRole, String status,
    List<String> roles) {
}
