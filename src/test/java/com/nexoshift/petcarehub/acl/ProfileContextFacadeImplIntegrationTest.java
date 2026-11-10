package com.nexoshift.petcarehub.acl;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.ClinicPatient;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Veterinarian;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateClinicPatientCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.AccountId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicPatientRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.VeterinarianRepository;
import com.nexoshift.petcarehub.profiles.interfaces.acl.ProfileContextFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba la fachada que Profiles publica a los otros BCs (Clinical Attention y AI Assistance)
 * contra la base H2 real: así se valida también el mapeo JPA de los value objects.
 * Cada test corre en una transacción que se deshace al final (no deja datos).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProfileContextFacadeImplIntegrationTest {

  @Autowired private ProfileContextFacade facade;
  @Autowired private VeterinarianRepository veterinarianRepository;
  @Autowired private ClinicRepository clinicRepository;
  @Autowired private ClinicPatientRepository clinicPatientRepository;

  @Test
  void findsTheVeterinarianProfileOfAnIamUser() {
    var vet = veterinarianRepository.save(
        new Veterinarian(new UserId(501L), new ClinicId(7L), "ana@demo.com"));

    assertThat(facade.fetchVeterinarianIdByUserId(501L)).isEqualTo(vet.getId());
    assertThat(facade.fetchClinicIdByVeterinarianId(vet.getId())).isEqualTo(7L);
  }

  @Test
  void returnsZeroWhenTheUserHasNoVeterinarianProfile() {
    assertThat(facade.fetchVeterinarianIdByUserId(999_999L)).isZero();
    assertThat(facade.fetchVeterinarianIdByUserId(null)).isZero();
    assertThat(facade.fetchVeterinarianIdByUserId(-1L)).isZero();
  }

  @Test
  void knowsWhichPetsArePatientsOfAClinic() {
    clinicPatientRepository.save(new ClinicPatient(new CreateClinicPatientCommand(3L, 20L)));

    assertThat(facade.isPetPatientOfClinic(3L, 20L)).isTrue();
    assertThat(facade.isPetPatientOfClinic(4L, 20L)).isFalse();   // otra clínica
    assertThat(facade.isPetPatientOfClinic(3L, 21L)).isFalse();   // otra mascota
    assertThat(facade.isPetPatientOfClinic(null, 20L)).isFalse();
  }

  @Test
  void onlyActiveRoomsOfTheSameClinicCount() {
    var clinic = new Clinic(new AccountId(77L));
    clinic.addConsultingRoom("Consultorio 1", "General");
    clinic.addConsultingRoom("Consultorio 2", "Cirugía");
    clinic = clinicRepository.saveAndFlush(clinic);
    var active = clinic.getConsultingRooms().get(0).getId();
    var inactive = clinic.getConsultingRooms().get(1).getId();
    clinic.deactivateConsultingRoom(inactive);
    clinicRepository.saveAndFlush(clinic);

    assertThat(facade.existsActiveConsultingRoom(clinic.getId(), active)).isTrue();
    assertThat(facade.existsActiveConsultingRoom(clinic.getId(), inactive)).isFalse();
    assertThat(facade.existsActiveConsultingRoom(clinic.getId() + 1000, active)).isFalse();
  }
}
