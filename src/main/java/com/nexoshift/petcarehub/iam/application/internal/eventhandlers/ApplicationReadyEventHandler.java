package com.nexoshift.petcarehub.iam.application.internal.eventhandlers;

import com.nexoshift.petcarehub.iam.domain.model.commands.SeedRolesCommand;
import com.nexoshift.petcarehub.iam.domain.services.RoleCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/** Seeds the authorization roles when the application starts. */
@Service("iamApplicationReadyEventHandler")
public class ApplicationReadyEventHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

  private final RoleCommandService roleCommandService;

  public ApplicationReadyEventHandler(RoleCommandService roleCommandService) {
    this.roleCommandService = roleCommandService;
  }

  @EventListener
  public void on(ApplicationReadyEvent event) {
    roleCommandService.handle(new SeedRolesCommand());
    LOGGER.info("IAM roles verified");
  }
}
