package com.nexoshift.petcarehub.aiassistance.infrastructure.llm.fake;

import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmAnswer;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmCompletion;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmPrompt;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Offline LLM for the automated tests only (ai.llm.provider=fake in the test profile), so
 * they never spend OpenAI credit. It is not part of the application.
 */
@Service
@ConditionalOnProperty(name = "ai.llm.provider", havingValue = "fake")
public class FakeLlmServiceImpl implements LlmService {

  static final String FAIL_TRIGGER = "#fail";

  private final long latencyMillis;

  public FakeLlmServiceImpl(@Value("${ai.llm.fake-latency-ms:800}") long latencyMillis) {
    this.latencyMillis = latencyMillis;
  }

  @Override
  public LlmCompletion generateSoapDraft(LlmPrompt prompt) {
    // Lets you test the 503 path from Swagger: write #fail in the current reason.
    if (prompt.userMessage().contains(FAIL_TRIGGER)) {
      throw new AiProviderUnavailableException("fake", null);
    }
    simulateLatency();
    var backgrounds = prompt.userMessage().split("Consulta del").length - 1;
    return new LlmCompletion(
        "Propietario refiere el motivo actual. Se consideraron " + backgrounds
            + " antecedentes seleccionados.",
        "Completar con el examen físico de hoy.",
        "Diferenciales sugeridos: 1) dermatitis alérgica, 2) infección bacteriana secundaria.",
        "Citología de piel y control en 7 días. Dosis a definir por el veterinario.",
        List.of("Borrador generado por el proveedor de prueba, no por un modelo real."),
        "fake");
  }

  @Override
  public LlmAnswer answerOwnerQuestion(LlmPrompt prompt) {
    // Same trick for the owner chat: write #fail in the question to get a 503.
    if (prompt.userMessage().contains(FAIL_TRIGGER)) {
      throw new AiProviderUnavailableException("fake", null);
    }
    simulateLatency();
    return new LlmAnswer(
        "En palabras simples: el veterinario revisó a tu mascota y dejó indicaciones para su "
            + "cuidado. Sigue el tratamiento tal como te lo indicó y no cambies las dosis por tu "
            + "cuenta. Si notas que empeora, no come o está decaída, llévala a la clínica. "
            + "(Respuesta de prueba, no de un modelo real.)",
        "fake");
  }

  private void simulateLatency() {
    if (latencyMillis <= 0) {
      return;
    }
    try {
      Thread.sleep(latencyMillis);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    }
  }
}
