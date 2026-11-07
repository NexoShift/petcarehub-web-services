package com.nexoshift.petcarehub.iam.support;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/** Small helper to call the IAM REST API from MockMvc tests. */
public final class IamApiClient {

  public static final String PASSWORD = "Password123!";

  public record Reply(int status, String body) {
    public String text(String field) {
      var matcher = Pattern.compile("\"" + field + "\":\"([^\"]*)\"").matcher(body);
      assertThat(matcher.find()).as("field %s in %s", field, body).isTrue();
      return matcher.group(1);
    }

    public long number(String field) {
      var matcher = Pattern.compile("\"" + field + "\":(\\d+)").matcher(body);
      assertThat(matcher.find()).as("field %s in %s", field, body).isTrue();
      return Long.parseLong(matcher.group(1));
    }

    public boolean flag(String field) {
      var matcher = Pattern.compile("\"" + field + "\":(true|false)").matcher(body);
      assertThat(matcher.find()).as("field %s in %s", field, body).isTrue();
      return Boolean.parseBoolean(matcher.group(1));
    }
  }

  private final MockMvc mockMvc;

  public IamApiClient(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  public Reply send(HttpMethod method, String url, String token, String json) throws Exception {
    var request = MockMvcRequestBuilders.request(method, url).accept(MediaType.APPLICATION_JSON);
    if (token != null) {
      request = request.header("Authorization", "Bearer " + token);
    }
    if (json != null) {
      request = request.contentType(MediaType.APPLICATION_JSON).content(json);
    }
    var response = mockMvc.perform(request).andReturn().getResponse();
    return new Reply(response.getStatus(), response.getContentAsString());
  }

  public Reply signUp(String email, String password, String accountType) throws Exception {
    return send(HttpMethod.POST, "/api/v1/authentication/sign-up", null,
        "{\"email\":\"%s\",\"password\":\"%s\",\"accountType\":\"%s\"}"
            .formatted(email, password, accountType));
  }

  public Reply signIn(String email, String password) throws Exception {
    return send(HttpMethod.POST, "/api/v1/authentication/sign-in", null,
        "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password));
  }

  /** Signs up with {@link #PASSWORD} and returns the sign-in reply (it has the token). */
  public Reply registerAndSignIn(String email, String accountType) throws Exception {
    assertThat(signUp(email, PASSWORD, accountType).status()).isEqualTo(201);
    var reply = signIn(email, PASSWORD);
    assertThat(reply.status()).isEqualTo(200);
    return reply;
  }
}
