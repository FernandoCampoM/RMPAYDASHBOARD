package com.retailmanager.rmpaydashboard.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class DateFormater {
    private static final ZoneId USER_ZONE = ZoneId.of("America/Puerto_Rico");

    public static OffsetDateTime nowOffsetDateTime() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    public static LocalDateTime nowLocalDateTime() {
        return nowOffsetDateTime().toLocalDateTime();
    }

    public static OffsetDateTime toStartOfDayOffset(String date) {
        return date!=null ? LocalDate.parse(date)
                .atStartOfDay(ZoneOffset.UTC) // added hora 00:00:00
                .toOffsetDateTime() : null;
    }

    public static OffsetDateTime toEndOfDayOffset(String date) {
        return date!=null ? LocalDate.parse(date).plusDays(1) // added hora 23:59:59
                .atStartOfDay(ZoneOffset.UTC)
                .minusSeconds(1)
                .toOffsetDateTime() : null;
    }

    public static LocalDateTime toStartOfDayLDT(String date) {
        return date!=null ? toStartOfDayOffset(date).toLocalDateTime() : null;
    }

    public static LocalDateTime toEndOfDayLDT(String date) {
        return date!=null ? toEndOfDayOffset(date).toLocalDateTime() : null;
    }

    public static LocalDateTime toZone(LocalDateTime ldt) {
        return ldt!=null ? ldt.atZone(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    public static LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }

    public static OffsetDateTime toOffsetDateTimeUtc(LocalDateTime ldt) {
        return ldt != null ? ldt.atOffset(ZoneOffset.UTC) : null;
    }

    public static Instant startOfDayUtc(Instant instant) {
        if (instant == null) return null;

        LocalDate dateUtc = instant.atZone(ZoneOffset.UTC).toLocalDate();
        return dateUtc.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    public static Instant endOfDayUtc(Instant instant) {
        if (instant == null) return null;

        LocalDate dateUtc = instant.atZone(ZoneOffset.UTC).toLocalDate();
        return dateUtc.plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .minusSeconds(1)
                .toInstant();
    }
}
