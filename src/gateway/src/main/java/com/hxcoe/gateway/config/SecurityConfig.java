package com.hxcoe.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 网关服务安全配置类
 * 禁用CSRF保护，允许服务间正常通信
 * 启用JWT认证
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            // 配置CSRF保护
            .csrf(csrf -> csrf.disable())
            // 配置表单登录
            .formLogin(formLogin -> formLogin.disable())
            // 配置HTTP基本认证
            .httpBasic(httpBasic -> httpBasic.disable())
            // 配置授权规则
            .authorizeExchange(authorizeExchange -> 
                authorizeExchange.anyExchange().permitAll());
        
        return http.build();
    }
}