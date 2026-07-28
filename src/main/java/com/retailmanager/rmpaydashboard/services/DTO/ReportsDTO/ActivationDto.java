package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.time.Instant;

public record ActivationDto(
        String terminalId,
        String serial,
        Instant date,
        String businessName,
        String terminalName,
        String type,
        String status,
        Double serviceValue,
        String userName
) {
}