package de.stphngrtz.computation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Computation {

    public final Id id;
    public final Structure.Id structureId;
    public final Element.Name elementName;
    public final Expression expression;
    public final Status status;
    public final Optional<BigDecimal> result;

    @SuppressWarnings("unused")
    private Computation() {
        this.id = new Id();
        this.structureId = new Structure.Id();
        this.elementName = new Element.Name();
        this.expression = new Expression();
        this.status = Status.NEW;
        this.result = Optional.empty();
    }

    public Computation(Id id, Structure.Id structureId, Element.Name elementName, Expression expression) {
        this.id = id;
        this.structureId = structureId;
        this.elementName = elementName;
        this.expression = expression;
        this.status = Status.NEW;
        this.result = Optional.empty();
    }

    private Computation(Id id, Structure.Id structureId, Element.Name elementName, Expression expression, Status status, Optional<BigDecimal> result) {
        this.id = id;
        this.structureId = structureId;
        this.elementName = elementName;
        this.expression = expression;
        this.status = status;
        this.result = result;
    }

    public Computation with(Id id) {
        return new Computation(id, structureId, elementName, expression, status, result);
    }

    public Computation with(Status status) {
        return new Computation(id, structureId, elementName, expression, status, result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Computation that = (Computation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(structureId, that.structureId) &&
                Objects.equals(elementName, that.elementName) &&
                Objects.equals(expression, that.expression) &&
                status == that.status &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, structureId, elementName, expression, status, result);
    }

    public static class Id implements Comparable<Computation.Id> {
        @JsonProperty
        private final String id;

        public Id() {
            this.id = UUID.randomUUID().toString();
        }

        public Id(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Computation.Id that = (Computation.Id) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return id;
        }

        @Override
        public int compareTo(Computation.Id o) {
            return id.compareTo(o.id);
        }
    }

    public static class Expression {
        @JsonProperty
        private final String value;

        Expression() {
            this.value = "";
        }

        public Expression(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Expression that = (Expression) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum Status {
        NEW,
        COMPUTING,
        FAILED,
        DONE;
    }

    public static MongoCollection<Computation> collection(MongoDatabase database) {
        return rawCollection(database).withDocumentClass(Computation.class);
    }

    public static MongoCollection<Document> rawCollection(MongoDatabase database) {
        return database.getCollection("computations");
    }

    public static class Fields {
        public static final String id = "_id";
        public static final String structureId = "structureId";
        public static final String elementName = "elementName";
        public static final String expression = "expression";

        public static BiMap<String, String> asMap(Predicate<String> filter) {
            return HashBiMap.create(Stream.of(Computation.Fields.class.getFields()).filter(field -> filter.test(field.getName())).collect(Collectors.toMap(Field::getName, field -> {
                try {
                    return (String) field.get(null);
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            })));
        }
    }

    // structure
    // element
    // 3 * 4 + {definition.name} / {definition.name}

    // was passiert, wenn eine solche frage ins system kommt=?
    // 1. structure ermitteln
    //    => wenn nicht vorhanden, dann fehler
    // 2. element ermitteln
    //    => wenn nicht vorhanden, dann fehler
    // 3. ausdruck auswerten
    // 4. wenn definition nicht an element vorhanden, dann in darunter liegenden elementen suchen, werte summieren
    //    => wenn nicht vorhanden, dann fehler
}
