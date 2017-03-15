package de.stphngrtz.computation.utils.guava;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Graphs {

    public static <N> MutableGraph<N> newGraph() {
        return new ConfigurableMutableEquivalentGraph<N>(GraphBuilder.directed().build());
    }

    public static <N> Set<Edge<N>> getEdges(Graph<N> graph) {
        Set<Edge<N>> edges = new HashSet<>();
        graph.nodes().stream().filter(node -> graph.inDegree(node) == 0).forEach(node -> edges.addAll(getEdges(graph, node)));
        return edges;
    }

    public static <N> Set<Edge<N>> getEdges(Graph<N> graph, N node) {
        Set<Edge<N>> edges = new HashSet<>();
        graph.successors(node).forEach(successor -> {
            edges.add(new Edge<>(node, successor));
            edges.addAll(getEdges(graph, successor));
        });
        return edges;
    }

    public static boolean equivalent(Graph<?> graphA, Graph<?> graphB) {
        return com.google.common.graph.Graphs.equivalent(graphA, graphB);
    }

    public static class Edge<N> {
        public final N source;
        public final N target;

        public Edge(N source, N target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge<?> edge = (Edge<?>) o;
            return Objects.equals(source, edge.source) &&
                    Objects.equals(target, edge.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target);
        }
    }
}
