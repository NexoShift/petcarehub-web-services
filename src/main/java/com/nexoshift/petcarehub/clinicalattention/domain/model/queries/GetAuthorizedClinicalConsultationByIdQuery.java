package com.nexoshift.petcarehub.clinicalattention.domain.model.queries;

/**
 * NUEVO. Lectura de una consulta desde la API REST, con control de acceso:
 * la ve el dueño de la mascota, un veterinario de la clínica de la consulta o un veterinario
 * de una clínica donde la mascota es paciente.
 * <p>
 * GetClinicalConsultationByIdQuery (sin requester) se queda solo para uso interno
 * (ClinicalAttentionContextFacade), porque otros BCs no tienen un usuario que validar.
 */
public record GetAuthorizedClinicalConsultationByIdQuery(Long consultationId,
                                                         Long requesterUserId) {

  public GetAuthorizedClinicalConsultationByIdQuery {
    if (consultationId == null || consultationId <= 0) {
      throw new IllegalArgumentException("Consultation id must be greater than zero");
    }
    if (requesterUserId == null || requesterUserId <= 0) {
      throw new IllegalArgumentException("Requester user id must be greater than zero");
    }
  }
}
