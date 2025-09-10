package com.deliverytech.delivery.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("clientes", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(30)));

        cacheConfigurations.put("cliente", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(2)));
        
        cacheConfigurations.put("produtos", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(45)));

        cacheConfigurations.put("produto", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(2)));

        cacheConfigurations.put("restaurantes", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(30)));

        cacheConfigurations.put("restaurante", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(2)));

        cacheConfigurations.put("pedidos", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(20)));

        cacheConfigurations.put("pedido", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(1)));

        return RedisCacheManager.builder(redisConnectionFactory)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}