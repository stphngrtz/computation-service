package de.stphngrtz.computation.model;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class ComputationExpressionGetVariablesTest {

    @Test
    public void Variables_of_an_empty_expression() throws Exception {
        assertThat(new Computation.Expression().getVariables(), equalTo(Collections.emptyList()));
        assertThat(new Computation.Expression("").getVariables(), equalTo(Collections.emptyList()));
    }

    @Test
    public void Variables_of_a_single_numeric_term() throws Exception {
        assertThat(new Computation.Expression("2").getVariables(), equalTo(Collections.emptyList()));
    }

    @Test
    public void Variables_of_a_numeric_expression_with_two_terms() throws Exception {
        assertThat(new Computation.Expression("2 - 4").getVariables(), equalTo(Collections.emptyList()));
    }

    @Test
    public void Variables_of_a_single_variable_expression() throws Exception {
        assertThat(new Computation.Expression("var1").getVariables(), contains("var1"));
    }

    @Test
    public void Variables_of_an_expression_with_two_variables() throws Exception {
        assertThat(new Computation.Expression("var1 + var2").getVariables(), contains("var1", "var2"));
    }

    @Test
    public void Variables_of_an_expression_with_two_variables_and_a_numeric_term() throws Exception {
        assertThat(new Computation.Expression("var1 + var2 - 4").getVariables(), contains("var1", "var2"));
    }
}
