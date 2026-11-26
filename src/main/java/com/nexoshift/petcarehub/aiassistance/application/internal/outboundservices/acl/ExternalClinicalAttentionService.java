package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl;

import java.util.List;
import java.util.Optional;

/**
 * NUEVO. Puerto de AI Assistance hacia Clinical Attention.
 * Sirve para validar el consultationId que llega al subir una imagen: antes se guardaba
 * cualquier número sin comprobar que la consulta existiera o fuera de esa mascota.
 */
public interface ExternalClinicalAttentionService {

  /** Mascota de la consulta. Empty si la consulta no existe. */
  Optional<Long> fetchPetIdByConsultationId(Long consultationId);

  /** True si la consulta está IN_PROGRESS (solo ahí el vet registra datos). */
  boolean isConsultationInProgress(Long consultationId);

  /** Any consultation as an AI background. Empty if it does not exist. */
  Optional<ConsultationBackground> fetchConsultation(Long consultationId);

  /** Completed consultations of a pet, newest first. */
  List<ConsultationBackground> fetchConsultationsByPetId(Long petId);
}
