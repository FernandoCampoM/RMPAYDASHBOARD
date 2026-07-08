package com.retailmanager.rmpaydashboard.models;

import java.util.List;


public record ScheduleListDetailedCalendar(
        String week,
        List<ScheduleDetailedCalendar> detailedCalendars
) {
}
