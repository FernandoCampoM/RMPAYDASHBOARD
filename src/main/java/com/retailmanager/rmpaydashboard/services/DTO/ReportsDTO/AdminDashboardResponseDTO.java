package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.time.Instant;
import java.util.List;

public record AdminDashboardResponseDTO(
        Instant generatedAt,
        String timezone,
        String currency,
        DashboardPeriodDTO period,
        DashboardSummaryDTO summary,
        List<NewClientDTO> newClients,
        List<DeactivatedClientDTO> deactivatedClients,
        List<PaymentMethodDTO> paymentMethods,
        PaymentMethodsTotalDTO paymentMethodsTotal,
        List<MonthlyTrendDTO> monthlyTrend,
        TerminalDistributionDTO terminalDistribution,
        List<?> recentActivity
) {
}