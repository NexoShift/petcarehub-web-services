package com.nexoshift.petcarehub.clinicalexams.domain.model.commands;

public record UploadClinicalFileCommand(
        Long examId, String fileName, String contentType, byte[] content) {

    public UploadClinicalFileCommand {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("File must not be empty");
        }
    }
}