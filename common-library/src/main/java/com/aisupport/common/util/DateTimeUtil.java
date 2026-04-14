package com.aisupport.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility helpers for consistently handling date-time values in UTC and
 * project-standard string format.
 */
public class DateTimeUtil {
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private DateTimeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Formats a {@link LocalDateTime} using the project default pattern.
     *
     * @param dateTime date-time to format
     * @return formatted text or {@code null} when input is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : null;
    }
    
    /**
     * Returns current UTC date-time.
     *
     * @return current UTC date-time
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }
    
    /**
     * Parses date-time text using the project default pattern.
     *
     * @param dateTimeString date-time text
     * @return parsed {@link LocalDateTime}
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DEFAULT_FORMATTER);
    }

}
