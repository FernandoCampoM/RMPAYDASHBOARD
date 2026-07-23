package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.time.LocalDate;

public record DailyTrendDto(
        LocalDate date,
        long activations,
        long registrations
) {
}