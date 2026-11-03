package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl;

/** Anonymous pet data: no owner name, phone or e-mail ever reaches the LLM. */
public record PetSummary(Long petId, String name, String species, String breed, String sex,
    Integer ageInMonths, String color) {

  /** The name is not sent to the model; it is only used to show it on the web. */
  public String toPromptText() {
    return "%s, raza %s, %s, %s meses, pelaje %s".formatted(
        species, breed, sex, ageInMonths, color);
  }
}
