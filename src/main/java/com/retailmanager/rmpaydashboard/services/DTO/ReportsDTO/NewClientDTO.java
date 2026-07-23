package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.time.Instant;

public record NewClientDTO(
        Long clientId,
        String clientName,
        Long businessId,
        String businessName,
        Instant registrationDate,
        Long planId,
        String planCode,
        String planName,
        Long sellerId,
        String sellerName,
        String status
) {
}