package de.hpi.dbs2.exercise2.utils;

import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class AssertionUtils {
    public static <T> void assertStreamEquals(
        Stream<T> expected,
        Stream<T> actual
    ) {
        Iterator<T> iterE = expected.iterator();
        Iterator<T> iterA = actual.iterator();
        int i = 0;
        while (iterE.hasNext() && iterA.hasNext()) {
            T expectedEntry = iterE.next();
            T actualEntry = iterA.next();
            if(!Objects.equals(expectedEntry, actualEntry)) {
                throw new AssertionFailedError(
                    String.format(
                        "stream contents differ at index [%d], expected: <%s> but was: <%s>",
                        i, expectedEntry, actualEntry
                    ),
                    expectedEntry, actualEntry
                );
            }
            i++;
        }
        Assertions.assertFalse(iterE.hasNext(), "expected stream is longer than actual stream");
        Assertions.assertFalse(iterA.hasNext(), "actual stream is longer than expected stream");
    }
}
