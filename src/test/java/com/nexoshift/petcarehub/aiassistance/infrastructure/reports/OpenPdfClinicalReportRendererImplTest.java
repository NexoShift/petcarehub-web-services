package com.nexoshift.petcarehub.aiassistance.infrastructure.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.reports.ClinicalReportData;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class OpenPdfClinicalReportRendererImplTest {

  @Test
  void rendersAPdfDocument() {
    var pdf = new OpenPdfClinicalReportRendererImpl().render(new ClinicalReportData(4L,
        "Perro, raza Bulldog francés", "Rascado intenso en orejas", LocalDate.of(2026, 11, 1),
        "S: prurito\nO: eritema\nA: atopia\nP: citología", false, "gpt-5.4-mini"));

    assertTrue(pdf.length > 500);
    assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
  }
}
