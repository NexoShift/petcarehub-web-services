package com.nexoshift.petcarehub.aiassistance.domain.model.entities;

//import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalLlmContext;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.springframework.data.domain.Auditable;

public class SoapDraft extends AuditableModel {

    /**
    private static final String WARNING_SEPARATOR = "\n";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinical_assistance_id")
    private ClinicalAssistance clinicalAssistance;
    */
}
