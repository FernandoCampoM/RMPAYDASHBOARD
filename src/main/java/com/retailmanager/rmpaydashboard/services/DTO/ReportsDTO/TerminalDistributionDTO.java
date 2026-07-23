package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

public record TerminalDistributionDTO(
        long active,
        long inactive,
        long deactivated,
        long total
) {
}