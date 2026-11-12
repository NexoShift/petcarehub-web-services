package com.nexoshift.petcarehub.profiles.domain;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreatePetCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetCode;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetSex;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.Species;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Reglas del agregado Pet y de su código único (PetCode). */
class PetTest {

  private static CreatePetCommand command(String name, LocalDate birthDate) {
    return new CreatePetCommand(8L, name, Species.DOG, "Mestizo", birthDate, PetSex.MALE, null);
  }

  @Test
  @DisplayName("A new pet keeps its data, trims the name and gets a pet code")
  void createsPet() {
    var pet = new Pet(command("  Spot ", LocalDate.of(2023, 9, 15)));

    assertThat(pet.getName()).isEqualTo("Spot");
    assertThat(pet.getOwnerId().ownerId()).isEqualTo(8L);
    assertThat(pet.getSpecies()).isEqualTo(Species.DOG);
    assertThat(pet.getPetCode().value()).matches("PET-[A-HJ-NP-Z2-9]{8}");
  }

  @Test
  @DisplayName("Each pet gets a different code")
  void codesAreUnique() {
    var codes = new HashSet<String>();
    for (int i = 0; i < 200; i++) {
      codes.add(new Pet(command("Spot", null)).getPetCode().value());
    }
    assertThat(codes).hasSize(200);
  }

  @Test
  @DisplayName("The command rejects a missing owner, name or species and a future birth date")
  void commandValidates() {
    assertThatThrownBy(() -> new CreatePetCommand(null, "Spot", Species.DOG, null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> command(" ", null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CreatePetCommand(8L, "Spot", null, null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> command("Spot", LocalDate.now().plusDays(1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Updating the profile applies the same rules")
  void updatesProfile() {
    var pet = new Pet(command("Spot", null));

    pet.updateProfile(" Rocky ", Species.CAT, "Siamés", LocalDate.of(2022, 1, 1), PetSex.FEMALE);

    assertThat(pet.getName()).isEqualTo("Rocky");
    assertThat(pet.getSpecies()).isEqualTo(Species.CAT);
    assertThatThrownBy(() -> pet.updateProfile("", Species.CAT, null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> pet.updateProfile("Rocky", null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> pet.updateProfile("Rocky", Species.CAT, null,
        LocalDate.now().plusDays(1), null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("A pet code must have the format PET- plus 8 letters or digits without 0, 1, I or O")
  void petCodeFormat() {
    assertThat(new PetCode("PET-ABCD2345").value()).isEqualTo("PET-ABCD2345");
    assertThatThrownBy(() -> new PetCode("PET-ABCD234")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PetCode("PET-ABCD2340")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PetCode("pet-ABCD2345")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PetCode(null)).isInstanceOf(IllegalArgumentException.class);
  }
}
