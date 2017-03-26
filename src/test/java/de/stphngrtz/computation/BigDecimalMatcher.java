package de.stphngrtz.computation;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalMatcher {

    private static final int SCALE = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static Matcher<BigDecimal> bigDecimal(long value) {
        return bigDecimal(new BigDecimal(value));
    }

    public static Matcher<BigDecimal> bigDecimal(double value) {
        return bigDecimal(new BigDecimal(value));
    }

    public static Matcher<BigDecimal> bigDecimal(BigDecimal value) {
        return new TypeSafeMatcher<BigDecimal>() {

            @Override
            protected boolean matchesSafely(BigDecimal item) {
                return value.setScale(SCALE, ROUNDING_MODE).compareTo(item.setScale(SCALE, ROUNDING_MODE)) == 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(value);
            }
        };
    }
}
