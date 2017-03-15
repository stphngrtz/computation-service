package de.stphngrtz.computation.utils.guava;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableGraph;

import java.util.Objects;
import java.util.Set;

public class ConfigurableMutableEquivalentGraph<N> implements MutableGraph<N> {


    private final MutableGraph<N> graph;

    ConfigurableMutableEquivalentGraph(MutableGraph<N> graph) {
        this.graph = graph;
    }

    @Override
    public boolean addNode(N node) {
        return graph.addNode(node);
    }

    @Override
    public boolean putEdge(N nodeU, N nodeV) {
        return graph.putEdge(nodeU, nodeV);
    }

    @Override
    public boolean removeNode(Object node) {
        return graph.removeNode(node);
    }

    @Override
    public boolean removeEdge(Object nodeU, Object nodeV) {
        return graph.removeEdge(nodeU, nodeV);
    }

    @Override
    public Set<N> nodes() {
        return graph.nodes();
    }

    @Override
    public Set<EndpointPair<N>> edges() {
        return graph.edges();
    }

    @Override
    public boolean isDirected() {
        return graph.isDirected();
    }

    @Override
    public boolean allowsSelfLoops() {
        return graph.allowsSelfLoops();
    }

    @Override
    public ElementOrder<N> nodeOrder() {
        return graph.nodeOrder();
    }

    @Override
    public Set<N> adjacentNodes(Object node) {
        return graph.adjacentNodes(node);
    }

    @Override
    public Set<N> predecessors(Object node) {
        return graph.predecessors(node);
    }

    @Override
    public Set<N> successors(Object node) {
        return graph.successors(node);
    }

    @Override
    public int degree(Object node) {
        return graph.degree(node);
    }

    @Override
    public int inDegree(Object node) {
        return graph.inDegree(node);
    }

    @Override
    public int outDegree(Object node) {
        return graph.outDegree(node);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurableMutableEquivalentGraph<?> that = (ConfigurableMutableEquivalentGraph<?>) o;
        return Graphs.equivalent(this.graph, that.graph);
    }

    @Override
    public int hashCode() {
        return Objects.hash(graph.nodes().toArray());
    }
}
