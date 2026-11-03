package com.nexoshift.petcarehub.iam.domain.model.valueobjects;

public record HolderProfile(String firstName, String lastName, String phoneNumber,
                            String clinicName, String ruc) {
}