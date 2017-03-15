package de.stphngrtz.computation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.graph.Graph;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Structure {

    public final Id id;
    public final Graph<Element> elements;

    public Structure(Id id, Graph<Element> elements) {
        this.id = id;
        this.elements = elements;
    }

    public Structure with(Id id) {
        return new Structure(id, elements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Structure structure = (Structure) o;
        return Objects.equals(id, structure.id) &&
                Objects.equals(elements, structure.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, elements);
    }

    public static class Id implements Comparable<Id> {
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
            Id that = (Id) o;
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
        public int compareTo(Id o) {
            return id.compareTo(o.id);
        }
    }

    public static MongoCollection<Structure> collection(MongoDatabase database) {
        return rawCollection(database).withDocumentClass(Structure.class);
    }

    public static MongoCollection<Document> rawCollection(MongoDatabase database) {
        return database.getCollection("structures");
    }

    public static class Fields {
        public static final String id = "_id";
        public static final String elements = "elements";

        public static BiMap<String, String> asMap(Predicate<String> filter) {
            return HashBiMap.create(Stream.of(Fields.class.getFields()).filter(field -> filter.test(field.getName())).collect(Collectors.toMap(Field::getName, field -> {
                try {
                    return (String) field.get(null);
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            })));
        }
    }
}
