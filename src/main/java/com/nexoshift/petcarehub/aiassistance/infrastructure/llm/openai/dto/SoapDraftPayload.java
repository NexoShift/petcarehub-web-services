package com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** The JSON the model writes inside choices[0].message.content. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SoapDraftPayload(String subjective, String objective, String assessment,
    String plan, List<String> warnings) {
}
