package de.stphngrtz.computation.utils.mongo;

import com.google.common.graph.Graph;
import com.google.common.graph.MutableGraph;
import de.stphngrtz.computation.utils.guava.Graphs;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.ArrayList;
import java.util.List;

public class GraphCodec<N> implements Codec<Graph<N>> {

    private final Codec<N> nodeCodec;

    GraphCodec(Codec<N> nodeCodec) {
        this.nodeCodec = nodeCodec;
    }

    @Override
    public Graph<N> decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        reader.readStartArray();
        List<N> nodes = new ArrayList<>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            nodes.add(nodeCodec.decode(reader, decoderContext));
        }
        reader.readEndArray();
        reader.readStartArray();
        MutableGraph<N> graph = Graphs.newGraph();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            reader.readStartDocument();
            graph.putEdge(nodes.get(reader.readInt32("source")), nodes.get(reader.readInt32("target")));
            reader.readEndDocument();
        }
        reader.readEndArray();
        reader.readEndDocument();
        return graph;
    }

    @Override
    public void encode(BsonWriter writer, Graph<N> value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeStartArray("nodes");
        ArrayList<N> nodes = new ArrayList<>(value.nodes());
        nodes.forEach(node -> encoderContext.encodeWithChildContext(nodeCodec, writer, node));
        writer.writeEndArray();

        writer.writeStartArray("edges");
        Graphs.getEdges(value).forEach(edge -> {
            writer.writeStartDocument();
            writer.writeInt32("source", nodes.indexOf(edge.source));
            writer.writeInt32("target", nodes.indexOf(edge.target));
            writer.writeEndDocument();
        });
        writer.writeEndArray();
        writer.writeEndDocument();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Graph<N>> getEncoderClass() {
        return (Class) Graph.class;
    }
}
