package com.nexoshift.petcarehub.clinicalattention.interfaces.acl;

import java.time.LocalDate;

/**
 * Published, read-only summary of a consultation for other contexts (for example the AI
 * backgrounds). Only simple types and short texts; never the aggregate itself.
 *
 * @param findings    chief complaint and clinical findings, or null
 * @param diagnosis   current primary diagnosis, or null
 * @param treatment   treatments and prescribed medications, or null
 */
public record ConsultationSummary(Long consultationId, Long petId, Long clinicId,
                                  String status, LocalDate date, String reason, String findings,
                                  String diagnosis, String treatment) {
}
