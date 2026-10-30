package com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects;

public record ClinicalFileContent(String fileName, String contentType, byte[] content) {
}