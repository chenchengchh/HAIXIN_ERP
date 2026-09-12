package com.hxcoe.oa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.oa.iam.config.OaJwtConfig;
import com.hxcoe.oa.iam.security.OaJwtAuthenticationFilter;
import com.hxcoe.oa.iam.security.OaSecurityExceptionHandler;
import com.hxcoe.oa.iam.util.OaJwtUtils;
import com.hxcoe.oa.web.TraceIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public TraceIdFilter traceIdFilter() {
        return new TraceIdFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            OaJwtConfig oaJwtConfig,
            OaJwtUtils oaJwtUtils,
            TraceIdFilter traceIdFilter
    ) throws Exception {
        OaSecurityExceptionHandler exceptionHandler = new OaSecurityExceptionHandler(objectMapper);
        OaJwtAuthenticationFilter jwtFilter = new OaJwtAuthenticationFilter(oaJwtConfig, oaJwtUtils);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(exceptionHandler)
                        .accessDeniedHandler(exceptionHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/iam/auth/login",
                                "/iam/auth/login",
                                "/api/login",
                                "/actuator/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(traceIdFilter, AuthorizationFilter.class)
                .addFilterBefore(jwtFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
