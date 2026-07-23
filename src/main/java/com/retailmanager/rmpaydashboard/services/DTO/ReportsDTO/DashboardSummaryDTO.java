package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

public record DashboardSummaryDTO(
        DashboardMetricDTO activeClients,
        DashboardMetricDTO negociosActivos,
        DashboardMetricDTO negociosInactivos,
        DashboardMetricDTO paymentsReceived,
        DashboardMetricDTO paymentsCount,
        DashboardMetricDTO newRMPayLiteClients,
        DashboardMetricDTO newTerminals,
        DashboardMetricDTO newRegisteredClients
) {
}