package com.backend.auth.svc.scheduler;

import com.backend.auth.svc.service.DiscoveryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisNodeRefresher {
    private final DiscoveryService discoveryService;

    public RedisNodeRefresher(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }
    @Scheduled(fixedDelay = 30000)
    public void refreshNodes() {
        try {
            discoveryService.updateRedisNodesIfNeeded();
            System.out.println("[RedisNodeRefresher] Node list refreshed.");
        } catch (Exception e) {
            System.err.println("[RedisNodeRefresher] Failed to refresh nodes: " + e.getMessage());
        }
    }
}
