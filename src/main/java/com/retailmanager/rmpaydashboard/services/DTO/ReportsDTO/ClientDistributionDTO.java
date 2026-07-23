package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

public record ClientDistributionDTO(
        Long active,
        Long inactive,
        Long deactivated,
        Long total
) {
}