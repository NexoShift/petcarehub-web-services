package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPrescriptionCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPrescriptionCommand.MedicationItem;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterPrescriptionResource;

public final class RegisterPrescriptionCommandFromResourceAssembler {

  private RegisterPrescriptionCommandFromResourceAssembler() {
  }

  public static RegisterPrescriptionCommand toCommandFromResource(
      Long consultationId, Long requesterUserId, RegisterPrescriptionResource resource) {
    var medications = resource.medications().stream()
        .map(item -> new MedicationItem(item.name(), item.dosage(), item.frequency(),
            item.duration(), item.indication()))
        .toList();
    return new RegisterPrescriptionCommand(consultationId, requesterUserId,
        resource.generalInstruction(), medications);
  }
}
