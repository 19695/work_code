package org.colm.code;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public class RecordUtil {

    public static <R> R recordSpendTime(Supplier<R> supplier) {
        Instant start = Instant.now();
        R result = supplier.get();
        Instant end = Instant.now();
        log(MessageFormat.format("Method start time:{0}, Method end time:{1}, Method spend time:{2}", start, end, Duration.between(start, end)));
        return result;
    }

    private static void log(String msg) {

    }

}
