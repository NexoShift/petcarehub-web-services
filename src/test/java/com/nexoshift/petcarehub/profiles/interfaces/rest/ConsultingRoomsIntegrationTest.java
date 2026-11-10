package com.nexoshift.petcarehub.profiles.interfaces.rest;

import com.nexoshift.petcarehub.support.PetCareApiClient;
import com.nexoshift.petcarehub.support.PetCareApiClient.ClinicSetup;
import com.nexoshift.petcarehub.support.PetCareApiClient.Reply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API de consultorios y asignación de veterinarios.
 * "clinic" ya trae "Consultorio 1" activo y su vet titular; "otherClinic" es ajena.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class ConsultingRoomsIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  private PetCareApiClient api;
  private ClinicSetup clinic;
  private ClinicSetup otherClinic;
  private String rooms;
  private String assignments;

  @BeforeEach
  void setUp() throws Exception {
    api = new PetCareApiClient(MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity()).build());
    clinic = api.clinicWithVeterinarian();
    otherClinic = api.clinicWithVeterinarian();
    rooms = "/api/v1/clinics/" + clinic.clinicId() + "/consulting-rooms";
    assignments = "/api/v1/clinics/" + clinic.clinicId() + "/consulting-room-assignments";
  }

  private String token() {
    return clinic.holder().token();
  }

  private Reply assign(long roomId, long veterinarianId) throws Exception {
    return api.send(HttpMethod.POST, assignments, token(),
        "{\"consultingRoomId\": %d, \"veterinarianId\": %d}".formatted(roomId, veterinarianId));
  }

  private long newRoom(String name) throws Exception {
    var reply = api.send(HttpMethod.POST, rooms, token(),
        "{\"name\": \"" + name + "\", \"description\": \"\"}");
    assertThat(reply.status()).as(reply.body()).isEqualTo(201);
    return reply.number("id");
  }

  @Test
  @DisplayName("The clinic creates, lists, reads and renames its rooms")
  void roomCrud() throws Exception {
    var id = newRoom("Cirugía");

    var list = api.send(HttpMethod.GET, rooms, token(), null);
    assertThat(list.status()).isEqualTo(200);
    assertThat(list.body()).contains("\"name\":\"Consultorio 1\"").contains("\"name\":\"Cirugía\"");
    assertThat(api.send(HttpMethod.GET, rooms + "/" + id, token(), null).status()).isEqualTo(200);

    var renamed = api.send(HttpMethod.PUT, rooms + "/" + id, token(),
        "{\"name\": \"Quirófano\", \"description\": \"Piso 2\"}");
    assertThat(renamed.status()).isEqualTo(200);
    assertThat(renamed.body()).contains("\"name\":\"Quirófano\"");
  }

  @Test
  @DisplayName("Room names cannot repeat inside the clinic and cannot be blank")
  void roomNameRules() throws Exception {
    assertThat(api.send(HttpMethod.POST, rooms, token(), "{\"name\": \"consultorio 1\"}").status())
        .isEqualTo(400);
    assertThat(api.send(HttpMethod.POST, rooms, token(), "{\"name\": \" \"}").status())
        .isEqualTo(400);
  }

  @Test
  @DisplayName("Another clinic cannot see or change my rooms")
  void otherClinicForbidden() throws Exception {
    var other = otherClinic.holder().token();

    assertThat(api.send(HttpMethod.GET, rooms, other, null).status()).isEqualTo(403);
    assertThat(api.send(HttpMethod.POST, rooms, other, "{\"name\": \"Intruso\"}").status())
        .isEqualTo(403);
    assertThat(api.send(HttpMethod.POST, assignments, other,
        "{\"consultingRoomId\": %d, \"veterinarianId\": %d}"
            .formatted(clinic.consultingRoomId(), otherClinic.veterinarianId())).status())
        .isEqualTo(403);
  }

  @Test
  @DisplayName("A room is deactivated and activated; an unknown room is 404")
  void activation() throws Exception {
    var id = newRoom("Rayos X");

    var off = api.send(HttpMethod.POST, rooms + "/" + id + "/deactivate", token(), null);
    assertThat(off.status()).isEqualTo(200);
    assertThat(off.body()).contains("\"active\":false");
    var on = api.send(HttpMethod.POST, rooms + "/" + id + "/activate", token(), null);
    assertThat(on.body()).contains("\"active\":true");
    assertThat(api.send(HttpMethod.GET, rooms + "/999999", token(), null).status()).isEqualTo(404);
  }

  @Test
  @DisplayName("A room without history is deleted")
  void deletesRoom() throws Exception {
    var id = newRoom("Temporal");

    assertThat(api.send(HttpMethod.DELETE, rooms + "/" + id, token(), null).status())
        .isEqualTo(200);
    assertThat(api.send(HttpMethod.GET, rooms + "/" + id, token(), null).status()).isEqualTo(404);
  }

  @Test
  @DisplayName("Assignment flow: assign, no deactivate while assigned, unassign, no delete with history")
  void assignmentFlow() throws Exception {
    var room = clinic.consultingRoomId();

    assertThat(assign(room, clinic.veterinarianId()).status()).isBetween(200, 201);
    var active = api.send(HttpMethod.GET, rooms + "/" + room + "/assignments/active", token(), null);
    assertThat(active.status()).isEqualTo(200);
    assertThat(active.number("veterinarianId")).isEqualTo(clinic.veterinarianId());

    assertThat(api.send(HttpMethod.POST, rooms + "/" + room + "/deactivate", token(), null)
        .status()).isEqualTo(409);

    var unassign = api.send(HttpMethod.POST,
        assignments + "/" + active.number("id") + "/unassignments", token(), null);
    assertThat(unassign.status()).isEqualTo(200);
    assertThat(api.send(HttpMethod.POST, rooms + "/" + room + "/deactivate", token(), null)
        .status()).isEqualTo(200);
    assertThat(api.send(HttpMethod.DELETE, rooms + "/" + room, token(), null).status())
        .isEqualTo(409);

    var history = api.send(HttpMethod.GET, rooms + "/" + room + "/assignments", token(), null);
    assertThat(history.body()).contains("\"active\":false");
  }

  @Test
  @DisplayName("Assigning another vet replaces the current one; the clinic list shows both records")
  void replacesVeterinarian() throws Exception {
    var room = clinic.consultingRoomId();
    var member = api.addVeterinarian(clinic);

    assign(room, clinic.veterinarianId());
    assign(room, member.veterinarianId());

    var active = api.send(HttpMethod.GET, rooms + "/" + room + "/assignments/active", token(), null);
    assertThat(active.number("veterinarianId")).isEqualTo(member.veterinarianId());
    var all = api.send(HttpMethod.GET, assignments, token(), null);
    assertThat(all.status()).isEqualTo(200);
    assertThat(all.body().split("\"consultingRoomId\"", -1).length - 1).isEqualTo(2);
  }

  @Test
  @DisplayName("A vet of another clinic cannot be assigned to my room")
  void foreignVeterinarian() throws Exception {
    var reply = assign(clinic.consultingRoomId(), otherClinic.veterinarianId());

    assertThat(reply.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Nobody can be assigned to an inactive room")
  void inactiveRoom() throws Exception {
    var id = newRoom("Cerrado");
    api.send(HttpMethod.POST, rooms + "/" + id + "/deactivate", token(), null);

    assertThat(assign(id, clinic.veterinarianId()).status()).isEqualTo(403);
  }
}
