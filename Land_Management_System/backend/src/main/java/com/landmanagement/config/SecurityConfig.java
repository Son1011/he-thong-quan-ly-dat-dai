package com.landmanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.landmanagement.dto.response.ApiErrorResponse;
import com.landmanagement.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security configuration for the application.
 * Handles JWT authentication, CORS, CSRF, and security headers.
 */
@Configuration
public class SecurityConfig {

        private final ObjectMapper objectMapper;

        public SecurityConfig(ObjectMapper objectMapper) {
                this.objectMapper = objectMapper;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter)
                        throws Exception {

                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .headers(headers -> headers
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives("default-src 'self'"))
                                                .frameOptions(frame -> frame.deny()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/actuator/health/**").permitAll()
                                                .requestMatchers("/actuator/**").hasRole("ADMIN")
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                                                                        .code("AUTHENTICATION_ERROR")
                                                                        .message("Authentication failed: "
                                                                                        + authException.getMessage())
                                                                        .status(HttpServletResponse.SC_UNAUTHORIZED)
                                                                        .path(request.getRequestURI())
                                                                        .timestamp(LocalDateTime.now())
                                                                        .build();

                                                        writeErrorResponse(
                                                                        response,
                                                                        HttpServletResponse.SC_UNAUTHORIZED,
                                                                        errorResponse);
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                                                                        .code("ACCESS_DENIED")
                                                                        .message("You do not have permission to access this resource")
                                                                        .status(HttpServletResponse.SC_FORBIDDEN)
                                                                        .path(request.getRequestURI())
                                                                        .timestamp(LocalDateTime.now())
                                                                        .build();

                                                        writeErrorResponse(
                                                                        response,
                                                                        HttpServletResponse.SC_FORBIDDEN,
                                                                        errorResponse);
                                                }))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        private void writeErrorResponse(
                        HttpServletResponse response,
                        int status,
                        ApiErrorResponse errorResponse)
                        throws IOException {

                response.setStatus(status);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOriginPatterns(List.of("*"));
                configuration.setAllowedMethods(List.of(
                                "GET",
                                "POST",
                                "PUT",
                                "PATCH",
                                "DELETE",
                                "OPTIONS",
                                "HEAD"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setExposedHeaders(List.of(
                                "Content-Disposition",
                                "X-Total-Count",
                                "X-Page-Number"));
                configuration.setAllowCredentials(false);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}