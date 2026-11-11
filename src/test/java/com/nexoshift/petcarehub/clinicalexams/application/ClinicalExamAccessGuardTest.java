package com.nexoshift.petcarehub.clinicalexams.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexoshift.petcarehub.clinicalexams.application.internal.access.ClinicalExamAccessGuard;
import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ClinicalExamAccessDeniedException;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ConsultationNotFoundException;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.VeterinarianNotRegisteredException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Reglas de acceso sin Spring, con fakes en memoria.
 * Usuario 10 = vet 100 de la clínica 1; usuario 20 = vet 200 de la clínica 2;
 * usuario 30 = dueño de la mascota 7. Consulta 5 = mascota 7 en la clínica 1.
 */
class ClinicalExamAccessGuardTest {

  private final Map<Long, Long> vetByUser = new HashMap<>(Map.of(10L, 100L, 20L, 200L));
  private final Map<Long, Long> clinicByVet = new HashMap<>(Map.of(100L, 1L, 200L, 2L));

  private final ExternalProfilesService profiles = new ExternalProfilesService() {
    public Optional<Long> fetchVeterinarianIdByUserId(Long userId) {
      return Optional.ofNullable(vetByUser.get(userId));
    }

    public Optional<Long> fetchClinicIdByVeterinarianId(Long veterinarianId) {
      return Optional.ofNullable(clinicByVet.get(veterinarianId));
    }

    public Optional<Long> fetchOwnerUserIdByPetId(Long petId) {
      return petId == 7L ? Optional.of(30L) : Optional.empty();
    }

    public boolean isPetPatientOfClinic(Long clinicId, Long petId) {
      return clinicId == 1L && petId == 7L;
    }
  };

  private final ExternalClinicalAttentionService clinicalAttention =
      new ExternalClinicalAttentionService() {
        public boolean isConsultationInProgress(Long consultationId) {
          return consultationId == 5L;
        }

        public Optional<Long> fetchPetIdByConsultationId(Long consultationId) {
          return consultationId == 5L ? Optional.of(7L) : Optional.empty();
        }

        public Optional<Long> fetchClinicIdByConsultationId(Long consultationId) {
          return consultationId == 5L ? Optional.of(1L) : Optional.empty();
        }
      };

  private final ClinicalExamAccessGuard guard =
      new ClinicalExamAccessGuard(clinicalAttention, profiles);

  @Test
  void onlyVeterinariansOfTheConsultationClinicCanWrite() {
    assertThat(guard.requireVeterinarianOfConsultation(10L, 5L).veterinarianId()).isEqualTo(100L);
    assertThatThrownBy(() -> guard.requireVeterinarianOfConsultation(20L, 5L))
        .isInstanceOf(ClinicalExamAccessDeniedException.class);
    assertThatThrownBy(() -> guard.requireVeterinarianOfConsultation(30L, 5L))
        .isInstanceOf(VeterinarianNotRegisteredException.class);
    assertThatThrownBy(() -> guard.requireVeterinarianOfConsultation(10L, 99L))
        .isInstanceOf(ConsultationNotFoundException.class);
  }

  @Test
  void aVeterinarianWithoutClinicCannotWrite() {
    vetByUser.put(40L, 400L);

    assertThatThrownBy(() -> guard.requireVeterinarianOfConsultation(40L, 5L))
        .isInstanceOf(ClinicalExamAccessDeniedException.class);
  }

  @Test
  void ownerAndClinicVeterinarianCanReadTheConsultation() {
    assertThatCode(() -> guard.requireCanReadConsultation(30L, 5L)).doesNotThrowAnyException();
    assertThatCode(() -> guard.requireCanReadConsultation(10L, 5L)).doesNotThrowAnyException();
    assertThatThrownBy(() -> guard.requireCanReadConsultation(20L, 5L))
        .isInstanceOf(ClinicalExamAccessDeniedException.class);
    assertThatThrownBy(() -> guard.requireCanReadConsultation(99L, 5L))
        .isInstanceOf(ClinicalExamAccessDeniedException.class);
    assertThatThrownBy(() -> guard.requireCanReadConsultation(30L, 99L))
        .isInstanceOf(ConsultationNotFoundException.class);
  }

  private static com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam examOfClinic1() {
    return new com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam(
        new com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicalConsultationId(5L),
        new com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicId(1L),
        new com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.PetId(7L),
        new com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.VeterinarianId(100L),
        com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ExamType.OTHER, null);
  }

  @Test
  void theExamBelongsToItsClinic() {
    var exam = examOfClinic1();

    assertThat(guard.requireCanWrite(10L, exam).veterinarianId()).isEqualTo(100L);
    assertThatThrownBy(() -> guard.requireCanWrite(20L, exam))
        .isInstanceOf(ClinicalExamAccessDeniedException.class);
    assertThatThrownBy(() -> guard.requireCanWrite(30L, exam))
        .isInstanceOf(VeterinarianNotRegisteredException.class);
    assertThatCode(() -> guard.requireCanRead(30L, exam)).doesNotThrowAnyException();
    assertThatCode(() -> guard.requireCanRead(10L, exam)).doesNotThrowAnyException();
    assertThatThrownBy(() -> guard.requireCanRead(20L, exam))
        .isInstanceOf(ClinicalExamAccessDeniedException.class);
  }

  @Test
  void clinicOfTheVeterinarian() {
    assertThat(guard.requireVeterinarianClinic(20L)).isEqualTo(2L);
    assertThatThrownBy(() -> guard.requireVeterinarianClinic(30L))
        .isInstanceOf(VeterinarianNotRegisteredException.class);
  }

  @Test
  void petExamsForOwnerOrVeterinarianOfAClinicWhereItIsPatient() {
    assertThatCode(() -> guard.requireCanReadPet(30L, 7L)).doesNotThrowAnyException();
    assertThatCode(() -> guard.requireCanReadPet(10L, 7L)).doesNotThrowAnyException();
    assertThatThrownBy(() -> guard.requireCanReadPet(20L, 7L))
        .isInstanceOf(ClinicalExamAccessDeniedException.class);
    assertThatThrownBy(() -> guard.requireCanReadPet(99L, 7L))
        .isInstanceOf(ClinicalExamAccessDeniedException.class);
  }
}
