package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;

public record PaymentMethodDTO(
        String code,
        String name,
        Long transactionCount,
        BigDecimal amount
) {
}

