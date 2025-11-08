package com.backend.auth.svc.config;

import jakarta.annotation.PreDestroy;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisTemplateFactory {
    private final ConcurrentHashMap<String, StringRedisTemplate> cache = new ConcurrentHashMap<>();

    /**
     * Returns a RedisTemplate for a dynamic host:port.
     * Caches templates to avoid creating new connections each time.
     */
    public StringRedisTemplate getTemplate(String host, int port) {
        String key = host + ":" + port;
        return cache.computeIfAbsent(key, k -> {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
            LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
            factory.afterPropertiesSet();
            return new StringRedisTemplate(factory);
        });
    }

    @PreDestroy
    public void shutdown() {
        cache.values().forEach(template -> {
            LettuceConnectionFactory factory =
                    (LettuceConnectionFactory) template.getConnectionFactory();
            if (factory != null && factory.isRunning()) {
                factory.destroy();
            }
        });
    }
}
