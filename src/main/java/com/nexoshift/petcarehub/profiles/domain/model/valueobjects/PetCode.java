package com.nexoshift.petcarehub.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.security.SecureRandom;

@Embeddable
public record PetCode(String value) {
    private static final String CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Validates the pet code.
     *
     * @param value the pet code value
     */
    public PetCode {
        if (value == null || !value.matches("PET-[A-HJ-NP-Z2-9]{8}")) {
            throw new IllegalArgumentException("Invalid pet code");
        }
    }

    /**
     * Generates a random pet code.
     *
     * @return the generated pet code
     */
    public static PetCode generate() {
        var code = new StringBuilder("PET-");

        for (int i = 0; i < 8; i++) {
            code.append(CHARACTERS.charAt(
                    RANDOM.nextInt(CHARACTERS.length())
            ));
        }

        return new PetCode(code.toString());
    }
}

