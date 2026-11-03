package com.nexoshift.petcarehub.clinicalexams.domain.services;

import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.CancelClinicalExamCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.CompleteClinicalExamCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.RegisterExamResultCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.RequestClinicalExamCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.StartClinicalExamCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.UploadClinicalFileCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.commands.ValidateExamResultCommand;
import com.nexoshift.petcarehub.clinicalexams.domain.model.entities.ClinicalFile;

public interface ClinicalExamCommandService {

    ClinicalExam handle(RequestClinicalExamCommand command);

    ClinicalExam handle(StartClinicalExamCommand command);

    ClinicalExam handle(RegisterExamResultCommand command);

    ClinicalExam handle(ValidateExamResultCommand command);

    ClinicalFile handle(UploadClinicalFileCommand command);

    ClinicalExam handle(CompleteClinicalExamCommand command);

    ClinicalExam handle(CancelClinicalExamCommand command);
}