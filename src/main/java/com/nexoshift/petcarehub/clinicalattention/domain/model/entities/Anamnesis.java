package com.nexoshift.petcarehub.clinicalattention.domain.model.entities;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Initial information given by the owner: reason for the visit, evolution, symptoms, diet,
 * behavior and treatments applied at home. A consultation has only one anamnesis and it is not
 * modified once registered (CON-01).
 */
@Entity
@Table(name = "anamneses")
public class Anamnesis extends AuditableModel {

  @Column(nullable = false, length = ClinicalFieldLimits.CHIEF_COMPLAINT)
  private String chiefComplaint;

  @Column(nullable = false, length = ClinicalFieldLimits.EVOLUTION_TIME)
  private String evolutionTime;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "anamnesis_symptoms", joinColumns = @JoinColumn(name = "anamnesis_id"))
  @Column(name = "symptom", length = ClinicalFieldLimits.SYMPTOM)
  @OrderColumn(name = "symptom_order")
  @Fetch(FetchMode.SELECT)
  private List<String> symptoms = new ArrayList<>();

  @Column(length = ClinicalFieldLimits.DIET)
  private String diet;

  @Column(length = ClinicalFieldLimits.BEHAVIOR)
  private String behavior;

  @Column(length = ClinicalFieldLimits.PREVIOUS_TREATMENTS)
  private String previousTreatments;

  @Column(length = ClinicalFieldLimits.OWNER_OBSERVATIONS)
  private String ownerObservations;

  protected Anamnesis() {
  }

  public Anamnesis(String chiefComplaint, String evolutionTime, List<String> symptoms,
      String diet, String behavior, String previousTreatments, String ownerObservations) {
    if (chiefComplaint == null || chiefComplaint.isBlank()) {
      throw new IllegalArgumentException("Chief complaint is required");
    }
    if (evolutionTime == null || evolutionTime.isBlank()) {
      throw new IllegalArgumentException("Evolution time is required");
    }
    this.chiefComplaint = chiefComplaint;
    this.evolutionTime = evolutionTime;
    if (symptoms != null) {
      this.symptoms.addAll(symptoms);
    }
    this.diet = diet;
    this.behavior = behavior;
    this.previousTreatments = previousTreatments;
    this.ownerObservations = ownerObservations;
  }

  public String getChiefComplaint() {
    return chiefComplaint;
  }

  public String getEvolutionTime() {
    return evolutionTime;
  }

  public List<String> getSymptoms() {
    return List.copyOf(symptoms);
  }

  public String getDiet() {
    return diet;
  }

  public String getBehavior() {
    return behavior;
  }

  public String getPreviousTreatments() {
    return previousTreatments;
  }

  public String getOwnerObservations() {
    return ownerObservations;
  }
}
