package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommissionReportDTO {
    private BigDecimal totalSalesBase = BigDecimal.ZERO;
    private BigDecimal totalCommission = BigDecimal.ZERO;
    private List<EmployeeCommissionSummaryDTO> employeeCommissions = new ArrayList<>();

    @Getter
    @Setter
    public static class EmployeeCommissionSummaryDTO {
        private Long userBusinessId;
        private String username;
        private BigDecimal totalSalesBase = BigDecimal.ZERO;
        private BigDecimal totalCommission = BigDecimal.ZERO;
        private Integer salesCount = 0;
    }
}
