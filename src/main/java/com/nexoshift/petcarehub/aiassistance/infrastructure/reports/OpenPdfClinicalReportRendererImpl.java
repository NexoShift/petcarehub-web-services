package com.nexoshift.petcarehub.aiassistance.infrastructure.reports;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.reports.ClinicalReportData;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.reports.ClinicalReportRenderer;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

/** Confirmed SOAP report as an A4 PDF, with OpenPDF 2.0.x. */
@Service
public class OpenPdfClinicalReportRendererImpl implements ClinicalReportRenderer {

  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
  private static final Font HEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
  private static final Font BODY = FontFactory.getFont(FontFactory.HELVETICA, 11);
  private static final Font NOTE = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9);

  @Override
  public byte[] render(ClinicalReportData data) {
    var out = new ByteArrayOutputStream();
    var document = new Document(PageSize.A4, 50, 50, 50, 50);
    PdfWriter.getInstance(document, out);
    document.open();
    document.add(new Paragraph("Informe clínico – PetCare Hub", TITLE));
    document.add(new Paragraph(
        "Consulta N.º " + data.consultationId() + " · " + DATE.format(data.date()), BODY));
    document.add(new Paragraph(" "));
    document.add(new Paragraph("Paciente", HEADING));
    document.add(new Paragraph(data.patientSummary(), BODY));
    document.add(new Paragraph("Motivo de consulta", HEADING));
    document.add(new Paragraph(data.currentReason(), BODY));
    document.add(new Paragraph("Nota SOAP", HEADING));
    document.add(new Paragraph(data.finalText(), BODY));
    document.add(new Paragraph(" "));
    document.add(new Paragraph((data.edited()
        ? "Borrador generado con IA (" + data.model() + ") y editado por el médico veterinario."
        : "Borrador generado con IA (" + data.model() + ") y aceptado por el médico veterinario.")
        + " La decisión clínica es responsabilidad del veterinario.", NOTE));
    document.close();
    return out.toByteArray();
  }
}
