package com.backend.auth.svc.service;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Profile("consistencyHashService")
public class ConsistencyHashService implements DiscoveryService{

    private final SortedMap<Integer, ServiceInstance> hashRing = new TreeMap<Integer, ServiceInstance>();
    private final DiscoveryClient discoveryClient;

    public ConsistencyHashService(DiscoveryClient discoveryClient) {
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

        hashRing.clear();
        if (nodes != null) {

            int VIRTUAL_NODE_COUNT = 100;

            for (ServiceInstance node : nodes) {
                String nodeId = node.getHost() + ":" + node.getPort();
                for (int i = 0; i < VIRTUAL_NODE_COUNT; i++) {
                    String vNodeId = nodeId + "#" + i;
                    int hash = Hashing.murmur3_32().hashString(vNodeId, StandardCharsets.UTF_8).asInt();
                    hashRing.put(hash, node);
                }
            }


//            for (ServiceInstance node : nodes) {
//                String nodeId = node.getHost() + ":" + node.getPort();
//                int hash = Hashing.murmur3_32().hashString(nodeId, StandardCharsets.UTF_8).asInt();
//                hashRing.put(hash, node);
//            }
        }
//        System.out.println("Updated hash ring with " + hashRing.size() + " nodes");
//        hashRing.values().forEach(n ->
//                System.out.println(" - " + n.getServiceId() + " (" + n.getUri() + ":" + n.getPort() + ")")
//        );
    }

    @Override
    public ServiceInstance getNodeForKey(String key) {
        if (hashRing.isEmpty()) updateRedisNodesIfNeeded();

        int hash = Hashing.murmur3_32()
                .hashString(key, StandardCharsets.UTF_8)
                .asInt();

        SortedMap<Integer, ServiceInstance> tailMap = hashRing.tailMap(hash);
        if (!tailMap.isEmpty()) {
            return tailMap.get(tailMap.firstKey());
        } else {
            return hashRing.get(hashRing.firstKey());
        }
    }
}
