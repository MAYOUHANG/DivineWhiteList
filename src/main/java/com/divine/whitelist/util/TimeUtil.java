package com.divine.whitelist.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private TimeUtil() {
    }

    public static String now() {
        return ZonedDateTime.now().format(FORMATTER);
    }
}
