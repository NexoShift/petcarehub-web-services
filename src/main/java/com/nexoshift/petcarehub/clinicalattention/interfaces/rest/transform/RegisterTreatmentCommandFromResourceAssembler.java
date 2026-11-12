package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterTreatmentCommand;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterTreatmentResource;

public final class RegisterTreatmentCommandFromResourceAssembler {
    private RegisterTreatmentCommandFromResourceAssembler (){
    }

    public static RegisterTreatmentCommand toCommandFromResource(
            Long consultationId, Long requesterUserId, RegisterTreatmentResource resource
    ){
        return new RegisterTreatmentCommand(consultationId,requesterUserId, resource.description(),
                resource.instruction(), resource.startDateTime(),resource.endDateTime());
    }
}
