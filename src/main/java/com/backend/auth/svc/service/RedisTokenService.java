package com.backend.auth.svc.service;

import com.backend.auth.svc.config.RedisTemplateFactory;
import com.backend.auth.svc.model.entity.User;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenService {

    private final DiscoveryService discoveryService;
    private final RedisTemplateFactory templateFactory;

    public RedisTokenService(DiscoveryService discoveryService, RedisTemplateFactory templateFactory) {
        this.discoveryService = discoveryService;
        this.templateFactory = templateFactory;
    }

    public void storeToken(String token, User user, long expirySeconds) {
        ServiceInstance node = discoveryService.getNodeForKey(user.getEmail());

        if (node == null) throw new RuntimeException("No Redis node available");

        StringRedisTemplate redisTemplate = templateFactory.getTemplate("localhost", node.getPort());

        String key = "auth:token:" + user.getId();
        redisTemplate.opsForValue().set(key, token, expirySeconds, TimeUnit.SECONDS);

        System.out.println("Stored token for user :" + user.getUsername() +" userId: "+ user.getId() +" in Redis node " + node.getServiceId());
    }

    public String getToken(String userId){
        ServiceInstance node = discoveryService.getNodeForKey(userId);

        if (node == null) throw new RuntimeException("No Redis node available");

        StringRedisTemplate redisTemplate = templateFactory.getTemplate("localhost", node.getPort());
        String key = "auth:token:" + userId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return redisTemplate.opsForValue().get(key);
        }
        throw new RuntimeException("Unauthorized: Token not found or expired");
    }

    public void removeToken(String userId) {
        ServiceInstance node = discoveryService.getNodeForKey(userId);

        if (node == null) return;

        StringRedisTemplate redisTemplate = templateFactory.getTemplate("localhost", node.getPort());
        String key = "auth:token:" + userId;
        redisTemplate.delete(key);

        System.out.println("Removed token for user " + userId + " from Redis node " + node.getServiceId());
    }

    public Optional<String> hasValidToken(String userId) {
        ServiceInstance node = discoveryService.getNodeForKey(userId);
        if (node == null) throw new RuntimeException("No Redis node available");
        StringRedisTemplate redisTemplate = templateFactory.getTemplate("localhost", node.getPort());
        String key = "auth:token:" + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        }
        return Optional.empty();
    }
}
