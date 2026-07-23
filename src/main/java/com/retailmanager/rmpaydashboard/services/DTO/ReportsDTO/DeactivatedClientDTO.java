package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;
import java.time.Instant;

public record DeactivatedClientDTO(
        Long clientId,
        String clientName,
        Long businessId,
        String businessName,
        Instant deactivationDate,
        String reasonCode,
        String reason,
        Instant lastPaymentDate,
        BigDecimal lastPaymentAmount,
        Boolean canReactivate
) {
}
