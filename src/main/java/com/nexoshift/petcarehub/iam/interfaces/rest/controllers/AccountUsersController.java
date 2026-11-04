package com.nexoshift.petcarehub.iam.interfaces.rest.controllers;

import com.nexoshift.petcarehub.iam.domain.exceptions.AccountOperationNotAllowedException;
import com.nexoshift.petcarehub.iam.domain.exceptions.UserNotFoundException;
import com.nexoshift.petcarehub.iam.domain.model.commands.CreateMemberUserCommand;
import com.nexoshift.petcarehub.iam.domain.model.queries.GetUserByIdQuery;
import com.nexoshift.petcarehub.iam.domain.services.AccountCommandService;
import com.nexoshift.petcarehub.iam.domain.services.AccountQueryService;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.CreateMemberUserResource;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.UserResource;
import com.nexoshift.petcarehub.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing users within an account.
 */
@RestController
@RequestMapping(value = "/api/v1/accounts/{accountId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Account", description = "Management of users within clinic accounts")
public class AccountUsersController {

    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;

    public AccountUsersController(
            AccountCommandService accountCommandService,
            AccountQueryService accountQueryService
    ) {
        this.accountCommandService = accountCommandService;
        this.accountQueryService = accountQueryService;
    }

    /**
     * Creates a veterinarian member in an existing clinic account.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    @Operation(
            summary = "Create a veterinarian member user in a clinic account"
    )
    public ResponseEntity<UserResource> createMember(
            @PathVariable Long accountId,
            @Valid @RequestBody CreateMemberUserResource resource,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {

        var requester = accountQueryService.handle(
                new GetUserByIdQuery(currentUser.getId())
        ).orElseThrow(
                () -> new UserNotFoundException(currentUser.getId())
        );

        if (
                !requester.isActive()
                        || !requester.isHolder()
                        || !requester.belongsTo(accountId)
        ) {
            throw new AccountOperationNotAllowedException(
                    "Only the active account holder can create member users"
            );
        }

        var command = new CreateMemberUserCommand(
                accountId,
                resource.email(),
                resource.password()
        );

        return accountCommandService.handle(command)
                .map(user -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(
                                UserResourceFromEntityAssembler.toResourceFromEntity(user)
                        )
                )
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}