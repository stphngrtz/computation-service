package de.stphngrtz.computation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

public class Definition {

    public final Name name;
    public final BigDecimal value;

    @SuppressWarnings("unused")
    private Definition() {
        this.name = new Name();
        this.value = BigDecimal.ZERO;
    }

    public Definition(Name name, BigDecimal value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Definition that = (Definition) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
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
        public static final String value = "value";
    }
}
