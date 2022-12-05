package de.hpi.dbs2.exercise2.utils;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamUtils {
    public static <T> Stream<T> reverseStream(T[] array) {
        return reverseRange(array.length, 0)
            .mapToObj(i -> array[i]);
    }

    /**
     * stream will be empty if startExclusive <= endInclusive
     */
    public static IntStream reverseRange(int startExclusive, int endInclusive) {
        if(startExclusive <= endInclusive)
            return IntStream.empty();
        return IntStream.iterate(startExclusive - 1, i -> i >= endInclusive, i -> i - 1);
    }
}
