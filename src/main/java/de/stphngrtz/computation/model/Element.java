package de.stphngrtz.computation.model;

import java.util.Objects;
import java.util.Set;

public class Element {

    public final String name;
    public final Set<Definition> definitions;

    public Element(String name, Set<Definition> definitions) {
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

    public static class Fields {
        public static final String name = "name";
        public static final String definitions = "definitions";
    }
}
