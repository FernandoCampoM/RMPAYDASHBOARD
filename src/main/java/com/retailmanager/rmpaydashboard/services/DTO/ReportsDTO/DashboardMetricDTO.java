package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;

public record DashboardMetricDTO(
        Number currentValue,
        Number previousValue,
        BigDecimal variationPercentage
) {
}