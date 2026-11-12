package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPrescriptionCommand;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterPrescriptionResource;

public final class RegisterPresciptionCommandFromResourceAssembler {
    private RegisterPresciptionCommandFromResourceAssembler(){

    }

    public static RegisterPrescriptionCommand toCommandFromResource(
            Long consultationId, Long requesterUserId, RegisterPrescriptionResource resource
    ){
        var medicationCommands = resource.medications().stream()
                .map(RegisterMedicationCommandFromResourceAssembler::toCommandFromResource)
                .toList();

        return new RegisterPrescriptionCommand(
                consultationId, requesterUserId, resource.dateAndTimeOfIssuance(),
                resource.generalInstruction(), medicationCommands);
    }
}
