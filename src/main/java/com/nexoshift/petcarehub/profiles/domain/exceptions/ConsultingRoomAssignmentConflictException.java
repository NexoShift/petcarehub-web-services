package com.nexoshift.petcarehub.profiles.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class ConsultingRoomAssignmentConflictException extends ConflictException {

    public ConsultingRoomAssignmentConflictException(String message){
        super(message);
    }
}
