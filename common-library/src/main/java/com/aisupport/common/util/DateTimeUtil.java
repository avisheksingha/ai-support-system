package com.aisupport.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Utility helpers for consistently handling date-time values.
 * Core representation is {@link Instant} (UTC) to prevent timezone ambiguity.
 */
public class DateTimeUtil {
    
    /**
     * ISO-8601 format (e.g., "2023-10-27T13:45:12.123Z"). 
     * This is the industry standard for APIs and system logs.
     */
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    
    /**
     * Legacy project format. Used ONLY for backward compatibility with 
     * older databases or specific UI requirements.
     */
    private static final DateTimeFormatter LEGACY_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private DateTimeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // ---------------------------------------------------------
    // 1. GETTING CURRENT TIME
    // ---------------------------------------------------------
    
    /**
     * Returns the current moment in time (always UTC).
     * Replaces the unsafe LocalDateTime.now(ZoneId.of("UTC")).
     */
    public static Instant now() {
        return Instant.now();
    }
    
    // ---------------------------------------------------------
    // 2. FORMATTING (Instant -> String)
    // ---------------------------------------------------------
    
    /**
     * Formats an {@link Instant} to standard ISO-8601 UTC string.
     * Highly recommended for JSON responses and logs.
     *
     * @param instant the moment in time
     * @return ISO formatted string, or {@code null} if input is null
     */
    public static String formatIso(Instant instant) {
        return instant != null ? ISO_FORMATTER.format(instant) : null;
    }

    /**
     * Formats an {@link Instant} using the legacy project pattern.
     * WARNING: The output string will NOT contain timezone info ('Z').
     * Only use this if a legacy system strictly requires "yyyy-MM-dd HH:mm:ss".
     *
     * @param instant the moment in time
     * @return legacy formatted string, or {@code null} if input is null
     */
    public static String formatLegacy(Instant instant) {
        if (instant == null) return null;
        // We must temporarily apply UTC to the formatter so it knows how to extract the date/time safely
        return LEGACY_FORMATTER.format(instant.atOffset(ZoneOffset.UTC));
    }
    
    // ---------------------------------------------------------
    // 3. PARSING (String -> Instant)
    // ---------------------------------------------------------
    
    /**
     * Parses an ISO-8601 string (e.g., "2023-10-27T13:45:12Z") into an Instant.
     *
     * @param isoString the ISO formatted string
     * @return parsed {@link Instant}
     */
    public static Instant parseIso(String isoString) {
        return isoString != null ? Instant.parse(isoString) : null;
    }

    /**
     * Parses a legacy string ("yyyy-MM-dd HH:mm:ss") safely into an Instant.
     * Because the string lacks timezone info, we EXPLICITLY enforce UTC 
     * to prevent the system from secretly using the server's local timezone.
     *
     * @param dateTimeString the legacy formatted string
     * @return parsed {@link Instant} interpreted strictly as UTC
     */
    public static Instant parseLegacyToInstant(String dateTimeString) {
        if (dateTimeString == null) return null;
        // 1. Parse the naive string
        LocalDateTime ldt = LocalDateTime.parse(dateTimeString, LEGACY_FORMATTER);
        // 2. Explicitly tell Java "this is UTC", then convert to Instant
        return ldt.atZone(ZoneId.of("UTC")).toInstant();
    }
    
    // ---------------------------------------------------------
    // 4. BONUS: DISPLAYING TO USERS
    // ---------------------------------------------------------
    
    /**
     * Formats an {@link Instant} into the legacy pattern, but adjusted 
     * for a specific user's timezone (e.g., showing "2023-10-27 09:00:00" to a user in New York).
     *
     * @param instant the moment in time
     * @param zoneId  the user's timezone (e.g., ZoneId.of("America/New_York"))
     * @return formatted string in the user's local time
     */
    public static String formatForUser(Instant instant, ZoneId zoneId) {
        if (instant == null || zoneId == null) return null;
        return LEGACY_FORMATTER.withZone(zoneId).format(instant);
    }
}
