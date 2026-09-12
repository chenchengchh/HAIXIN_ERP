package com.hxcoe.aibrain.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * RestTemplate 配置（P4-1 动作执行引擎基础设施）
 * 注册带 Authorization 头中继拦截器的 RestTemplate：
 * 决策中心"执行"按钮触发的请求经网关携带 JWT 到达 ai-brain，
 * 本拦截器将该 JWT 继续中继到目标业务服务，使下游 JWT 过滤器识别调用方身份（防 401）
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 构建带头中继的 RestTemplate
     *
     * @param builder Spring Boot RestTemplateBuilder
     * @return RestTemplate 实例
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.additionalInterceptors((request, body, execution) -> {
            // 从当前请求上下文取出 Authorization 头并转发（定时任务等无上下文场景自动跳过）
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest current = attributes.getRequest();
                String authorization = current.getHeader("Authorization");
                if (authorization != null && !authorization.isBlank()) {
                    request.getHeaders().set("Authorization", authorization);
                }
            }
            return execution.execute(request, body);
        }).build();
    }
}
