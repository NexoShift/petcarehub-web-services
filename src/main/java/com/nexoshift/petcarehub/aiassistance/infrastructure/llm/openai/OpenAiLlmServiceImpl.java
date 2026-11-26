package com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai;

import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmAnswer;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmCompletion;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmPrompt;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmService;
import com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai.configuration.OpenAiProperties;
import com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai.dto.ChatCompletionRequest;
import com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai.dto.ChatCompletionResponse;
import com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai.dto.ChatMessage;
import com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai.dto.SoapDraftPayload;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * OpenAI adapter (anti-corruption layer): nothing about HTTP or OpenAI error codes leaves
 * this class, only AiProviderUnavailableException. The prompt is never logged.
 */
@Service
@ConditionalOnProperty(name = "ai.llm.provider", havingValue = "openai")
public class OpenAiLlmServiceImpl implements LlmService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiLlmServiceImpl.class);
  private static final String PROVIDER = "OpenAI";

  /** JSON Schema the model is forced to follow (structured outputs, strict mode). */
  private static final Map<String, Object> SOAP_SCHEMA = Map.of(
      "type", "object",
      "properties", Map.of(
          "subjective", Map.of("type", "string"),
          "objective", Map.of("type", "string"),
          "assessment", Map.of("type", "string"),
          "plan", Map.of("type", "string"),
          "warnings", Map.of("type", "array", "items", Map.of("type", "string"))),
      "required", List.of("subjective", "objective", "assessment", "plan", "warnings"),
      "additionalProperties", false);

  private static final Map<String, Object> RESPONSE_FORMAT = Map.of(
      "type", "json_schema",
      "json_schema", Map.of("name", "soap_draft", "strict", true, "schema", SOAP_SCHEMA));

  private final RestClient restClient;
  private final OpenAiProperties properties;
  private final ObjectMapper objectMapper;

  public OpenAiLlmServiceImpl(OpenAiProperties properties, ObjectMapper objectMapper) {
    if (properties.apiKey() == null || properties.apiKey().isBlank()) {
      throw new IllegalStateException("Set OPENAI_API_KEY or use ai.llm.provider=fake");
    }
    this.properties = properties;
    this.objectMapper = objectMapper;
    var httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    var requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(properties.timeout());
    this.restClient = RestClient.builder()
        .baseUrl(properties.baseUrl())
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
        .requestFactory(requestFactory)
        .build();
  }

  @Override
  public LlmCompletion generateSoapDraft(LlmPrompt prompt) {
    var response = complete(prompt, RESPONSE_FORMAT);
    SoapDraftPayload payload;
    try {
      payload = objectMapper.readValue(contentOf(response), SoapDraftPayload.class);
    } catch (JacksonException exception) {
      LOGGER.warn("OpenAI draft was not valid JSON: {}", exception.getMessage());
      throw new AiProviderUnavailableException(PROVIDER, exception);
    }
    return new LlmCompletion(payload.subjective(), payload.objective(), payload.assessment(),
        payload.plan(), payload.warnings(), response.model());
  }

  @Override
  public LlmAnswer answerOwnerQuestion(LlmPrompt prompt) {
    // Plain text answer: no response_format.
    var response = complete(prompt, null);
    var text = contentOf(response);
    if (text.isBlank()) {
      throw new AiProviderUnavailableException(PROVIDER, null);
    }
    return new LlmAnswer(text.trim(), response.model());
  }

  private ChatCompletionResponse complete(LlmPrompt prompt, Map<String, Object> responseFormat) {
    var request = new ChatCompletionRequest(
        properties.model(),
        List.of(new ChatMessage("system", prompt.systemMessage()),
            new ChatMessage("user", prompt.userMessage())),
        responseFormat,
        properties.maxCompletionTokens());
    try {
      return restClient.post()
          .uri("/chat/completions")
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(ChatCompletionResponse.class);
    } catch (RestClientResponseException exception) {
      // 401 bad key, 429 no credit or rate limit, 5xx provider error
      LOGGER.warn("OpenAI answered {}: {}", exception.getStatusCode(),
          exception.getResponseBodyAsString());
      throw new AiProviderUnavailableException(PROVIDER, exception);
    } catch (RestClientException | JacksonException exception) {
      // timeout, no network, or a body we could not read
      LOGGER.warn("OpenAI call failed: {}", exception.getMessage());
      throw new AiProviderUnavailableException(PROVIDER, exception);
    }
  }

  /** Content of the first choice; a cut or refused answer counts as provider failure. */
  private static String contentOf(ChatCompletionResponse response) {
    if (response == null || response.choices() == null || response.choices().isEmpty()) {
      throw new AiProviderUnavailableException(PROVIDER, null);
    }
    var choice = response.choices().getFirst();
    if ("length".equals(choice.finishReason()) || choice.message() == null
        || choice.message().content() == null) {
      // Cut by max_completion_tokens, or the model refused to answer.
      LOGGER.warn("OpenAI returned no usable answer (finish_reason={})", choice.finishReason());
      throw new AiProviderUnavailableException(PROVIDER, null);
    }
    return choice.message().content();
  }
}
