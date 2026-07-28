package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.time.LocalDate;

public record DashboardPeriodDTO(
        Integer year,
        Integer month,
        String label,
        LocalDate startDate,
        LocalDate endDate,
        String previousPeriodLabel,
        LocalDate previousStartDate,
        LocalDate previousEndDate
) {
}
