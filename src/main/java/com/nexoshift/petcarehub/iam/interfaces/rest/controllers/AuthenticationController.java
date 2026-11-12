package com.nexoshift.petcarehub.iam.interfaces.rest.controllers;

import com.nexoshift.petcarehub.iam.domain.model.commands.RequestPasswordRecoveryCommand;
import com.nexoshift.petcarehub.iam.domain.services.AccountCommandService;
import com.nexoshift.petcarehub.iam.domain.services.PasswordCommandService;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.PasswordRecoveryResource;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.PasswordResetResource;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.SignInResource;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.SignUpResource;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.UserResource;
import com.nexoshift.petcarehub.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.nexoshift.petcarehub.iam.interfaces.rest.transform.ResetPasswordCommandFromResourceAssembler;
import com.nexoshift.petcarehub.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.nexoshift.petcarehub.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.nexoshift.petcarehub.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.nexoshift.petcarehub.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Sign-up, sign-in and password recovery")
@SecurityRequirements
public class AuthenticationController {

  private final AccountCommandService accountCommandService;
  private final PasswordCommandService passwordCommandService;

  public AuthenticationController(AccountCommandService accountCommandService,
      PasswordCommandService passwordCommandService) {
    this.accountCommandService = accountCommandService;
    this.passwordCommandService = passwordCommandService;
  }

  @PostMapping("/sign-up")
  @Operation(summary = "Register a pet owner or clinic account ")
  public ResponseEntity<UserResource> signUp(@Valid @RequestBody SignUpResource resource) {
    var command = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
    return accountCommandService.handle(command)
        .map(user -> ResponseEntity.status(HttpStatus.CREATED)
            .body(UserResourceFromEntityAssembler.toResourceFromEntity(user)))
        .orElseGet(() -> ResponseEntity.badRequest().build());
  }

  @PostMapping("/sign-in")
  @Operation(summary = "Sign in and get a JWT")
  public ResponseEntity<AuthenticatedUserResource> signIn(
      @Valid @RequestBody SignInResource resource) {
    var command = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
    return accountCommandService.handle(command)
        .map(session -> ResponseEntity.ok(
            AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(session)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
  }

  @PostMapping("/password-recovery")
  @Operation(summary = "Request a password recovery e-mail")
  public ResponseEntity<MessageResource> requestPasswordRecovery(
      @Valid @RequestBody PasswordRecoveryResource resource) {
    passwordCommandService.handle(new RequestPasswordRecoveryCommand(resource.email()));
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResource(
        "If the email is registered, you will receive a link to reset your password"));
  }

  @PostMapping("/password-reset")
  @Operation(summary = "Set a new password with the token received by e-mail")
  public ResponseEntity<MessageResource> resetPassword(
      @Valid @RequestBody PasswordResetResource resource) {
    passwordCommandService.handle(
        ResetPasswordCommandFromResourceAssembler.toCommandFromResource(resource));
    return ResponseEntity.ok(new MessageResource("Password updated successfully"));
  }
}
