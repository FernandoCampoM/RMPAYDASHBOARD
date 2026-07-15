package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.util.List;

public record ActivationDashboardResponseDto(
        long totalRegistrations,
        long totalActivations,
        long lastMonthRegistrations,
        long lastMonthActivations,
        double totalSales,
        double activationVariationPercentage,
        double registrationVariationPercentage,
        long activeTerminals,
        long inactiveTerminals,
        long deactivatedTerminals,
        long totalTerminals,
        List<RegistrationDto> registrations,
        List<ActivationDto> activations,
        List<DailyTrendDto> dailyTrend,
        StatusDistributionDto statusDistribution,
        StatusDistributionPercentageDto statusDistributionPercentage
) {
}