package com.nexoshift.petcarehub.aiassistance.domain.model.aggregates;

import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestOwnerExplanationCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

/**
 * One question of a pet owner and the plain-language answer of the LLM. It is created only
 * when the model answered: if the provider fails nothing is saved and the API answers 503.
 */
@Entity
public class OwnerExplanation extends AuditableAbstractAggregateRoot<OwnerExplanation> {

  @Embedded
  private PetId petId;

  @Embedded
  private ClinicalConsultationId consultationId;

  @Embedded
  private UserId ownerId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String question;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String answer;

  @Column(length = 60)
  private String model;

  protected OwnerExplanation() {
  }

  public OwnerExplanation(RequestOwnerExplanationCommand command, String answer, String model) {
    if (answer == null || answer.isBlank()) {
      throw new IllegalArgumentException("The answer is empty");
    }
    this.petId = new PetId(command.petId());
    this.consultationId = command.consultationId() == null
        ? null : new ClinicalConsultationId(command.consultationId());
    this.ownerId = new UserId(command.ownerUserId());
    this.question = command.question();
    this.answer = answer.trim();
    this.model = model;
  }

  public Long getPetId() {
    return petId.petId();
  }

  public Long getConsultationId() {
    return consultationId == null ? null : consultationId.consultationId();
  }

  public Long getOwnerId() {
    return ownerId.userId();
  }

  public String getQuestion() {
    return question;
  }

  public String getAnswer() {
    return answer;
  }

  public String getModel() {
    return model;
  }
}
