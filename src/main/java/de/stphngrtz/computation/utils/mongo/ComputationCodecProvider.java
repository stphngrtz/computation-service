package de.stphngrtz.computation.utils.mongo;

import com.google.common.graph.Graph;
import de.stphngrtz.computation.model.Definition;
import de.stphngrtz.computation.model.Element;
import de.stphngrtz.computation.model.Structure;
import org.bson.BsonType;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ComputationCodecProvider implements CodecProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> Codec<T> get(Class<T> aClass, CodecRegistry codecRegistry) {
        if (Objects.equals(aClass, Structure.class))
            return (Codec<T>) new StructureCodec(codecRegistry);
        if (Objects.equals(aClass, Structure.Id.class))
            return (Codec<T>) new StructureIdCodec();
        if (Objects.equals(aClass, Element.class))
            return (Codec<T>) new ElementCodec(codecRegistry);
        if (Objects.equals(aClass, Definition.class))
            return (Codec<T>) new DefinitionCodec(codecRegistry);
        return null;
    }

    private class StructureCodec extends ToDocumentWithIdCodec<Structure, Structure.Id> {
        StructureCodec(CodecRegistry codecRegistry) {
            super(codecRegistry.get(Structure.Id.class));
            Codec<Graph<Element>> graphCodec = new GraphCodec<>(codecRegistry.get(Element.class));

            register((writer, value, encoderContext) -> {
                writer.writeName(Structure.Fields.elements);
                encoderContext.encodeWithChildContext(graphCodec, writer, value.elements);
            });
            register((reader, decoderContext, id) -> {
                reader.readName(Structure.Fields.elements);
                Graph<Element> elements = graphCodec.decode(reader, decoderContext);
                return new Structure(id, elements);
            }, 1);
        }

        @Override
        protected Structure.Id id(Structure value) {
            return value.id;
        }

        @Override
        protected int version() {
            return 1;
        }
    }

    private class StructureIdCodec extends ToStringCodec<Structure.Id> {
        @Override
        protected String toString(Structure.Id value) {
            return value.toString();
        }

        @Override
        protected Structure.Id fromString(String value) {
            return new Structure.Id(value);
        }
    }

    private class ElementCodec extends ToDocumentCodec<Element> {
        ElementCodec(CodecRegistry codecRegistry) {
            Codec<Definition> definitionCodec = codecRegistry.get(Definition.class);

            register((writer, value, encoderContext) -> {
                writer.writeString(Element.Fields.name, value.name);

                writer.writeStartArray(Element.Fields.definitions);
                value.definitions.forEach(definition -> encoderContext.encodeWithChildContext(definitionCodec, writer, definition));
                writer.writeEndArray();
            });
            register((reader, decoderContext) -> {
                String name = reader.readString(Element.Fields.name);

                reader.readName(Element.Fields.definitions);
                Set<Definition> definitions = new HashSet<>();
                reader.readStartArray();
                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    definitions.add(definitionCodec.decode(reader, decoderContext));
                }
                reader.readEndArray();

                return new Element(name, definitions);
            }, 1);
        }

        @Override
        protected int version() {
            return 1;
        }
    }

    private class DefinitionCodec extends ToDocumentCodec<Definition> {
        DefinitionCodec(CodecRegistry codecRegistry) {
            Codec<BigDecimal> bigDecimalCodec = codecRegistry.get(BigDecimal.class);

            register((writer, value, encoderContext) -> {
                writer.writeString(Definition.Fields.name, value.name);

                writer.writeName(Definition.Fields.value);
                encoderContext.encodeWithChildContext(bigDecimalCodec, writer, value.value);
            });
            register((reader, decoderContext) -> {
                String name = reader.readString(Definition.Fields.name);

                reader.readName(Definition.Fields.value);
                BigDecimal value = bigDecimalCodec.decode(reader, decoderContext);

                return new Definition(name, value);
            }, 1);
        }

        @Override
        protected int version() {
            return 1;
        }
    }
}
