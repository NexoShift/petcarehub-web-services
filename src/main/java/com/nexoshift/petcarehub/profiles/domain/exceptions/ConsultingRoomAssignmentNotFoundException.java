package com.nexoshift.petcarehub.profiles.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class ConsultingRoomAssignmentNotFoundException extends NotFoundException {
    public ConsultingRoomAssignmentNotFoundException(Long assignmentId) {
        super("Consulting room assignment with ID " + assignmentId + " was not found.");
    }
}
