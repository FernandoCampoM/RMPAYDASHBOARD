package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;

public record PaymentMethodsTotalDTO(
        Long transactionCount,
        BigDecimal amount
) {
}
