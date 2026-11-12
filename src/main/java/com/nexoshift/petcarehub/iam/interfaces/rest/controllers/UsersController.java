package com.nexoshift.petcarehub.iam.interfaces.rest.controllers;

import com.nexoshift.petcarehub.iam.domain.exceptions.UserNotFoundException;
import com.nexoshift.petcarehub.iam.domain.model.commands.ActivateUserCommand;
import com.nexoshift.petcarehub.iam.domain.model.commands.DeactivateUserCommand;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetUserByIdQuery;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.Roles;
import com.nexoshift.petcarehub.iam.domain.services.AccountCommandService;
import com.nexoshift.petcarehub.iam.domain.services.AccountQueryService;
import com.nexoshift.petcarehub.iam.domain.services.PasswordCommandService;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.ChangePasswordResource;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.UserResource;
import com.nexoshift.petcarehub.iam.interfaces.rest.transform.ChangePasswordCommandFromResourceAssembler;
import com.nexoshift.petcarehub.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User management")
public class UsersController {

  private final AccountQueryService accountQueryService;
  private final AccountCommandService accountCommandService;
  private final PasswordCommandService passwordCommandService;

  public UsersController(AccountQueryService accountQueryService,
      AccountCommandService accountCommandService,
      PasswordCommandService passwordCommandService) {
    this.accountQueryService = accountQueryService;
    this.accountCommandService = accountCommandService;
    this.passwordCommandService = passwordCommandService;
  }

  @GetMapping("/me")
  @Operation(summary = "Get the authenticated user")
  public ResponseEntity<UserResource> getCurrentUser(
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return getUserById(currentUser.getId(), currentUser);
  }

  @GetMapping("/{userId}")
  @Operation(summary = "Get a user: the user itself or the clinic admin of its account")
  public ResponseEntity<UserResource> getUserById(@PathVariable Long userId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    var user = accountQueryService.handle(new GetUserByIdQuery(userId))
        .orElseThrow(() -> new UserNotFoundException(userId));
    boolean isSelf = user.getId().equals(currentUser.getId());
    boolean isAdminOfAccount = currentUser.hasRole(Roles.ROLE_CLINIC_ADMIN.name())
        && user.belongsTo(currentUser.getAccountId());
    if (!isSelf && !isAdminOfAccount) {
      throw new ForbiddenException("You can only see users of your own account");
    }
    return ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(user));
  }

  @PutMapping("/{userId}/password")
  @Operation(summary = "Change the password of the authenticated user")
  public ResponseEntity<MessageResource> changePassword(@PathVariable Long userId,
      @Valid @RequestBody ChangePasswordResource resource,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    if (!userId.equals(currentUser.getId())) {
      throw new ForbiddenException("You can only change your own password");
    }
    passwordCommandService.handle(
        ChangePasswordCommandFromResourceAssembler.toCommandFromResource(userId, resource));
    return ResponseEntity.ok(new MessageResource("Password updated successfully"));
  }

  @PutMapping("/{userId}/activate")
  @PreAuthorize("hasRole('CLINIC_ADMIN')")
  @Operation(summary = "Activate a member of the clinic account")
  public ResponseEntity<UserResource> activateUser(@PathVariable Long userId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return accountCommandService.handle(new ActivateUserCommand(userId, currentUser.getId()))
        .map(user -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(user)))
        .orElseGet(() -> ResponseEntity.badRequest().build());
  }

  @PutMapping("/{userId}/deactivate")
  @PreAuthorize("hasRole('CLINIC_ADMIN')")
  @Operation(summary = "Deactivate a member of the clinic account")
  public ResponseEntity<UserResource> deactivateUser(@PathVariable Long userId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return accountCommandService.handle(new DeactivateUserCommand(userId, currentUser.getId()))
        .map(user -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(user)))
        .orElseGet(() -> ResponseEntity.badRequest().build());
  }
}
