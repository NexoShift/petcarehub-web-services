package com.nexoshift.petcarehub.clinicalexams.interfaces.rest.controllers;

import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ClinicalExamNotFoundException;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.*;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.GetClinicalExamByIdQuery;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.GetClinicalExamsOfMyClinicQuery;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.GetClinicalFileContentQuery;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ExamStatus;
import com.nexoshift.petcarehub.clinicalexams.domain.services.ClinicalExamCommandService;
import com.nexoshift.petcarehub.clinicalexams.domain.services.ClinicalExamQueryService;
import com.nexoshift.petcarehub.clinicalexams.interfaces.rest.resources.*;
import com.nexoshift.petcarehub.clinicalexams.interfaces.rest.transform.ClinicalExamResourceFromEntityAssembler;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.List;

/**
 * Clinical exams. Writing requires ROLE_VETERINARIAN; reading is also open to the owner of the
 * pet. The fine-grained rule (same clinic, own pet) is decided by the application services.
 */
@RestController
@RequestMapping(value = "/api/v1/clinical-exams", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('VETERINARIAN')")
@Tag(name = "Clinical Exams", description = "Exams requested in a consultation, results and files")
public class ClinicalExamsController {

    private final ClinicalExamCommandService commandService;
    private final ClinicalExamQueryService queryService;

    public ClinicalExamsController(ClinicalExamCommandService commandService,
                                   ClinicalExamQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Request an exam in a consultation IN_PROGRESS (status REQUESTED)")
    public ResponseEntity<ClinicalExamResource> requestClinicalExam(
            @Valid @RequestBody RequestClinicalExamResource resource,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var exam = commandService.handle(new RequestClinicalExamCommand(
                resource.consultationId(), currentUser.getId(), resource.type(),
                resource.indication()));
        return ResponseEntity.created(URI.create("/api/v1/clinical-exams/" + exam.getId()))
                .body(ClinicalExamResourceFromEntityAssembler.toResourceFromEntity(exam));
    }

    @GetMapping
    @Operation(summary = "Exams of the clinic of the authenticated veterinarian, newest first; "
            + "optional status filter (REQUESTED, IN_PROCESS, COMPLETED, CANCELLED)")
    public ResponseEntity<List<ClinicalExamResource>> getClinicalExamsOfMyClinic(
            @RequestParam(required = false) ExamStatus status,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var exams = queryService.handle(
                new GetClinicalExamsOfMyClinicQuery(currentUser.getId(), status));
        return ResponseEntity.ok(exams.stream()
                .map(ClinicalExamResourceFromEntityAssembler::toResourceFromEntity).toList());
    }

    @GetMapping("/{examId}")
    @PreAuthorize("hasAnyRole('VETERINARIAN', 'PET_OWNER')")
    @Operation(summary = "Get an exam (veterinarian of the clinic or owner of the pet)")
    public ResponseEntity<ClinicalExamResource> getClinicalExamById(
            @PathVariable Long examId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var exam = queryService.handle(new GetClinicalExamByIdQuery(examId, currentUser.getId()))
                .orElseThrow(() -> new ClinicalExamNotFoundException(examId));
        return ResponseEntity.ok(ClinicalExamResourceFromEntityAssembler.toResourceFromEntity(
                exam, ClinicalExamResourceFromEntityAssembler.isVeterinarian(currentUser)));
    }

    @PutMapping("/{examId}/start")
    @Operation(summary = "Start the exam (REQUESTED to IN_PROCESS)")
    public ResponseEntity<ClinicalExamResource> startClinicalExam(
            @PathVariable Long examId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ok(commandService.handle(new StartClinicalExamCommand(examId, currentUser.getId())));
    }

    @PostMapping(value = "/{examId}/result", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register the result (once, while IN_PROCESS)")
    public ResponseEntity<ClinicalExamResource> registerExamResult(
            @PathVariable Long examId, @Valid @RequestBody RegisterExamResultResource resource,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var exam = commandService.handle(new RegisterExamResultCommand(examId, currentUser.getId(),
                resource.summary(), resource.values(), resource.interpretation(),
                resource.resultDate()));
        return ResponseEntity.status(201)
                .body(ClinicalExamResourceFromEntityAssembler.toResourceFromEntity(exam));
    }

    @PutMapping("/{examId}/result/validate")
    @Operation(summary = "Validate the result; the validator is the authenticated veterinarian")
    public ResponseEntity<ClinicalExamResource> validateExamResult(
            @PathVariable Long examId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ok(commandService.handle(new ValidateExamResultCommand(examId, currentUser.getId())));
    }

    @PutMapping("/{examId}/complete")
    @Operation(summary = "Complete the exam (needs a validated result)")
    public ResponseEntity<ClinicalExamResource> completeClinicalExam(
            @PathVariable Long examId, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ok(commandService.handle(new CompleteClinicalExamCommand(examId, currentUser.getId())));
    }

    @PutMapping(value = "/{examId}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel a REQUESTED or IN_PROCESS exam with a reason")
    public ResponseEntity<ClinicalExamResource> cancelClinicalExam(
            @PathVariable Long examId, @Valid @RequestBody CancelClinicalExamResource resource,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ok(commandService.handle(
                new CancelClinicalExamCommand(examId, currentUser.getId(), resource.reason())));
    }

    @PostMapping(value = "/{examId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Attach an image, video or PDF (max 10 MB) to a started exam")
    public ResponseEntity<ClinicalFileResource> uploadClinicalFile(
            @PathVariable Long examId, @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var clinicalFile = commandService.handle(new UploadClinicalFileCommand(examId,
                currentUser.getId(), file.getOriginalFilename(), file.getContentType(),
                bytesOf(file)));
        return ResponseEntity.status(201)
                .body(ClinicalExamResourceFromEntityAssembler.toResourceFromEntity(clinicalFile));
    }

    @GetMapping(value = "/{examId}/files/{fileId}", produces = MediaType.ALL_VALUE)
    @PreAuthorize("hasAnyRole('VETERINARIAN', 'PET_OWNER')")
    @Operation(summary = "Download a file of the exam (veterinarian of the clinic or owner)")
    public ResponseEntity<byte[]> downloadClinicalFile(
            @PathVariable Long examId, @PathVariable Long fileId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        var content = queryService.handle(
                new GetClinicalFileContentQuery(examId, fileId, currentUser.getId()));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(content.fileName(), java.nio.charset.StandardCharsets.UTF_8)
                        .build().toString())
                .body(content.content());
    }

    private static ResponseEntity<ClinicalExamResource> ok(
            com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam exam) {
        return ResponseEntity.ok(ClinicalExamResourceFromEntityAssembler.toResourceFromEntity(exam));
    }

    private static byte[] bytesOf(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new UncheckedIOException("The file could not be read", exception);
        }
    }
}
