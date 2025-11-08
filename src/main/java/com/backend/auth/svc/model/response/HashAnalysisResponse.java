package com.backend.auth.svc.model.response;

import java.util.List;
import java.util.Map;

public class HashAnalysisResponse {
    private List<NodeInfo> nodes;
    private List<GapInfo> gaps; // For ring-based services; optional for modular
    private Map<String, Integer> keyDistribution;
    private String prettyText;

    // Getters / setters
    public List<NodeInfo> getNodes() { return nodes; }
    public void setNodes(List<NodeInfo> nodes) { this.nodes = nodes; }

    public List<GapInfo> getGaps() { return gaps; }
    public void setGaps(List<GapInfo> gaps) { this.gaps = gaps; }

    public Map<String, Integer> getKeyDistribution() { return keyDistribution; }
    public void setKeyDistribution(Map<String, Integer> keyDistribution) { this.keyDistribution = keyDistribution; }

    public String getPrettyText() { return prettyText; }
    public void setPrettyText(String prettyText) { this.prettyText = prettyText; }

    // NodeInfo
    public static class NodeInfo {
        public final String serviceId;
        public final String host;
        public final int port;
        public final Long position; // optional for ring, null for modular

        public NodeInfo(String serviceId, String host, int port, Long position) {
            this.serviceId = serviceId;
            this.host = host;
            this.port = port;
            this.position = position;
        }
    }

    // GapInfo (for ring-based services)
    public static class GapInfo {
        public final String fromNode;
        public final String toNode;
        public final long gap;
        public final boolean wrap;

        public GapInfo(String fromNode, String toNode, long gap, boolean wrap) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.gap = gap;
            this.wrap = wrap;
        }
    }
}
