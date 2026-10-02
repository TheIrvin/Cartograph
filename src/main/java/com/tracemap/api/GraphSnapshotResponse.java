package com.tracemap.api;

import com.tracemap.graph.model.GraphEdge;
import com.tracemap.graph.model.GraphMetrics;
import com.tracemap.graph.model.GraphNode;
import com.tracemap.graph.model.GraphSnapshot;
import com.tracemap.graph.model.GraphWarning;

import java.util.List;

public record GraphSnapshotResponse(String repository, String commitSha, List<GraphNode> nodes,
        List<GraphEdge> edges, List<GraphWarning> warnings, Metrics metrics) {
    public static GraphSnapshotResponse from(GraphSnapshot snapshot) {
        return new GraphSnapshotResponse(snapshot.repository(), snapshot.commitSha(), snapshot.nodes(),
                snapshot.edges(), snapshot.warnings(), Metrics.from(snapshot.metrics()));
    }

    public record Metrics(int filesSeen, int filesParsed, int nodes, int edges) {
        static Metrics from(GraphMetrics metrics) {
            return new Metrics(metrics.filesSeen(), metrics.filesParsed(), metrics.nodeCount(), metrics.edgeCount());
        }
    }
}
