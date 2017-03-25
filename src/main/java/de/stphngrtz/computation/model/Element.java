package de.stphngrtz.computation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class Element {

    public final Name name;
    public final Set<Definition> definitions;

    @SuppressWarnings("unused")
    private Element() {
        this.name = new Name();
        this.definitions = Collections.emptySet();
    }

    public Element(Name name, Set<Definition> definitions) {
        this.name = name;
        this.definitions = definitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element element = (Element) o;
        return Objects.equals(name, element.name) &&
                Objects.equals(definitions, element.definitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, definitions);
    }

    public static class Name {
        @JsonProperty
        private final String value;

        Name() {
            this.value = "";
        }

        public Name(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Name name = (Name) o;
            return Objects.equals(value, name.value);
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

    public static class Fields {
        public static final String name = "name";
        public static final String definitions = "definitions";
    }
}
