package com.nexoshift.petcarehub.iam.interfaces.rest.transform;

import com.nexoshift.petcarehub.iam.domain.model.commands.SignUpCommand;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.HolderProfile;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.SignUpResource;

public final class SignUpCommandFromResourceAssembler {

  private SignUpCommandFromResourceAssembler() {
  }

  public static SignUpCommand toCommandFromResource(SignUpResource resource) {
    return new SignUpCommand(resource.email(), resource.password(), resource.accountType(),
            new HolderProfile(resource.firstName(), resource.lastName(), resource.phoneNumber(),
                    resource.clinicName(), resource.ruc()));
  }
}
