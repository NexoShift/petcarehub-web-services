package com.nexoshift.petcarehub.profiles.interfaces.rest.controllers;

import com.nexoshift.petcarehub.profiles.domain.model.commands.AssignVeterinarianCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UnassignVeterinarianCommand;
import com.nexoshift.petcarehub.profiles.domain.model.queries.*;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicCommandService;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicQueryService;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.AssignVeterinarianResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.ConsultingRoomAssignmentResource;
import com.nexoshift.petcarehub.profiles.interfaces.rest.transform.ConsultingRoomAssignmentResourceFromEntityAssembler;
import com.nexoshift.petcarehub.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing veterinarian assignments to consulting rooms.
 */
@RestController
@RequestMapping(value = "/api/v1/clinics/{clinicId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clinic Consulting Room Assignments", description = "Available Consulting Room Assignment Endpoints")
public class ClinicConsultingRoomAssignmentsController {

    private final ClinicCommandService clinicCommandService;
    private final ClinicQueryService clinicQueryService;

    /**
     * Constructs the clinic consulting room assignments controller.
     *
     * @param clinicCommandService the clinic command service
     * @param clinicQueryService the clinic query service
     */
    public ClinicConsultingRoomAssignmentsController(
            ClinicCommandService clinicCommandService,
            ClinicQueryService clinicQueryService) {

        this.clinicCommandService = clinicCommandService;
        this.clinicQueryService = clinicQueryService;
    }

    /**
     * Assigns a veterinarian to a consulting room.
     *
     * @param clinicId the clinic identifier
     * @param resource the assignment information
     * @return a confirmation message
     */
    @PostMapping("/consulting-room-assignments")
    @Operation(
            summary = "Assign veterinarian",
            description = "Assign a veterinarian to a consulting room"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veterinarian assigned"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Clinic, consulting room, or veterinarian not found"),
            @ApiResponse(responseCode = "409", description = "Assignment conflict")
    })
    public ResponseEntity<MessageResource> assignVeterinarian(
            @PathVariable Long clinicId,
            @RequestBody AssignVeterinarianResource resource) {

        var assignVeterinarianCommand = new AssignVeterinarianCommand(
                clinicId,
                resource.consultingRoomId(),
                resource.veterinarianId()
        );

        clinicCommandService.handle(assignVeterinarianCommand);

        return ResponseEntity.ok(new MessageResource("Veterinarian assigned successfully"));
    }

    /**
     * Retrieves all consulting room assignments belonging to a clinic.
     *
     * @param clinicId the clinic identifier
     * @return the list of consulting room assignment resources
     */
    @GetMapping("/consulting-room-assignments")
    @Operation(
            summary = "Get clinic consulting room assignments",
            description = "Retrieve all consulting room assignments for a clinic"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignments retrieved"),
            @ApiResponse(responseCode = "404", description = "Clinic not found")
    })
    public ResponseEntity<List<ConsultingRoomAssignmentResource>> getAssignmentsByClinicId(
            @PathVariable Long clinicId) {

        var getClinicByIdQuery = new GetClinicByIdQuery(clinicId);
        var clinic = clinicQueryService.handle(getClinicByIdQuery);

        if (clinic.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var getConsultingRoomAssignmentsByClinicIdQuery =
                new GetConsultingRoomAssignmentsByClinicIdQuery(clinicId);

        var assignments = clinicQueryService.handle(
                getConsultingRoomAssignmentsByClinicIdQuery
        );

        var assignmentResources = assignments.stream()
                .map(ConsultingRoomAssignmentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(assignmentResources);
    }


    /**
     * Retrieves all assignments for a consulting room.
     *
     * @param clinicId the clinic identifier
     * @param consultingRoomId the consulting room identifier
     * @return the list of consulting room assignment resources
     */
    @GetMapping("/consulting-rooms/{consultingRoomId}/assignments")
    @Operation(
            summary = "Get assignments by consulting room",
            description = "Retrieve the assignment history of a consulting room"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignments retrieved"),
            @ApiResponse(responseCode = "404", description = "Consulting room not found")
    })
    public ResponseEntity<List<ConsultingRoomAssignmentResource>> getAssignmentsByConsultingRoomId(
            @PathVariable Long clinicId,
            @PathVariable Long consultingRoomId) {

        var getConsultingRoomByIdQuery = new GetConsultingRoomByIdQuery(
                clinicId,
                consultingRoomId
        );

        var consultingRoom = clinicQueryService.handle(
                getConsultingRoomByIdQuery
        );

        if (consultingRoom.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var getAssignmentsByConsultingRoomIdQuery =
                new GetAssignmentsByConsultingRoomIdQuery(
                        clinicId,
                        consultingRoomId
                );

        var assignments = clinicQueryService.handle(getAssignmentsByConsultingRoomIdQuery);

        var assignmentResources = assignments.stream()
                .map(ConsultingRoomAssignmentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(assignmentResources);
    }


    /**
     * Retrieves the active assignment for a consulting room.
     *
     * @param clinicId the clinic identifier
     * @param consultingRoomId the consulting room identifier
     * @return the active consulting room assignment resource
     */
    @GetMapping("/consulting-rooms/{consultingRoomId}/assignments/active")
    @Operation(
            summary = "Get active consulting room assignment",
            description = "Retrieve the active veterinarian assignment for a consulting room"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active assignment found"),
            @ApiResponse(responseCode = "404", description = "Active assignment not found")
    })
    public ResponseEntity<ConsultingRoomAssignmentResource> getActiveAssignment(
            @PathVariable Long clinicId,
            @PathVariable Long consultingRoomId) {

        var getActiveAssignmentByConsultingRoomIdQuery =
                new GetActiveAssignmentByConsultingRoomIdQuery(
                        clinicId,
                        consultingRoomId
                );

        var assignment = clinicQueryService.handle(
                getActiveAssignmentByConsultingRoomIdQuery
        );

        if (assignment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var assignmentResource = ConsultingRoomAssignmentResourceFromEntityAssembler
                .toResourceFromEntity(assignment.get());

        return ResponseEntity.ok(assignmentResource);
    }

    /**
     * Ends an active veterinarian assignment.
     *
     * @param clinicId the clinic identifier
     * @param assignmentId the assignment identifier
     * @return a confirmation message
     */
    @PostMapping("/consulting-room-assignments/{assignmentId}/unassignments")
    @Operation(
            summary = "Unassign veterinarian",
            description = "End an active veterinarian assignment while preserving its history"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veterinarian unassigned"),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
            @ApiResponse(responseCode = "409", description = "Assignment already inactive")
    })
    public ResponseEntity<MessageResource> unassignVeterinarian(
            @PathVariable Long clinicId,
            @PathVariable Long assignmentId) {

        var unassignVeterinarianCommand = new UnassignVeterinarianCommand(
                clinicId,
                assignmentId
        );

        clinicCommandService.handle(unassignVeterinarianCommand);

        return ResponseEntity.ok(
                new MessageResource("Veterinarian unassigned successfully")
        );
    }


}
