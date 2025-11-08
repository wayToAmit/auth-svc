package com.backend.auth.svc.service;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Profile("modularHashService")
public class ModularHashService implements DiscoveryService{
    private final DiscoveryClient discoveryClient;
    private final AtomicReference<List<ServiceInstance>> cachedNodes = new AtomicReference<>();

    public ModularHashService(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public List<ServiceInstance> getRedisNodes() {
        return discoveryClient.getServices().stream()
                .filter(serviceId -> serviceId.startsWith("redis"))
                .flatMap(serviceId -> discoveryClient.getInstances(serviceId).stream())
                .toList();
    }

    @Override
    public synchronized void updateRedisNodesIfNeeded() {
        List<ServiceInstance> nodes = getRedisNodes();
        if (nodes == null || nodes.isEmpty()) {
            System.err.println("No Redis nodes found in Consul!");
            cachedNodes.set(List.of());
        } else {
            cachedNodes.set(nodes);
//            System.out.println("Updated Redis nodes: " + nodes.size());
//            nodes.forEach(n -> System.out.println(" - " + n.getServiceId() + " (" + n.getHost() + ":" + n.getPort() + ")"));
        }
    }

    @Override
    public ServiceInstance getNodeForKey(String key) {

        List<ServiceInstance> nodes = cachedNodes.get();
        if (nodes.isEmpty()) {
            updateRedisNodesIfNeeded(); // Lazy init fallback
            nodes = cachedNodes.get();
            if (nodes.isEmpty()) return null;
        }

        int index = Math.abs(key.hashCode()) % nodes.size();
        ServiceInstance selectedNode = nodes.get(index);
//        System.out.println("Key [" + key + "] → Redis node: " +
//                selectedNode.getServiceId() + " (" + selectedNode.getHost() + ":" + selectedNode.getPort() + ")");
        return selectedNode;
    }
}
