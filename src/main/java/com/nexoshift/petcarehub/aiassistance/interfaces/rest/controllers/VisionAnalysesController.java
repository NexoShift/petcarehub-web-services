package com.nexoshift.petcarehub.aiassistance.interfaces.rest.controllers;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.VisionAnalysisNotFoundException;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestVisionAnalysisCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetVisionAnalysesByConsultationIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetVisionAnalysesByPetIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetVisionAnalysisByIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.RequesterRole;
import com.nexoshift.petcarehub.aiassistance.domain.services.VisionAnalysisCommandService;
import com.nexoshift.petcarehub.aiassistance.domain.services.VisionAnalysisQueryService;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.VisionAnalysisResource;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform.VisionAnalysisResourceFromEntityAssembler;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Veterinarians upload SKIN or RADIOGRAPH images and see the analyses of any pet.
 * Pet owners upload only SKIN images and only see the analyses of their own pets.
 */
@RestController
@RequestMapping(value = "/api/v1/ai/vision-analyses", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "AI Vision Analysis", description = "Skin photos and radiographs analyzed with computer vision")
@PreAuthorize("hasAnyRole('VETERINARIAN', 'PET_OWNER')")
public class VisionAnalysesController {

  private static final String VETERINARIAN = "ROLE_VETERINARIAN";

  private final VisionAnalysisCommandService commandService;
  private final VisionAnalysisQueryService queryService;
  private final FileStorageService fileStorageService;
  private final ExternalProfilesService profilesService;

  public VisionAnalysesController(VisionAnalysisCommandService commandService,
      VisionAnalysisQueryService queryService, FileStorageService fileStorageService,
      ExternalProfilesService profilesService) {
    this.commandService = commandService;
    this.queryService = queryService;
    this.fileStorageService = fileStorageService;
    this.profilesService = profilesService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload an image (SKIN or RADIOGRAPH) and analyze it")
  public ResponseEntity<VisionAnalysisResource> requestVisionAnalysis(
      @RequestPart("file") MultipartFile file,
      @RequestParam Long petId,
      @RequestParam ImageType imageType,
      @RequestParam(required = false) Long consultationId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) throws IOException {
    var command = new RequestVisionAnalysisCommand(consultationId, petId, currentUser.getId(),
        roleOf(currentUser), imageType, file.getBytes(), file.getContentType());
    var analysis = commandService.handle(command);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(VisionAnalysisResourceFromEntityAssembler.toResourceFromEntity(analysis));
  }

  @GetMapping("/{visionAnalysisId}")
  @Operation(summary = "Get a vision analysis with its findings")
  public VisionAnalysisResource getVisionAnalysisById(@PathVariable Long visionAnalysisId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return VisionAnalysisResourceFromEntityAssembler.toResourceFromEntity(
        loadVisible(visionAnalysisId, currentUser));
  }

  @GetMapping(params = "petId")
  @Operation(summary = "List the vision analyses of a pet, newest first")
  public List<VisionAnalysisResource> getVisionAnalysesByPetId(@RequestParam Long petId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    ensureCanSeePet(petId, currentUser);
    return queryService.handle(new GetVisionAnalysesByPetIdQuery(petId)).stream()
        .map(VisionAnalysisResourceFromEntityAssembler::toResourceFromEntity)
        .toList();
  }

  @GetMapping(params = "consultationId")
  @PreAuthorize("hasRole('VETERINARIAN')")
  @Operation(summary = "List the vision analyses of a consultation, newest first")
  public List<VisionAnalysisResource> getVisionAnalysesByConsultationId(
      @RequestParam Long consultationId) {
    return queryService.handle(new GetVisionAnalysesByConsultationIdQuery(consultationId))
        .stream()
        .map(VisionAnalysisResourceFromEntityAssembler::toResourceFromEntity)
        .toList();
  }

  @GetMapping(value = "/{visionAnalysisId}/image",
      produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
  @Operation(summary = "Download the analyzed image (request it as a blob with the token)")
  public ResponseEntity<byte[]> getImage(@PathVariable Long visionAnalysisId,
      @AuthenticationPrincipal UserDetailsImpl currentUser) {
    var image = loadVisible(visionAnalysisId, currentUser).getImage();
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(image.contentType()))
        .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
        .body(fileStorageService.load(image.storageKey()));
  }

  private VisionAnalysis loadVisible(Long visionAnalysisId, UserDetailsImpl currentUser) {
    var analysis = queryService.handle(new GetVisionAnalysisByIdQuery(visionAnalysisId))
        .orElseThrow(() -> new VisionAnalysisNotFoundException(visionAnalysisId));
    ensureCanSeePet(analysis.getPetId(), currentUser);
    return analysis;
  }

  private void ensureCanSeePet(Long petId, UserDetailsImpl currentUser) {
    if (roleOf(currentUser) == RequesterRole.PET_OWNER
        && !profilesService.isPetOwnedBy(petId, currentUser.getId())) {
      throw new ForbiddenException("You can only see the analyses of your own pets");
    }
  }

  /** A CLINIC holder has both CLINIC_ADMIN and VETERINARIAN: veterinarian wins. */
  private static RequesterRole roleOf(UserDetailsImpl currentUser) {
    return currentUser.hasRole(VETERINARIAN) ? RequesterRole.VETERINARIAN : RequesterRole.PET_OWNER;
  }
}
