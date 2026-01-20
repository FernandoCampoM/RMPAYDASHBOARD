package com.retailmanager.rmpaydashboard.models;

import java.time.LocalDateTime;

public record ScheduleDetailedCalendar(
        Long id,
        String day,
        String hour,
        LocalDateTime dateStart,
        LocalDateTime dateEnd,
        String color,
        Long employee
) {
}
