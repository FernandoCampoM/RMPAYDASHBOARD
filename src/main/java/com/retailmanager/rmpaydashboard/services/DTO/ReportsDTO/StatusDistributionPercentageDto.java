package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

public record StatusDistributionPercentageDto(
        double activePercentage,
        double inactivePercentage,
        double deactivatedPercentage,
        double registrationsPercentage
) {
}