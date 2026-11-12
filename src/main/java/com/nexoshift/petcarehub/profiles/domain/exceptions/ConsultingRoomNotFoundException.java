package com.nexoshift.petcarehub.profiles.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class ConsultingRoomNotFoundException extends NotFoundException {

    public ConsultingRoomNotFoundException(Long consultingRoomId) {
        super("Consulting room with ID " + consultingRoomId + " was not found.");
    }
}
