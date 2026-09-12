package com.hxcoe.common.web;

import com.hxcoe.common.exception.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@AutoConfiguration
public class RequestContextAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = {
            "jakarta.servlet.Filter",
            "org.springframework.boot.web.servlet.FilterRegistrationBean"
    })
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletRequestContextConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "hxcoeRequestContextFilter")
        public FilterRegistrationBean<RequestContextFilter> hxcoeRequestContextFilter(
                @Value("${spring.application.name:unknown-service}") String serviceName
        ) {
            FilterRegistrationBean<RequestContextFilter> registrationBean = new FilterRegistrationBean<>();
            registrationBean.setFilter(new RequestContextFilter(serviceName));
            registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return registrationBean;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletExceptionConfiguration {

        @Bean
        @ConditionalOnMissingBean(GlobalExceptionHandler.class)
        public GlobalExceptionHandler hxcoeGlobalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }
}
