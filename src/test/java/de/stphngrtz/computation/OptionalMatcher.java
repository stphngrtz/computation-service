package de.stphngrtz.computation;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class OptionalMatcher {

    public static <T> Matcher<Optional<T>> present(Matcher<T> value) {
        return new TypeSafeMatcher<Optional<T>>() {
            @Override
            protected boolean matchesSafely(Optional<T> item) {
                return item.isPresent() && value.matches(item.get());
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(value);
            }
        };
    }

    public static <T> Matcher<Optional<T>> absent() {
        return new TypeSafeMatcher<Optional<T>>() {
            @Override
            protected boolean matchesSafely(Optional<T> item) {
                return !item.isPresent();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Value should not be present!");
            }
        };
    }
}
