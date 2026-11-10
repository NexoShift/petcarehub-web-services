package com.nexoshift.petcarehub.profiles.domain;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.ClinicPatient;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.PetOwner;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Veterinarian;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateClinicPatientCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreatePetOwnerCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateVeterinarianCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Ids, comandos y agregados pequeños de Profiles (PetOwner, Veterinarian, ClinicPatient). */
class ProfilesValueObjectsTest {

  @Test
  @DisplayName("All ids must be greater than zero")
  void idsMustBePositive() {
    assertThatThrownBy(() -> new UserId(0L)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new OwnerId(null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PetId(-1L)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ClinicId(0L)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new VeterinarianId(0L)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ConsultingRoomId(0L)).isInstanceOf(IllegalArgumentException.class);
    assertThat(new PetId(5L).petId()).isEqualTo(5L);
  }

  @Test
  @DisplayName("A pet owner needs the IAM user and an email (trimmed)")
  void petOwner() {
    assertThatThrownBy(() -> new CreatePetOwnerCommand(null, "Ares", "Torres", "a@b.com",
        null, null, null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CreatePetOwnerCommand(1L, "Ares", "Torres", " ",
        null, null, null)).isInstanceOf(IllegalArgumentException.class);

    var owner = new PetOwner(new CreatePetOwnerCommand(1L, "Ares", "Torres", " a@b.com ",
        "912345678", null, null));
    assertThat(owner.getUserId().userId()).isEqualTo(1L);
    assertThat(owner.getEmail()).isEqualTo("a@b.com");
  }

  @Test
  @DisplayName("A veterinarian needs the IAM user and the clinic")
  void veterinarian() {
    assertThatThrownBy(() -> new CreateVeterinarianCommand(null, 3L, "Ana", "López",
        "ana@b.com", null, null, "CMVP 1", null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CreateVeterinarianCommand(10L, null, "Ana", "López",
        "ana@b.com", null, null, "CMVP 1", null, null))
        .isInstanceOf(IllegalArgumentException.class);

    var vet = new Veterinarian(new CreateVeterinarianCommand(10L, 3L, "Ana", "López",
        "ana@b.com", null, null, "CMVP 1", "Dermatología", null));
    assertThat(vet.getUserId().userId()).isEqualTo(10L);
    assertThat(vet.getClinicId().clinicId()).isEqualTo(3L);
  }

  @Test
  @DisplayName("Registering a patient keeps the clinic, the pet and the date")
  void clinicPatient() {
    var patient = new ClinicPatient(new CreateClinicPatientCommand(3L, 20L));

    assertThat(patient.getClinicId().clinicId()).isEqualTo(3L);
    assertThat(patient.getPetId().petId()).isEqualTo(20L);
    assertThat(patient.getRegisteredAt()).isNotNull();
  }
}
