package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;
import java.time.Instant;

public record RegistrationDto(
        Long businessId,
        Instant date,
        String businessName,
        String town,
        String contact,
        String type,
        String status,
        String merchantId
) {
}