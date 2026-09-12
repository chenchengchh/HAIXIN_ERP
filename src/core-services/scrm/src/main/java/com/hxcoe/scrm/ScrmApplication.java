package com.hxcoe.scrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * SCRM模块主应用类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ScrmApplication {
    
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ScrmApplication.class);
        Environment env = app.run(args).getEnvironment();
        
        // 打印配置信息，用于调试
        System.out.println("=== 应用配置信息 ===");
        System.out.println("当前激活的profile: " + env.getProperty("spring.profiles.active"));
        System.out.println("Redis Host: " + env.getProperty("spring.redis.host"));
        System.out.println("Redis Port: " + env.getProperty("spring.redis.port"));
        System.out.println("MySQL URL: " + env.getProperty("spring.datasource.url"));
        System.out.println("Nacos Server: " + env.getProperty("spring.cloud.nacos.discovery.server-addr"));
        System.out.println("=== 应用配置信息结束 ===");
    }
    
    /**
     * 显式配置Redis连接工厂，确保使用正确的Redis连接参数
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(Environment environment) {
        // 从环境中获取Redis配置
        String host = environment.getProperty("spring.redis.host", "localhost");
        int port = environment.getProperty("spring.redis.port", Integer.class, 6379);
        String password = environment.getProperty("spring.redis.password");
        int database = environment.getProperty("spring.redis.database", Integer.class, 0);
        
        System.out.println("=== 显式Redis连接配置 ===");
        System.out.println("Redis Host (显式配置): " + host);
        System.out.println("Redis Port (显式配置): " + port);
        System.out.println("Redis Database (显式配置): " + database);
        System.out.println("=== 显式Redis连接配置结束 ===");
        
        // 创建Lettuce连接工厂
        LettuceConnectionFactory factory = new LettuceConnectionFactory(host, port);
        factory.setPassword(password);
        factory.setDatabase(database);
        factory.afterPropertiesSet();
        
        return factory;
    }
    
    /**
     * 配置RedisTemplate，用于Redis操作
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
