package com.hxcoe.scrm.service.douyin;

import com.hxcoe.scrm.exception.douyin.DouyinException;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DouyinModuleGuard {

    private static final Logger logger = LoggerFactory.getLogger(DouyinModuleGuard.class);

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final boolean enabled;

    public DouyinModuleGuard(
            ObjectProvider<StringRedisTemplate> redisTemplateProvider,
            @Value("${scrm.douyin.enabled:true}") boolean enabled
    ) {
        this.redisTemplateProvider = redisTemplateProvider;
        this.enabled = enabled;
    }

    public void requireModuleEnabled(String operation) {
        if (!enabled) {
            throw serviceUnavailable("DOUYIN_MODULE_DISABLED", operation + "暂不可用，抖音模块已显式降级");
        }
    }

    public void requireRedisAvailable(String operation) {
        requireModuleEnabled(operation);
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            throw serviceUnavailable("DOUYIN_REDIS_UNAVAILABLE", operation + "暂不可用，Redis 依赖未就绪");
        }
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        if (connectionFactory == null) {
            throw serviceUnavailable("DOUYIN_REDIS_UNAVAILABLE", operation + "暂不可用，Redis 连接工厂未初始化");
        }
        try (RedisConnection connection = connectionFactory.getConnection()) {
            connection.ping();
        } catch (Exception ex) {
            throw serviceUnavailable("DOUYIN_REDIS_UNAVAILABLE", operation + "暂不可用，Redis 连接异常", ex);
        }
    }

    public void requireBrowserAvailable(String operation) {
        requireModuleEnabled(operation);
        try (
                Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setArgs(List.of("--no-sandbox", "--disable-setuid-sandbox", "--disable-dev-shm-usage")))
        ) {
            logger.debug("Douyin browser probe passed operation={}", operation);
        } catch (Exception ex) {
            throw serviceUnavailable("DOUYIN_BROWSER_UNAVAILABLE", operation + "暂不可用，Playwright 运行环境未就绪", ex);
        }
    }

    private DouyinException serviceUnavailable(String errorCode, String message) {
        return serviceUnavailable(errorCode, message, null);
    }

    private DouyinException serviceUnavailable(String errorCode, String message, Throwable cause) {
        logger.warn("Douyin module degraded errorCode={} message={}", errorCode, message, cause);
        return new DouyinException(message, errorCode, 503, cause);
    }
}
