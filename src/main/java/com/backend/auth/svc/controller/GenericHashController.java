package com.backend.auth.svc.controller;

import com.backend.auth.svc.model.response.HashAnalysisResponse;
import com.backend.auth.svc.service.DiscoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class GenericHashController {

    private final DiscoveryService discoveryService;

    public GenericHashController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Analyze key distribution for any DiscoveryService (modular or ring-based)
     * @param sampleKeys number of random keys to simulate (default 10_000)
     */
    @GetMapping
    public ResponseEntity<HashAnalysisResponse> analyze(@RequestParam(value = "sampleKeys", required = false) Integer sampleKeys) {
        int sampleSize = (sampleKeys == null || sampleKeys <= 0) ? 10_000 : sampleKeys;

        // Ensure nodes are up-to-date
        discoveryService.updateRedisNodesIfNeeded();

        // Fetch current nodes
        List<?> nodeListRaw = null;
        try {
            nodeListRaw = (List<?>) discoveryService.getClass()
                    .getMethod("getRedisNodes")
                    .invoke(discoveryService);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<?> nodes = (nodeListRaw != null) ? (List<?>) nodeListRaw : List.of();

        // Prepare response DTO
        HashAnalysisResponse response = new HashAnalysisResponse();

        if (nodes.isEmpty()) {
            response.setPrettyText("No nodes available for analysis.");
            return ResponseEntity.ok(response);
        }

        // Map node info
        List<HashAnalysisResponse.NodeInfo> nodeInfos = nodes.stream().map(n -> {
            try {
                String serviceId = (String) n.getClass().getMethod("getServiceId").invoke(n);
                String host = (String) n.getClass().getMethod("getHost").invoke(n);
                int port = (Integer) n.getClass().getMethod("getPort").invoke(n);
                return new HashAnalysisResponse.NodeInfo(serviceId, host, port, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();
        response.setNodes(nodeInfos);

        // Compute sample key distribution
        Map<String, Integer> dist = new LinkedHashMap<>();
        nodeInfos.forEach(n -> dist.put(n.serviceId, 0));

        for (int i = 0; i < sampleSize; i++) {
            String key = UUID.randomUUID().toString();
            Object node = discoveryService.getNodeForKey(key);
            try {
                String serviceId = (String) node.getClass().getMethod("getServiceId").invoke(node);
                dist.compute(serviceId, (k, v) -> (v == null ? 1 : v + 1));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        response.setKeyDistribution(dist);

        // Build pretty text
        StringBuilder pretty = new StringBuilder();
        pretty.append("Generic Hash Analysis\n---------------------------------\n");
        pretty.append("Nodes:\n");
        nodeInfos.forEach(n -> pretty.append(" - ").append(n.serviceId).append(" (").append(n.host).append(":").append(n.port).append(")\n"));
        pretty.append("\nSample key distribution (").append(sampleSize).append(" keys):\n");
        int total = dist.values().stream().mapToInt(Integer::intValue).sum();
        dist.forEach((k, v) -> pretty.append(" - ").append(k).append(" -> ").append(v)
                .append(" keys (").append(String.format("%.2f", 100.0 * v / total)).append("%)\n"));
        response.setPrettyText(pretty.toString());

        return ResponseEntity.ok(response);
    }
}
