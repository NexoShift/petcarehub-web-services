package com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects;

public enum ClinicalFileType {
    IMAGE,
    VIDEO,
    DOCUMENT;

    public static ClinicalFileType fromContentType(String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return IMAGE;
        }
        if (contentType != null && contentType.startsWith("video/")) {
            return VIDEO;
        }
        if ("application/pdf".equals(contentType)) {
            return DOCUMENT;
        }
        throw new IllegalArgumentException("Only images, videos and PDF documents are allowed");
    }
}