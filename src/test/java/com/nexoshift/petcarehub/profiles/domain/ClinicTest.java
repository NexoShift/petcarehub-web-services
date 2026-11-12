package com.nexoshift.petcarehub.profiles.domain;

import com.nexoshift.petcarehub.profiles.domain.exceptions.ConsultingRoomAssignmentConflictException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ConsultingRoomAssignmentNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ConsultingRoomNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateClinicCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.AccountId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.VeterinarianId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.nexoshift.petcarehub.profiles.support.ProfilesTestSupport.withId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reglas del agregado Clinic: datos de la clínica, consultorios y asignación de veterinarios.
 * Escenario: clínica 3 con el consultorio 11 ("Consultorio 1") y el 12 ("Cirugía").
 */
class ClinicTest {

  private static final long ROOM = 11L;
  private static final long OTHER_ROOM = 12L;
  private static final VeterinarianId ANA = new VeterinarianId(100L);
  private static final VeterinarianId LUIS = new VeterinarianId(101L);

  private Clinic clinic;

  @BeforeEach
  void setUp() {
    clinic = withId(new Clinic(new AccountId(77L)), 3L);
    withId(clinic.addConsultingRoom("Consultorio 1", "General"), ROOM);
    withId(clinic.addConsultingRoom("Cirugía", null), OTHER_ROOM);
  }

  /** Asigna y pone ids a las asignaciones nuevas (como haría JPA al guardar). */
  private void assign(long roomId, VeterinarianId vet) {
    clinic.assignVeterinarian(new ConsultingRoomId(roomId), vet);
    long next = 500L;
    for (var assignment : clinic.getAssignments()) {
      if (assignment.getId() == null) {
        withId(assignment, next);
      }
      next++;
    }
  }

  private long activeAssignmentId(long roomId) {
    return clinic.getAssignments().stream()
        .filter(a -> a.isActive() && a.getConsultingRoomId().consultingRoomId() == roomId)
        .findFirst().orElseThrow().getId();
  }

  // ------------------------------------------------------------------ datos

  @Test
  @DisplayName("The RUC must have exactly 11 digits (or be empty)")
  void validatesRuc() {
    assertThatThrownBy(() -> new CreateClinicCommand(77L, "Vet", "123", null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> clinic.updateProfile("Vet", "2012345678X", null, null, null))
        .isInstanceOf(IllegalArgumentException.class);

    clinic.updateProfile("Veterinaria San Martín", "20123456789", "Av. 1", "987654321", null);
    assertThat(clinic.getRuc()).isEqualTo("20123456789");
    clinic.updateProfile("Veterinaria San Martín", null, null, null, null);
    assertThat(clinic.getRuc()).isNull();
  }

  @Test
  @DisplayName("A clinic needs its IAM account")
  void needsAccount() {
    assertThatThrownBy(() -> new Clinic((AccountId) null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new AccountId(0L)).isInstanceOf(IllegalArgumentException.class);
  }

  // ------------------------------------------------------------ consultorios

  @Test
  @DisplayName("New consulting rooms start active and their names are trimmed")
  void addsRooms() {
    var room = clinic.addConsultingRoom("  Rayos X  ", null);

    assertThat(room.isActive()).isTrue();
    assertThat(room.getName()).isEqualTo("Rayos X");
    assertThat(clinic.getConsultingRooms()).hasSize(3);
  }

  @Test
  @DisplayName("Room names are required, at most 100 characters and unique ignoring case")
  void roomNames() {
    assertThatThrownBy(() -> clinic.addConsultingRoom(" ", null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> clinic.addConsultingRoom("x".repeat(101), null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> clinic.addConsultingRoom("consultorio 1", null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> clinic.updateConsultingRoom(OTHER_ROOM, "CONSULTORIO 1", null))
        .isInstanceOf(IllegalArgumentException.class);

    clinic.updateConsultingRoom(ROOM, "Consultorio 1", "Renovado");   // su propio nombre sí
    assertThat(clinic.getConsultingRooms().get(0).getDescription()).isEqualTo("Renovado");
  }

  @Test
  @DisplayName("The room list cannot be changed from outside the aggregate")
  void roomsAreReadOnly() {
    assertThatThrownBy(() -> clinic.getConsultingRooms().clear())
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> clinic.getAssignments().clear())
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @DisplayName("A room can be deactivated and activated again")
  void activatesAndDeactivates() {
    clinic.deactivateConsultingRoom(ROOM);
    assertThat(clinic.getConsultingRooms().get(0).isActive()).isFalse();

    clinic.activateConsultingRoom(ROOM);
    assertThat(clinic.getConsultingRooms().get(0).isActive()).isTrue();
  }

  @Test
  @DisplayName("An unknown room is a not found error")
  void unknownRoom() {
    assertThatThrownBy(() -> clinic.activateConsultingRoom(99L))
        .isInstanceOf(ConsultingRoomNotFoundException.class);
    assertThatThrownBy(() -> clinic.removeConsultingRoom(99L))
        .isInstanceOf(ConsultingRoomNotFoundException.class);
  }

  @Test
  @DisplayName("A room without assignment history can be removed")
  void removesRoom() {
    clinic.removeConsultingRoom(OTHER_ROOM);

    assertThat(clinic.getConsultingRooms()).extracting("name").containsExactly("Consultorio 1");
  }

  // ------------------------------------------------------------- asignaciones

  @Test
  @DisplayName("Assigning a veterinarian creates an active assignment")
  void assignsVeterinarian() {
    assign(ROOM, ANA);

    var assignment = clinic.getAssignments().get(0);
    assertThat(assignment.isActive()).isTrue();
    assertThat(assignment.getVeterinarianId()).isEqualTo(ANA);
    assertThat(assignment.getAssignedAt()).isNotNull();
  }

  @Test
  @DisplayName("Assigning the same veterinarian again does nothing")
  void sameVeterinarianTwice() {
    assign(ROOM, ANA);
    assign(ROOM, ANA);

    assertThat(clinic.getAssignments()).hasSize(1);
  }

  @Test
  @DisplayName("Assigning another veterinarian closes the previous assignment and keeps it as history")
  void replacesVeterinarian() {
    assign(ROOM, ANA);
    assign(ROOM, LUIS);

    assertThat(clinic.getAssignments()).hasSize(2);
    assertThat(clinic.getAssignments().get(0).isActive()).isFalse();
    assertThat(clinic.getAssignments().get(0).getUnassignedAt()).isNotNull();
    assertThat(clinic.getAssignments().get(1).getVeterinarianId()).isEqualTo(LUIS);
  }

  @Test
  @DisplayName("A veterinarian cannot be assigned to an inactive room")
  void inactiveRoomCannotBeAssigned() {
    clinic.deactivateConsultingRoom(ROOM);

    assertThatThrownBy(() -> assign(ROOM, ANA))
        .isInstanceOf(ProfileOperationNotAllowedException.class);
  }

  @Test
  @DisplayName("A room with an active assignment cannot be deactivated")
  void cannotDeactivateAssignedRoom() {
    assign(ROOM, ANA);

    assertThatThrownBy(() -> clinic.deactivateConsultingRoom(ROOM))
        .isInstanceOf(ConsultingRoomAssignmentConflictException.class);
  }

  @Test
  @DisplayName("After unassigning, the room can be deactivated but not removed (it has history)")
  void unassignThenDeactivate() {
    assign(ROOM, ANA);
    clinic.unassignVeterinarian(activeAssignmentId(ROOM));

    clinic.deactivateConsultingRoom(ROOM);
    assertThat(clinic.getConsultingRooms().get(0).isActive()).isFalse();
    assertThatThrownBy(() -> clinic.removeConsultingRoom(ROOM))
        .isInstanceOf(ConsultingRoomAssignmentConflictException.class);
  }

  @Test
  @DisplayName("Unassigning twice or an unknown assignment fails")
  void unassignErrors() {
    assign(ROOM, ANA);
    var id = activeAssignmentId(ROOM);
    clinic.unassignVeterinarian(id);

    assertThatThrownBy(() -> clinic.unassignVeterinarian(id))
        .isInstanceOf(ConsultingRoomAssignmentConflictException.class);
    assertThatThrownBy(() -> clinic.unassignVeterinarian(9999L))
        .isInstanceOf(ConsultingRoomAssignmentNotFoundException.class);
  }

  @Test
  @DisplayName("Each room has its own assignment")
  void assignmentsPerRoom() {
    assign(ROOM, ANA);
    assign(OTHER_ROOM, LUIS);

    assertThat(clinic.getAssignments()).hasSize(2);
    assertThat(clinic.getAssignments()).allMatch(a -> a.isActive());
  }
}
