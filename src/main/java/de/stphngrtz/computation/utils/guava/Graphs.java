package de.stphngrtz.computation.utils.guava;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.util.*;

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

    /**
     * Builder für Graphs
     */
    public static <N> Graph<N> builder(Graphs.Builder.Node<N> node) {
        MutableGraph<N> graph = Graphs.newGraph();
        node.into(graph);
        return graph;
    }

    /**
     * Marker-Interface für {@link Node}
     */
    public interface Builder {

        class Node<N> {
            private final N n;
            private final List<Node<N>> nodes;

            Node(N n, List<Node<N>> nodes) {
                this.n = n;
                this.nodes = nodes;
            }

            void into(MutableGraph<N> graph) {
                if (nodes.isEmpty())
                    graph.addNode(n);
                else {
                    nodes.forEach(node -> node.into(graph, n));
                }
            }

            void into(MutableGraph<N> graph, N n) {
                graph.putEdge(n, this.n);
                nodes.forEach(node -> node.into(graph, this.n));
            }
        }

        @SafeVarargs
        static <N> Node<N> node(N n, Node<N>... nodes) {
            return new Node<>(n, Arrays.asList(nodes));
        }
    }
}
