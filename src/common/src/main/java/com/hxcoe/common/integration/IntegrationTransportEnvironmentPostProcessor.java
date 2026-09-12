package com.hxcoe.common.integration;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class IntegrationTransportEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String EXCLUDE_KEY = "spring.autoconfigure.exclude";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String transport = environment.getProperty("hxcoe.integration.transport", "HTTP");
        Set<String> excludes = new LinkedHashSet<>();
        String existing = environment.getProperty(EXCLUDE_KEY, "");
        if (existing != null && !existing.isBlank()) {
            for (String s : existing.split(",")) {
                if (!s.isBlank()) {
                    excludes.add(s.trim());
                }
            }
        }
        if (!"RABBIT".equalsIgnoreCase(transport)) {
            excludes.add("org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration");
            excludes.add("org.springframework.boot.autoconfigure.amqp.RabbitReactiveAutoConfiguration");
            excludes.add("org.springframework.boot.autoconfigure.amqp.RabbitHealthContributorAutoConfiguration");
        }
        if (!"REDIS_STREAM".equalsIgnoreCase(transport)) {
            excludes.add("org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration");
            excludes.add("org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration");
            excludes.add("org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration");
            excludes.add("org.springframework.boot.autoconfigure.data.redis.RedisHealthContributorAutoConfiguration");
        }
        String joined = String.join(",", excludes);
        environment.getPropertySources().addFirst(new MapPropertySource("hxcoeIntegrationTransportExcludes", java.util.Map.of(EXCLUDE_KEY, joined)));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
