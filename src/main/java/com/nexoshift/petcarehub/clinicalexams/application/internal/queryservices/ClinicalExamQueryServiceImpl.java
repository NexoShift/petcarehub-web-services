package com.nexoshift.petcarehub.clinicalexams.application.internal.queryservices;

import com.nexoshift.petcarehub.clinicalexams.application.internal.access.ClinicalExamAccessGuard;
import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.clinicalexams.domain.exceptions.ClinicalExamNotFoundException;
import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.*;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicalFileContent;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalexams.domain.repositories.ClinicalExamRepository;
import com.nexoshift.petcarehub.clinicalexams.domain.services.ClinicalExamQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Reads of Clinical Exams. Newest exams first. */
@Service
public class ClinicalExamQueryServiceImpl implements ClinicalExamQueryService {

    private static final Comparator<ClinicalExam> NEWEST_FIRST =
            Comparator.comparing(ClinicalExam::getRequestedAt).reversed();

    private final ClinicalExamRepository clinicalExamRepository;
    private final ClinicalExamAccessGuard accessGuard;
    private final FileStorageService fileStorageService;

    public ClinicalExamQueryServiceImpl(ClinicalExamRepository clinicalExamRepository,
                                        ClinicalExamAccessGuard accessGuard,
                                        FileStorageService fileStorageService) {
        this.clinicalExamRepository = clinicalExamRepository;
        this.accessGuard = accessGuard;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClinicalExam> handle(GetClinicalExamByIdQuery query) {
        var exam = clinicalExamRepository.findById(query.examId());
        exam.ifPresent(found -> accessGuard.requireCanRead(query.requesterUserId(), found));
        return exam;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicalExam> handle(GetClinicalExamsByConsultationIdQuery query) {
        accessGuard.requireCanReadConsultation(query.requesterUserId(), query.consultationId());
        return sorted(clinicalExamRepository.findAllByConsultationId(
                new ClinicalConsultationId(query.consultationId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicalExam> handle(GetClinicalExamsByPetIdQuery query) {
        accessGuard.requireCanReadPet(query.requesterUserId(), query.petId());
        return sorted(clinicalExamRepository.findAllByPetId(new PetId(query.petId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicalExam> handle(GetClinicalExamsByPetIdAndStatusQuery query) {
        accessGuard.requireCanReadPet(query.requesterUserId(), query.petId());
        return sorted(clinicalExamRepository.findAllByPetIdAndStatus(
                new PetId(query.petId()), query.status()));
    }

    /** Worklist of the clinic (it replaces the spreadsheet of pending exams). */
    @Override
    @Transactional(readOnly = true)
    public List<ClinicalExam> handle(GetClinicalExamsOfMyClinicQuery query) {
        var clinicId = new ClinicId(accessGuard.requireVeterinarianClinic(query.requesterUserId()));
        return sorted(query.status() == null
                ? clinicalExamRepository.findAllByClinicId(clinicId)
                : clinicalExamRepository.findAllByClinicIdAndStatus(clinicId, query.status()));
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicalFileContent handle(GetClinicalFileContentQuery query) {
        var exam = clinicalExamRepository.findById(query.examId())
                .orElseThrow(() -> new ClinicalExamNotFoundException(query.examId()));
        accessGuard.requireCanRead(query.requesterUserId(), exam);
        var file = exam.findFile(query.fileId());
        return new ClinicalFileContent(file.getFileName(), file.getContentType(),
                fileStorageService.download(file.getStorageLocation()));
    }

    private static List<ClinicalExam> sorted(List<ClinicalExam> exams) {
        return exams.stream().sorted(NEWEST_FIRST).toList();
    }
}
