package com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmPrompt;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;
import com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai.configuration.OpenAiProperties;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** Runs the adapter against a local fake OpenAI server: no key, no internet, no cost. */
class OpenAiLlmServiceImplTest {

  private static final String DRAFT_JSON = """
      {"subjective":"Prurito","objective":"Eritema","assessment":"1) Atopia",
       "plan":"Citologia. Dosis a definir por el veterinario.","warnings":["Falta peso"]}""";

  private final ObjectMapper objectMapper = JsonMapper.builder().build();
  private final AtomicReference<String> lastRequestBody = new AtomicReference<>();
  private final AtomicReference<String> lastAuthorization = new AtomicReference<>();
  private HttpServer server;
  private volatile int status;
  private volatile String responseBody;
  private volatile long delayMillis;

  @BeforeEach
  void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/v1/chat/completions", exchange -> {
      lastAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
      lastRequestBody.set(new String(exchange.getRequestBody().readAllBytes(),
          StandardCharsets.UTF_8));
      sleep(delayMillis);
      var bytes = responseBody.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(status, bytes.length);
      exchange.getResponseBody().write(bytes);
      exchange.close();
    });
    server.start();
  }

  @AfterEach
  void stopServer() {
    server.stop(0);
  }

  private OpenAiLlmServiceImpl adapter(Duration timeout) {
    var baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
    return new OpenAiLlmServiceImpl(
        new OpenAiProperties("sk-test", baseUrl, "gpt-5.4-mini", timeout, 1200), objectMapper);
  }

  private void answer(int newStatus, String body) {
    status = newStatus;
    responseBody = body;
  }

  private static String completionWith(String content, String finishReason) {
    return """
        {"id":"x","model":"gpt-5.4-mini-2026","choices":[{"index":0,
         "message":{"role":"assistant","content":%s},"finish_reason":"%s"}],
         "usage":{"prompt_tokens":10}}""".formatted(content, finishReason);
  }

  private String quoted(String json) {
    return objectMapper.writeValueAsString(json);
  }

  @Test
  void parsesTheStructuredDraft() {
    answer(200, completionWith(quoted(DRAFT_JSON), "stop"));

    var completion = adapter(Duration.ofSeconds(5))
        .generateSoapDraft(new LlmPrompt("system", "user"));

    assertEquals("Prurito", completion.subjective());
    assertEquals("1) Atopia", completion.assessment());
    assertEquals(1, completion.warnings().size());
    assertEquals("gpt-5.4-mini-2026", completion.model());
  }

  @Test
  void sendsTheKeyTheSchemaAndNoTemperature() {
    answer(200, completionWith(quoted(DRAFT_JSON), "stop"));

    adapter(Duration.ofSeconds(5)).generateSoapDraft(new LlmPrompt("system", "user"));

    assertEquals("Bearer sk-test", lastAuthorization.get());
    var body = lastRequestBody.get();
    assertTrue(body.contains("\"max_completion_tokens\":1200"));
    assertTrue(body.contains("\"json_schema\""));
    assertTrue(body.contains("\"strict\":true"));
    assertFalse(body.contains("temperature"));
  }

  @Test
  void rateLimitOrNoCreditIsUnavailable() {
    answer(429, "{\"error\":{\"code\":\"insufficient_quota\"}}");
    assertThrows(AiProviderUnavailableException.class,
        () -> adapter(Duration.ofSeconds(5)).generateSoapDraft(new LlmPrompt("s", "u")));
  }

  @Test
  void invalidKeyIsUnavailable() {
    answer(401, "{\"error\":{\"code\":\"invalid_api_key\"}}");
    assertThrows(AiProviderUnavailableException.class,
        () -> adapter(Duration.ofSeconds(5)).generateSoapDraft(new LlmPrompt("s", "u")));
  }

  @Test
  void timeoutIsUnavailable() {
    answer(200, completionWith(quoted(DRAFT_JSON), "stop"));
    delayMillis = 1500;
    assertThrows(AiProviderUnavailableException.class,
        () -> adapter(Duration.ofMillis(300)).generateSoapDraft(new LlmPrompt("s", "u")));
  }

  @Test
  void draftCutByTheTokenLimitIsUnavailable() {
    answer(200, completionWith(quoted("{\"subjective\":\"Pru"), "length"));
    assertThrows(AiProviderUnavailableException.class,
        () -> adapter(Duration.ofSeconds(5)).generateSoapDraft(new LlmPrompt("s", "u")));
  }

  @Test
  void refusalWithoutContentIsUnavailable() {
    answer(200, completionWith("null", "stop"));
    assertThrows(AiProviderUnavailableException.class,
        () -> adapter(Duration.ofSeconds(5)).generateSoapDraft(new LlmPrompt("s", "u")));
  }

  @Test
  void contentThatIsNotJsonIsUnavailable() {
    answer(200, completionWith(quoted("Lo siento, no puedo"), "stop"));
    assertThrows(AiProviderUnavailableException.class,
        () -> adapter(Duration.ofSeconds(5)).generateSoapDraft(new LlmPrompt("s", "u")));
  }

  @Test
  void ownerAnswerIsPlainTextWithoutSchema() {
    answer(200, completionWith(quoted("  Rocky tiene la piel irritada.  "), "stop"));

    var answer = adapter(Duration.ofSeconds(5))
        .answerOwnerQuestion(new LlmPrompt("system", "user"));

    assertEquals("Rocky tiene la piel irritada.", answer.text());
    assertEquals("gpt-5.4-mini-2026", answer.model());
    assertFalse(lastRequestBody.get().contains("response_format"));
  }

  @Test
  void ownerAnswerCutByTheTokenLimitIsUnavailable() {
    answer(200, completionWith(quoted("Rocky tiene"), "length"));
    assertThrows(AiProviderUnavailableException.class,
        () -> adapter(Duration.ofSeconds(5)).answerOwnerQuestion(new LlmPrompt("s", "u")));
  }

  @Test
  void ownerAnswerWithProviderErrorIsUnavailable() {
    answer(500, "{\"error\":{\"code\":\"server_error\"}}");
    assertThrows(AiProviderUnavailableException.class,
        () -> adapter(Duration.ofSeconds(5)).answerOwnerQuestion(new LlmPrompt("s", "u")));
  }

  @Test
  void missingKeyFailsAtStartup() {
    var properties = new OpenAiProperties(" ", "http://localhost", "m", Duration.ofSeconds(1), 10);
    assertThrows(IllegalStateException.class,
        () -> new OpenAiLlmServiceImpl(properties, objectMapper));
  }

  private static void sleep(long millis) {
    if (millis <= 0) {
      return;
    }
    try {
      Thread.sleep(millis);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    }
  }
}
