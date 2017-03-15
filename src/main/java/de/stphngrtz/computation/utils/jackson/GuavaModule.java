package de.stphngrtz.computation.utils.jackson;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.graph.*;
import de.stphngrtz.computation.utils.guava.Graphs;
import de.stphngrtz.computation.utils.guava.Networks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class GuavaModule extends SimpleModule {

    GuavaModule() {
        super("GuavaExtendedModule", new Version(1, 0, 0, "SNAPSHOT", "de.stphngrtz", "computation-service"));
        addSerializer(Graph.class, new GraphSerializer());
        addSerializer(Network.class, new NetworkSerializer());
        addDeserializer(Graph.class, new GraphDeserializer(Object.class));
        addDeserializer(Network.class, new NetworkDeserializer(Object.class, Object.class));
    }

    private class GraphSerializer extends JsonSerializer<Graph> {
        @Override
        public void serialize(Graph graph, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            List<?> nodes = new ArrayList<>(((Graph<?>) graph).nodes());

            jsonGenerator.writeStartObject();

            jsonGenerator.writeFieldName("nodes");
            jsonGenerator.writeStartArray();
            for (Object node : nodes) {
                jsonGenerator.writeObject(node);
            }
            jsonGenerator.writeEndArray();

            jsonGenerator.writeFieldName("edges");
            jsonGenerator.writeStartArray();

            for (Graphs.Edge<?> edge : Graphs.getEdges((Graph<?>) graph)) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectField("source", nodes.indexOf(edge.source));
                jsonGenerator.writeObjectField("target", nodes.indexOf(edge.target));
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
    }

    private static class GraphDeserializer extends JsonDeserializer<Graph> implements ContextualDeserializer {
        private final Class<?> nodeType;

        GraphDeserializer(Class<?> nodeType) {
            this.nodeType = nodeType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Graph deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            MutableGraph graph = Graphs.newGraph();

            ObjectCodec codec = p.getCodec();
            TreeNode rootTreeNode = codec.readTree(p);

            if (!rootTreeNode.isObject())
                throw new IllegalStateException("Object expected!");

            TreeNode nodesTreeNode = rootTreeNode.get("nodes");
            if (!nodesTreeNode.isArray())
                throw new IllegalStateException("Array expected!");

            List<Object> nodes = new ArrayList<>();

            int i = 0;
            TreeNode nodesTreeSubnode;
            while ((nodesTreeSubnode = nodesTreeNode.get(i++)) != null) {
                JsonParser node = nodesTreeSubnode.traverse(codec);
                node.nextToken();
                nodes.add(ctxt.readValue(node, nodeType));
            }

            TreeNode edgesTreeNode = rootTreeNode.get("edges");
            if (!edgesTreeNode.isArray())
                throw new IllegalStateException("Array expected!");

            int j = 0;
            TreeNode edgesTreeSubnode;
            while ((edgesTreeSubnode = edgesTreeNode.get(j++)) != null) {
                if (!edgesTreeSubnode.isObject())
                    throw new IllegalStateException("Object expected!");

                JsonParser source = edgesTreeSubnode.get("source").traverse(codec);
                source.nextToken();
                JsonParser target = edgesTreeSubnode.get("target").traverse(codec);
                target.nextToken();

                graph.putEdge(
                        nodes.get(ctxt.readValue(source, Integer.class)),
                        nodes.get(ctxt.readValue(target, Integer.class))
                );
            }

            nodes.removeAll(graph.nodes());
            nodes.forEach(graph::addNode);

            return graph;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            if (property == null)
                return this;

            JavaType type = property.getType();
            if (type.containedTypeCount() != 1)
                throw new IllegalStateException("One contained type expected!");

            Class<?> node = type.containedType(0).getRawClass();
            return new GraphDeserializer(node);
        }
    }

    private class NetworkSerializer extends JsonSerializer<Network> {
        @Override
        public void serialize(Network network, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            List<?> nodes = new ArrayList<>(((Network<?, ?>) network).nodes());

            jsonGenerator.writeStartObject();

            jsonGenerator.writeFieldName("nodes");
            jsonGenerator.writeStartArray();
            for (Object node : nodes) {
                jsonGenerator.writeObject(node);
            }
            jsonGenerator.writeEndArray();

            jsonGenerator.writeFieldName("edges");
            jsonGenerator.writeStartArray();
            for (Object edge : network.edges()) {
                EndpointPair endpointPair = network.incidentNodes(edge);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectField("source", nodes.indexOf(endpointPair.source()));
                jsonGenerator.writeObjectField("edge", edge);
                jsonGenerator.writeObjectField("target", nodes.indexOf(endpointPair.target()));
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
    }

    private static class NetworkDeserializer extends JsonDeserializer<Network> implements ContextualDeserializer {
        private final Class<?> edgeType;
        private final Class<?> nodeType;

        NetworkDeserializer(Class<?> edgeType, Class<?> nodeType) {
            this.edgeType = edgeType;
            this.nodeType = nodeType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Network deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            MutableNetwork network = Networks.newNetwork();

            ObjectCodec codec = p.getCodec();
            TreeNode rootTreeNode = codec.readTree(p);

            if (!rootTreeNode.isObject())
                throw new IllegalStateException("Object expected!");

            TreeNode nodesTreeNode = rootTreeNode.get("nodes");
            if (!nodesTreeNode.isArray())
                throw new IllegalStateException("Array expected!");

            List<Object> nodes = new ArrayList<>();

            int i = 0;
            TreeNode nodesTreeSubnode;
            while ((nodesTreeSubnode = nodesTreeNode.get(i++)) != null) {
                JsonParser node = nodesTreeSubnode.traverse(codec);
                node.nextToken();
                nodes.add(ctxt.readValue(node, nodeType));
            }

            TreeNode edgesTreeNode = rootTreeNode.get("edges");
            if (!edgesTreeNode.isArray())
                throw new IllegalStateException("Array expected!");

            int j = 0;
            TreeNode edgesTreeSubnode;
            while ((edgesTreeSubnode = edgesTreeNode.get(j++)) != null) {
                if (!edgesTreeSubnode.isObject())
                    throw new IllegalStateException("Object expected!");

                JsonParser source = edgesTreeSubnode.get("source").traverse(codec);
                source.nextToken();
                JsonParser edge = edgesTreeSubnode.get("edge").traverse(codec);
                edge.nextToken();
                JsonParser target = edgesTreeSubnode.get("target").traverse(codec);
                target.nextToken();

                network.addEdge(
                        nodes.get(ctxt.readValue(source, Integer.class)),
                        nodes.get(ctxt.readValue(target, Integer.class)),
                        ctxt.readValue(edge, edgeType)
                );
            }

            nodes.removeAll(network.nodes());
            nodes.forEach(network::addNode);

            return network;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            if (property == null)
                return this;

            JavaType type = property.getType();
            if (type.containedTypeCount() != 2)
                throw new IllegalStateException("Contained type count of 2 expected!");

            Class<?> edge = type.containedType(1).getRawClass();
            Class<?> node = type.containedType(0).getRawClass();
            return new NetworkDeserializer(edge, node);
        }
    }
}
