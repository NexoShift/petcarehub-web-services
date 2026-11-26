package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.stub;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ConsultationBackground;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Test double with a demo clinical history (it is not a Spring bean).
 * Rocky (pet 1) has consultations 1 to 3; today's consultation is 4. Luna (pet 2) has 5.
 */
public class ClinicalAttentionServiceStub implements ExternalClinicalAttentionService {

  private static final List<ConsultationBackground> CONSULTATIONS = List.of(
      new ConsultationBackground(1L, 1L, 1L, LocalDate.of(2026, 3, 12),
          "Prurito en patas y abdomen",
          "Eritema interdigital, lamido de patas, pápulas en abdomen",
          "Dermatitis atópica canina (sospecha)",
          "Oclacitinib 14 días, champú de clorhexidina 2 %, dieta hidrolizada"),
      new ConsultationBackground(2L, 1L, 1L, LocalDate.of(2026, 6, 5),
          "Sacude la cabeza, mal olor en oído izquierdo",
          "Secreción ceruminosa marrón; citología con Malassezia +++",
          "Otitis externa por Malassezia",
          "Limpieza ótica y gotas de clotrimazol, gentamicina y betametasona por 10 días"),
      new ConsultationBackground(3L, 1L, 1L, LocalDate.of(2026, 8, 20),
          "Vómitos tras cambio de alimento",
          "Deshidratación leve, abdomen sin dolor",
          "Gastritis aguda por cambio de dieta",
          "Dieta blanda y maropitant"),
      new ConsultationBackground(5L, 2L, 1L, LocalDate.of(2026, 5, 2),
          "Vacunación anual", "Examen físico normal", "Paciente sano",
          "Vacuna triple felina"));

  @Override
  public Optional<ConsultationBackground> fetchConsultation(Long consultationId) {
    return CONSULTATIONS.stream()
        .filter(consultation -> consultation.consultationId().equals(consultationId))
        .findFirst();
  }

  @Override
  public List<ConsultationBackground> fetchConsultationsByPetId(Long petId) {
    return CONSULTATIONS.stream()
        .filter(consultation -> consultation.petId().equals(petId))
        .sorted(Comparator.comparing(ConsultationBackground::date).reversed())
        .toList();
  }

  @Override
  public Optional<Long> fetchPetIdByConsultationId(Long consultationId) {
    if (consultationId != null && consultationId == 4L) {
      return Optional.of(1L);
    }
    return fetchConsultation(consultationId).map(ConsultationBackground::petId);
  }

  @Override
  public boolean isConsultationInProgress(Long consultationId) {
    return consultationId != null && consultationId == 4L;
  }
}
