package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl;

import java.time.LocalDate;

/** A previous consultation of the pet, as this module sees it. {@code clinicId}: where it was. */
public record ConsultationBackground(Long consultationId, Long petId, Long clinicId, LocalDate date,
    String reason, String findings, String diagnosis, String treatment) {

  public String toPromptText() {
    return """
        Consulta del %s
        Motivo: %s
        Hallazgos: %s
        Diagnóstico: %s
        Tratamiento: %s""".formatted(orMissing(date), orMissing(reason), orMissing(findings),
        orMissing(diagnosis), orMissing(treatment));
  }

  private static String orMissing(Object value) {
    return value == null ? "no registrado" : value.toString();
  }
}
