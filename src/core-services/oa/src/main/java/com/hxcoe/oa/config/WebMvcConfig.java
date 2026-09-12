package com.hxcoe.oa.config;

import com.hxcoe.oa.iam.audit.OaAuditActionInterceptor;
import com.hxcoe.oa.iam.repository.OaAuditActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private OaAuditActionRepository oaAuditActionRepository;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OaAuditActionInterceptor(oaAuditActionRepository))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/iam/auth/login",
                        "/iam/auth/login",
                        "/api/login",
                        "/actuator/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                );
    }
}

