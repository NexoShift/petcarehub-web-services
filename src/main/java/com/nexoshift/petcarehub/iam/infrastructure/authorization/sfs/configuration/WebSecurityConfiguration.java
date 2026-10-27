package com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.configuration;

import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.pipeline.ForbiddenRequestHandler;
import com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.pipeline.UnauthorizedRequestHandlerEntryPoint;
import com.nexoshift.petcarehub.iam.infrastructure.tokens.jwt.BearerTokenService;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Stateless security with JWT. Public routes: authentication and Swagger. */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {

  private static final String[] PUBLIC_ROUTES = {
      "/api/v1/authentication/**",
      "/v3/api-docs/**",
      "/swagger-ui.html",
      "/swagger-ui/**",
      "/swagger-resources/**",
      "/webjars/**",
      "/error"
  };

  private final BearerTokenService tokenService;
  private final UserDetailsService userDetailsService;
  private final UnauthorizedRequestHandlerEntryPoint unauthorizedHandler;
  private final ForbiddenRequestHandler forbiddenHandler;
  private final List<String> allowedOrigins;

  public WebSecurityConfiguration(BearerTokenService tokenService,
      @Qualifier("defaultUserDetailsService") UserDetailsService userDetailsService,
      UnauthorizedRequestHandlerEntryPoint unauthorizedHandler,
      ForbiddenRequestHandler forbiddenHandler,
      @Value("${app.cors.allowed-origins:*}") List<String> allowedOrigins) {
    this.tokenService = tokenService;
    this.userDetailsService = userDetailsService;
    this.unauthorizedHandler = unauthorizedHandler;
    this.forbiddenHandler = forbiddenHandler;
    this.allowedOrigins = allowedOrigins;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(unauthorizedHandler)
            .accessDeniedHandler(forbiddenHandler))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(PUBLIC_ROUTES).permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(new BearerAuthorizationRequestFilter(tokenService, userDetailsService),
            UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    var configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(allowedOrigins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
