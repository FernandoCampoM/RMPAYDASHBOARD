package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

public record StatusDistributionDto(
        long active,
        long inactive30Days,
        long deactivated,
        long registrations,
        long total
) {
}
