package com.nexoshift.petcarehub.profiles.application.internal.eventhandlers;

import com.nexoshift.petcarehub.iam.interfaces.acl.events.AccountRegisteredIntegrationEvent;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateClinicCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreatePetOwnerCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateVeterinarianCommand;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicCommandService;
import com.nexoshift.petcarehub.profiles.domain.services.PetOwnerCommandService;
import com.nexoshift.petcarehub.profiles.domain.services.VeterinarianCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service("profilesAccountRegisteredEventHandler")
public class AccountRegisteredEventHandler {

    private final PetOwnerCommandService petOwnerCommandService;
    private final ClinicCommandService clinicCommandService;
    private final VeterinarianCommandService veterinarianCommandService;

    public AccountRegisteredEventHandler(PetOwnerCommandService petOwnerCommandService,
                                         ClinicCommandService clinicCommandService,
                                         VeterinarianCommandService veterinarianCommandService) {
        this.petOwnerCommandService = petOwnerCommandService;
        this.clinicCommandService = clinicCommandService;
        this.veterinarianCommandService = veterinarianCommandService;
    }

    @EventListener
    public void on(AccountRegisteredIntegrationEvent event) {
        switch (event.accountType()) {
            case "PET_OWNER" -> {
                var petOwnerId = petOwnerCommandService.handle(new CreatePetOwnerCommand(
                        event.holderUserId(), event.firstName(), event.lastName(),
                        event.email(), event.phoneNumber(), null, null));
                log.info("Profile PetOwner {} created for user {}", petOwnerId, event.holderUserId());
            }
            case "CLINIC" -> {
                var clinicId = clinicCommandService.handle(new CreateClinicCommand(
                        event.accountId(), event.clinicName(), event.ruc(), null,
                        event.phoneNumber(), null, null));
                var veterinarianId = veterinarianCommandService.handle(new CreateVeterinarianCommand(
                        event.holderUserId(), clinicId, event.firstName(), event.lastName(),
                        event.email(), event.phoneNumber(), null, null, null, null));
                log.info("Profile Clinic {} and Veterinarian {} created for account {}",
                        clinicId, veterinarianId, event.accountId());
            }
            default -> log.warn("Unknown account type {}", event.accountType());
        }
    }
}