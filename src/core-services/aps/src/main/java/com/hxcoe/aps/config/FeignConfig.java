package com.hxcoe.aps.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign客户端配置。
 * <p>提供服务间调用的请求拦截器，将当前HTTP请求的Authorization头中继到下游服务（如OA），
 * 使下游服务的JWT认证过滤器能够识别调用方身份，解决APS通过Feign直连OA提交审批时的401问题。</p>
 * <p>注意：本类不标注@Configuration，避免被组件扫描注册为主上下文bean导致重复加载；
 * 由@EnableFeignClients(defaultConfiguration=FeignConfig.class)作为Feign默认配置引入，
 * 对APS所有Feign客户端生效。</p>
 */
public class FeignConfig {

    private static final Logger logger = LoggerFactory.getLogger(FeignConfig.class);

    /**
     * Authorization头中继拦截器。
     * <p>从当前请求上下文（RequestContextHolder）取出Authorization头并转发到Feign请求，
     * 实现用户身份在服务间调用的传递。若当前无线程绑定请求（如异步/定时任务），则跳过。</p>
     *
     * @return 请求拦截器
     */
    @Bean
    public RequestInterceptor authorizationRelayInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes == null) {
                    return;
                }
                HttpServletRequest request = attributes.getRequest();
                String authorization = request.getHeader("Authorization");
                if (authorization != null && !authorization.isBlank()) {
                    template.header("Authorization", authorization);
                }
            }
        };
    }
}
