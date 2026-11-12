package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterMedicationCommand;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterMedicationResource;

public final class RegisterMedicationCommandFromResourceAssembler {
    private RegisterMedicationCommandFromResourceAssembler() {
    }

    public static RegisterMedicationCommand toCommandFromResource(
        RegisterMedicationResource resource
    ) {
        return new RegisterMedicationCommand(resource.name(),resource.dosage(),
                resource.frequency(), resource.duration(), resource.indication());
    }
}
