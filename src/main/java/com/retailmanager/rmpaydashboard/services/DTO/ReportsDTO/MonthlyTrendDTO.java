package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;

public record MonthlyTrendDTO(
        String month,
        String label,
        Long newClients,
        Long newTerminals,
        Long deactivations,
        BigDecimal paymentsReceived
) {
}