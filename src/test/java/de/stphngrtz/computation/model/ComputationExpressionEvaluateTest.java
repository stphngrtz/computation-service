package de.stphngrtz.computation.model;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static de.stphngrtz.computation.BigDecimalMatcher.bigDecimal;
import static de.stphngrtz.computation.OptionalMatcher.absent;
import static de.stphngrtz.computation.OptionalMatcher.present;
import static org.hamcrest.MatcherAssert.assertThat;

public class ComputationExpressionEvaluateTest {

    @Test
    public void Evaluation_of_an_empty_expression() throws Exception {
        assertThat(new Computation.Expression().evaluate(variable -> null), absent());
        assertThat(new Computation.Expression("").evaluate(variable -> null), absent());
    }

    @Test
    public void Evaluation_of_a_single_numeric_term() throws Exception {
        assertThat(new Computation.Expression("2").evaluate(variable -> null), present(bigDecimal(2)));
    }

    @Test
    public void Evaluation_of_a_numeric_subtraction_with_two_terms() throws Exception {
        assertThat(new Computation.Expression("2 - 4").evaluate(variable -> null), present(bigDecimal(-2)));
    }

    @Test
    public void Evaluation_of_a_numeric_addition_with_two_terms() throws Exception {
        assertThat(new Computation.Expression("2 + 4").evaluate(variable -> null), present(bigDecimal(6)));
    }

    @Test
    public void Evaluation_of_a_numeric_expression_with_three_terms() throws Exception {
        assertThat(new Computation.Expression("2 + 4 - 5").evaluate(variable -> null), present(bigDecimal(1)));
    }

    @Test
    public void Evaluation_of_a_single_variable() throws Exception {
        assertThat(new Computation.Expression("var1").evaluate(variable -> new BigDecimal(2)), present(bigDecimal(2)));
    }

    @Test
    public void Evaluation_of_a_addition_with_two_variables() throws Exception {
        Map<String, BigDecimal> mapping = new HashMap<>();
        mapping.put("var1", new BigDecimal(2));
        mapping.put("var2", new BigDecimal(3));

        assertThat(new Computation.Expression("var1 + var2").evaluate(mapping::get), present(bigDecimal(5)));
    }

    @Test
    public void Evaluation_of_an_expression_with_two_variables_and_a_numeric_term() throws Exception {
        Map<String, BigDecimal> mapping = new HashMap<>();
        mapping.put("var1", new BigDecimal(2));
        mapping.put("var2", new BigDecimal(3));

        assertThat(new Computation.Expression("var1 + var2 - 4").evaluate(mapping::get), present(bigDecimal(1)));
    }

    @Test
    public void Evaluation_of_an_expression_with_missing_variable_mappings() throws Exception {
        Map<String, BigDecimal> mapping = new HashMap<>();
        mapping.put("var1", new BigDecimal(2));

        assertThat(new Computation.Expression("var1 + var2 - 4").evaluate(mapping::get), absent());
    }
}
