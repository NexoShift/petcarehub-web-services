package com.nexoshift.petcarehub.profiles.interfaces.rest.controllers;

import com.nexoshift.petcarehub.profiles.domain.model.commands.*;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetClinicByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetConsultingRoomByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetConsultingRoomsByClinicIdQuery;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicCommandService;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicQueryService;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.ConsultingRoomResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.CreateConsultingRoomResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.UpdateConsultingRoomResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.ConsultingRoomResourceFromEntityAssembler;
import com.nexoshift.petcarehub.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing consulting rooms belonging to clinics.
 */
@RestController
@RequestMapping(
        value = "/api/v1/clinics/{clinicId}/consulting-rooms",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Clinic Consulting Rooms", description = "Available Clinic Consulting Room Endpoints")
public class ClinicConsultingRoomsController {

    private final ClinicCommandService clinicCommandService;
    private final ClinicQueryService clinicQueryService;

    /**
     * Constructs the clinic consulting rooms controller.
     *
     * @param clinicCommandService the clinic command service
     * @param clinicQueryService the clinic query service
     */
    public ClinicConsultingRoomsController(
            ClinicCommandService clinicCommandService,
            ClinicQueryService clinicQueryService) {

        this.clinicCommandService = clinicCommandService;
        this.clinicQueryService = clinicQueryService;
    }

    /**
     * Creates a consulting room within a clinic.
     *
     * @param clinicId the clinic identifier
     * @param resource the created consulting room resource
     * @return the created consulting room resource
     */
    @PostMapping
    @Operation(summary = "Create consulting room", description = "Create a consulting room for a clinic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consulting room created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    public ResponseEntity<ConsultingRoomResource> createConsultingRoom(
            @PathVariable Long clinicId,
            @RequestBody CreateConsultingRoomResource resource) {

        var command = new CreateConsultingRoomCommand(
                clinicId,
                resource.name(),
                resource.description()
        );

        var consultingRoomId = clinicCommandService.handle(command);



        var getConsultingRoomByIdQuery = new GetConsultingRoomByIdQuery(clinicId, consultingRoomId);

        var consultingRoom = clinicQueryService.handle(getConsultingRoomByIdQuery);

        if (consultingRoom.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var consultingRoomResource = ConsultingRoomResourceFromEntityAssembler
                .toResourceFromEntity(consultingRoom.get());

        return new ResponseEntity<>(consultingRoomResource, HttpStatus.CREATED);
    }

    /**
     * Retrieves all consulting rooms belonging to a clinic.
     *
     * @param clinicId the clinic identifier
     * @return the consulting room resources
     */
    @GetMapping
    @Operation(summary = "Get consulting rooms", description = "Get consulting rooms by clinic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulting rooms retrieved"),
            @ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    public ResponseEntity<List<ConsultingRoomResource>> getConsultingRoomsByClinicId(
            @PathVariable Long clinicId) {

        var clinic = clinicQueryService.handle(new GetClinicByIdQuery(clinicId));

        if (clinic.isEmpty()) return ResponseEntity.notFound().build();

        var rooms = clinicQueryService.handle(
                new GetConsultingRoomsByClinicIdQuery(clinicId)
        );

        var resources = rooms.stream()
                .map(ConsultingRoomResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves a consulting room by its identifier.
     *
     * @param clinicId the clinic identifier
     * @param consultingRoomId the consulting room identifier
     * @return the consulting room resource
     */
    @GetMapping("/{consultingRoomId}")
    @Operation(summary = "Get consulting room by ID", description = "Get a consulting room by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulting room found"),
            @ApiResponse(responseCode = "404", description = "Consulting room not found")
    })
    public ResponseEntity<ConsultingRoomResource> getConsultingRoomById(
            @PathVariable Long clinicId,
            @PathVariable Long consultingRoomId) {

        var room = clinicQueryService.handle(
                new GetConsultingRoomByIdQuery(clinicId, consultingRoomId)
        );

        if (room.isEmpty()) return ResponseEntity.notFound().build();

        var resource = ConsultingRoomResourceFromEntityAssembler
                .toResourceFromEntity(room.get());

        return ResponseEntity.ok(resource);
    }


    /**
     * Updates a consulting room.
     *
     * @param clinicId the clinic identifier
     * @param consultingRoomId the consulting room identifier
     * @param resource the update consulting room resource
     * @return the updated consulting room resource
     */
    @PutMapping("/{consultingRoomId}")
    @Operation(summary = "Update consulting room", description = "Update consulting room information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulting room updated"),
            @ApiResponse(responseCode = "404", description = "Consulting room not found")
    })
    public ResponseEntity<ConsultingRoomResource> updateConsultingRoom(
            @PathVariable Long clinicId,
            @PathVariable Long consultingRoomId,
            @RequestBody UpdateConsultingRoomResource resource) {

        var command = new UpdateConsultingRoomCommand(
                clinicId,
                consultingRoomId,
                resource.name(),
                resource.description()
        );

        clinicCommandService.handle(command);

        var room = clinicQueryService.handle(
                new GetConsultingRoomByIdQuery(clinicId, consultingRoomId)
        );

        if (room.isEmpty()) return ResponseEntity.notFound().build();

        var roomResource = ConsultingRoomResourceFromEntityAssembler
                .toResourceFromEntity(room.get());

        return ResponseEntity.ok(roomResource);
    }

    /**
     * Activates a consulting room.
     *
     * @param clinicId the clinic identifier
     * @param consultingRoomId the consulting room identifier
     * @return the updated consulting room resource
     */
    @PostMapping("/{consultingRoomId}/activate")
    @Operation(summary = "Activate consulting room", description = "Activate a consulting room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulting room activated"),
            @ApiResponse(responseCode = "404", description = "Consulting room not found")
    })
    public ResponseEntity<ConsultingRoomResource> activateConsultingRoom(
            @PathVariable Long clinicId,
            @PathVariable Long consultingRoomId) {

        clinicCommandService.handle(new ActivateConsultingRoomCommand(clinicId, consultingRoomId));

        var room = clinicQueryService.handle(new GetConsultingRoomByIdQuery(clinicId, consultingRoomId));

        if (room.isEmpty()) return ResponseEntity.notFound().build();

        var roomResource = ConsultingRoomResourceFromEntityAssembler.toResourceFromEntity(room.get());

        return ResponseEntity.ok(roomResource);
    }

    /**
     * Deactivates a consulting room.
     *
     * @param clinicId the clinic identifier
     * @param consultingRoomId the consulting room identifier
     * @return the updated consulting room resource
     */
    @PostMapping("/{consultingRoomId}/deactivate")
    @Operation(summary = "Deactivate consulting room", description = "Deactivate a consulting room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulting room deactivated"),
            @ApiResponse(responseCode = "409", description = "Consulting room has an active assignment")
    })
    public ResponseEntity<ConsultingRoomResource> deactivateConsultingRoom(
            @PathVariable Long clinicId,
            @PathVariable Long consultingRoomId) {

        clinicCommandService.handle(new DeactivateConsultingRoomCommand(clinicId, consultingRoomId));

        var room = clinicQueryService.handle(new GetConsultingRoomByIdQuery(clinicId, consultingRoomId));

        if (room.isEmpty()) return ResponseEntity.notFound().build();

        var roomResource = ConsultingRoomResourceFromEntityAssembler.toResourceFromEntity(room.get());

        return ResponseEntity.ok(roomResource);
    }


    /**
     * Deletes a consulting room without assignment history.
     *
     * @param clinicId the clinic identifier
     * @param consultingRoomId the consulting room identifier
     * @return an empty successful response
     */
    @DeleteMapping("/{consultingRoomId}")
    @Operation(summary = "Delete consulting room", description = "Delete a consulting room without assignment history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulting room deleted"),
            @ApiResponse(responseCode = "404", description = "Consulting room not found"),
            @ApiResponse(responseCode = "409", description = "Consulting room has assignment history")
    })
    public ResponseEntity<MessageResource> deleteConsultingRoom(
            @PathVariable Long clinicId,
            @PathVariable Long consultingRoomId) {

        clinicCommandService.handle(new DeleteConsultingRoomCommand(clinicId, consultingRoomId));

        return ResponseEntity.ok(new MessageResource("Consulting room deleted successfully"));
    }

}
