package de.stphngrtz.computation.utils.mongo;

import com.google.common.graph.Graph;
import de.stphngrtz.computation.model.Computation;
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
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (Objects.equals(clazz, Structure.class))
            return (Codec<T>) new StructureCodec(registry);
        if (Objects.equals(clazz, Structure.Id.class))
            return (Codec<T>) new StructureIdCodec();
        if (Objects.equals(clazz, Element.class))
            return (Codec<T>) new ElementCodec(registry);
        if (Objects.equals(clazz, Element.Name.class))
            return (Codec<T>) new ElementNameCodec();
        if (Objects.equals(clazz, Definition.class))
            return (Codec<T>) new DefinitionCodec(registry);
        if (Objects.equals(clazz, Definition.Name.class))
            return (Codec<T>) new DefinitionNameCodec();
        if (Objects.equals(clazz, Computation.class))
            return (Codec<T>) new ComputationCodec(registry);
        if (Objects.equals(clazz, Computation.Id.class))
            return (Codec<T>) new ComputationIdCodec();
        if (Objects.equals(clazz, Computation.Expression.class))
            return (Codec<T>) new ComputationExpressionCodec();

        return null;
    }

    private class StructureCodec extends ToDocumentWithIdCodec<Structure, Structure.Id> {
        StructureCodec(CodecRegistry registry) {
            super(registry.get(Structure.Id.class));
            Codec<Graph<Element>> graphCodec = new GraphCodec<>(registry.get(Element.class));

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
        ElementCodec(CodecRegistry registry) {
            Codec<Element.Name> nameCodec = registry.get(Element.Name.class);
            Codec<Definition> definitionCodec = registry.get(Definition.class);

            register((writer, value, encoderContext) -> {
                writer.writeName(Element.Fields.name);
                encoderContext.encodeWithChildContext(nameCodec, writer, value.name);

                writer.writeStartArray(Element.Fields.definitions);
                value.definitions.forEach(definition -> encoderContext.encodeWithChildContext(definitionCodec, writer, definition));
                writer.writeEndArray();
            });
            register((reader, decoderContext) -> {
                reader.readName(Element.Fields.name);
                Element.Name name = nameCodec.decode(reader, decoderContext);

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

    private class ElementNameCodec extends ToStringCodec<Element.Name> {

        @Override
        String toString(Element.Name value) {
            return value.toString();
        }

        @Override
        Element.Name fromString(String value) {
            return new Element.Name(value);
        }
    }

    private class DefinitionCodec extends ToDocumentCodec<Definition> {
        DefinitionCodec(CodecRegistry registry) {
            Codec<Definition.Name> nameCodec = registry.get(Definition.Name.class);
            Codec<BigDecimal> bigDecimalCodec = registry.get(BigDecimal.class);

            register((writer, value, encoderContext) -> {
                writer.writeName(Definition.Fields.name);
                encoderContext.encodeWithChildContext(nameCodec, writer, value.name);

                writer.writeName(Definition.Fields.value);
                encoderContext.encodeWithChildContext(bigDecimalCodec, writer, value.value);
            });
            register((reader, decoderContext) -> {
                reader.readName(Definition.Fields.name);
                Definition.Name name = nameCodec.decode(reader, decoderContext);

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

    private class DefinitionNameCodec extends ToStringCodec<Definition.Name> {

        @Override
        String toString(Definition.Name value) {
            return value.toString();
        }

        @Override
        Definition.Name fromString(String value) {
            return new Definition.Name(value);
        }
    }

    private class ComputationCodec extends ToDocumentWithIdCodec<Computation, Computation.Id> {

        ComputationCodec(CodecRegistry registry) {
            super(registry.get(Computation.Id.class));
            Codec<Structure.Id> structureIdCodec = registry.get(Structure.Id.class);
            Codec<Element.Name> elementNameCodec = registry.get(Element.Name.class);
            Codec<Computation.Expression> expressionCodec = registry.get(Computation.Expression.class);

            register((writer, value, encoderContext) -> {
                writer.writeName(Computation.Fields.structureId);
                encoderContext.encodeWithChildContext(structureIdCodec, writer, value.structureId);

                writer.writeName(Computation.Fields.elementName);
                encoderContext.encodeWithChildContext(elementNameCodec, writer, value.elementName);

                writer.writeName(Computation.Fields.expression);
                encoderContext.encodeWithChildContext(expressionCodec, writer, value.expression);
            });
            register((reader, decoderContext, id) -> {
                reader.readName(Computation.Fields.structureId);
                Structure.Id structureId = structureIdCodec.decode(reader, decoderContext);

                reader.readName(Computation.Fields.elementName);
                Element.Name elementName = elementNameCodec.decode(reader, decoderContext);

                reader.readName(Computation.Fields.expression);
                Computation.Expression expression = expressionCodec.decode(reader, decoderContext);

                return new Computation(id, structureId, elementName, expression);
            }, 1);
        }

        @Override
        protected Computation.Id id(Computation value) {
            return value.id;
        }

        @Override
        protected int version() {
            return 1;
        }
    }

    private class ComputationIdCodec extends ToStringCodec<Computation.Id> {
        @Override
        protected String toString(Computation.Id value) {
            return value.toString();
        }

        @Override
        protected Computation.Id fromString(String value) {
            return new Computation.Id(value);
        }
    }

    private class ComputationExpressionCodec extends ToStringCodec<Computation.Expression> {
        @Override
        protected String toString(Computation.Expression value) {
            return value.toString();
        }

        @Override
        protected Computation.Expression fromString(String value) {
            return new Computation.Expression(value);
        }
    }
}
