package com.nexoshift.petcarehub.profiles.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class ProfileAlreadyExistsException extends ConflictException {

    public ProfileAlreadyExistsException(String profileType, Long referenceId){
        super(profileType + "profile with reference ID " + referenceId + " already exists.");
    }
}
