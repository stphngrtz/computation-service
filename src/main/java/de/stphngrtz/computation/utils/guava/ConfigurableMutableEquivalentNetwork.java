package de.stphngrtz.computation.utils.guava;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableNetwork;

import java.util.Set;

public class ConfigurableMutableEquivalentNetwork<N, E> implements MutableNetwork<N, E> {

    private final MutableNetwork<N, E> network;

    ConfigurableMutableEquivalentNetwork(MutableNetwork<N, E> network) {
        this.network = network;
    }

    @Override
    public boolean addNode(N node) {
        return network.addNode(node);
    }

    @Override
    public boolean addEdge(N nodeU, N nodeV, E edge) {
        return network.addEdge(nodeU, nodeV, edge);
    }

    @Override
    public boolean removeNode(Object node) {
        return network.removeNode(node);
    }

    @Override
    public boolean removeEdge(Object edge) {
        return network.removeEdge(edge);
    }

    @Override
    public Set<N> nodes() {
        return network.nodes();
    }

    @Override
    public Set<E> edges() {
        return network.edges();
    }

    @Override
    public Graph<N> asGraph() {
        return network.asGraph();
    }

    @Override
    public boolean isDirected() {
        return network.isDirected();
    }

    @Override
    public boolean allowsParallelEdges() {
        return network.allowsParallelEdges();
    }

    @Override
    public boolean allowsSelfLoops() {
        return network.allowsSelfLoops();
    }

    @Override
    public ElementOrder<N> nodeOrder() {
        return network.nodeOrder();
    }

    @Override
    public ElementOrder<E> edgeOrder() {
        return network.edgeOrder();
    }

    @Override
    public Set<N> adjacentNodes(Object node) {
        return network.adjacentNodes(node);
    }

    @Override
    public Set<N> predecessors(Object node) {
        return network.predecessors(node);
    }

    @Override
    public Set<N> successors(Object node) {
        return network.successors(node);
    }

    @Override
    public Set<E> incidentEdges(Object node) {
        return network.incidentEdges(node);
    }

    @Override
    public Set<E> inEdges(Object node) {
        return network.inEdges(node);
    }

    @Override
    public Set<E> outEdges(Object node) {
        return network.outEdges(node);
    }

    @Override
    public int degree(Object node) {
        return network.degree(node);
    }

    @Override
    public int inDegree(Object node) {
        return network.inDegree(node);
    }

    @Override
    public int outDegree(Object node) {
        return network.outDegree(node);
    }

    @Override
    public EndpointPair<N> incidentNodes(Object edge) {
        return network.incidentNodes(edge);
    }

    @Override
    public Set<E> adjacentEdges(Object edge) {
        return network.adjacentEdges(edge);
    }

    @Override
    public Set<E> edgesConnecting(Object nodeU, Object nodeV) {
        return network.edgesConnecting(nodeU, nodeV);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurableMutableEquivalentNetwork<?, ?> that = (ConfigurableMutableEquivalentNetwork<?, ?>) o;
        return Networks.equivalent(this.network, that.network);
    }

    @Override
    public int hashCode() {
        Multimap<E, N> multimap = HashMultimap.create();
        network.edges().forEach(edge -> {
            EndpointPair<N> endpointPair = network.incidentNodes(edge);
            multimap.put(edge, endpointPair.source());
            multimap.put(edge, endpointPair.target());
        });
        return multimap.hashCode();
    }
}
