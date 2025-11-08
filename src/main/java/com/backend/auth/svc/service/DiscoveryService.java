package com.backend.auth.svc.service;


import org.springframework.cloud.client.ServiceInstance;

public interface DiscoveryService {
    public ServiceInstance getNodeForKey(String key);

    public void updateRedisNodesIfNeeded();
}
