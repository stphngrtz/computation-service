package de.stphngrtz.computation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.stphngrtz.computation.ComputationExpressionLanguageBaseVisitor;
import de.stphngrtz.computation.ComputationExpressionLanguageLexer;
import de.stphngrtz.computation.ComputationExpressionLanguageParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.bson.Document;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
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

        private ComputationExpressionLanguageParser parser;

        private ComputationExpressionLanguageParser parser() {
            if (parser == null)
                parser = new ComputationExpressionLanguageParser(new CommonTokenStream(new ComputationExpressionLanguageLexer(new ANTLRInputStream(value))));

            return parser;
        }

        public Optional<BigDecimal> evaluate(Function<String, BigDecimal> variableToValue) {
            if (Strings.isNullOrEmpty(value))
                return Optional.empty();

            try {
                return Optional.of(parser().expression().accept(new ExpressionEvaluationVisitor(variableToValue)));
            } catch (IllegalStateException e) {
                return Optional.empty();
            }
        }

        public List<String> getVariables() {
            if (Strings.isNullOrEmpty(value))
                return Collections.emptyList();

            return parser().expression().accept(new ExpressionVariablesVisitor());
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

        private static class ExpressionEvaluationVisitor extends ComputationExpressionLanguageBaseVisitor<BigDecimal> {

            private final Function<String, BigDecimal> variableToValue;

            private ExpressionEvaluationVisitor(Function<String, BigDecimal> variableToValue) {
                this.variableToValue = variableToValue;
            }

            @Override
            public BigDecimal visitExpression(ComputationExpressionLanguageParser.ExpressionContext ctx) {
                BigDecimal value = BigDecimal.ZERO;
                BiFunction<BigDecimal, BigDecimal, BigDecimal> operation = BigDecimal::add;

                for (int i = 0; i < ctx.getChildCount(); i++) {
                    ParseTree child = ctx.getChild(i);
                    if (i % 2 == 0) {
                        BigDecimal term = child.accept(new TermVisitor(variableToValue));
                        if (term == null)
                            throw new IllegalStateException();

                        value = operation.apply(value, term);
                    } else {
                        operation = child.accept(new OperationVisitor());
                        if (operation == null)
                            throw new IllegalStateException();
                    }
                }
                return value;
            }
        }

        private static class OperationVisitor extends ComputationExpressionLanguageBaseVisitor<BiFunction<BigDecimal, BigDecimal, BigDecimal>> {
            @Override
            public BiFunction<BigDecimal, BigDecimal, BigDecimal> visitTerminal(TerminalNode node) {
                Token symbol = node.getSymbol();
                switch (symbol.getType()) {
                    case ComputationExpressionLanguageLexer.PLUS:
                        return BigDecimal::add;
                    case ComputationExpressionLanguageLexer.MINUS:
                        return BigDecimal::subtract;
                }
                return null;
            }
        }

        private static class TermVisitor extends ComputationExpressionLanguageBaseVisitor<BigDecimal> {

            private final Function<String, BigDecimal> variableToValue;

            private TermVisitor(Function<String, BigDecimal> variableToValue) {
                this.variableToValue = variableToValue;
            }

            @Override
            public BigDecimal visitTerminal(TerminalNode node) {
                Token symbol = node.getSymbol();
                switch (symbol.getType()) {
                    case ComputationExpressionLanguageLexer.NUMBER:
                        return new BigDecimal(symbol.getText());
                    case ComputationExpressionLanguageLexer.VARIABLE:
                        return variableToValue.apply(symbol.getText());
                }
                return null;
            }
        }

        private static class ExpressionVariablesVisitor extends ComputationExpressionLanguageBaseVisitor<List<String>> {
            @Override
            public List<String> visitExpression(ComputationExpressionLanguageParser.ExpressionContext ctx) {
                return ctx.VARIABLE().stream().map(node -> node.getSymbol().getText()).collect(Collectors.toList());
            }
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
                } catch (IllegalAccessException e) {
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
