package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommissionAuditReportDTO {
    private BigDecimal totalCommission = BigDecimal.ZERO;
    private List<CommissionSaleAuditDTO> sales = new ArrayList<>();

    @Getter
    @Setter
    public static class CommissionSaleAuditDTO {
        private String saleId;
        private Instant saleDate;
        private BigDecimal commissionTotal = BigDecimal.ZERO;
        private List<CommissionAuditItemDTO> items = new ArrayList<>();
        private List<CommissionAuditShareDTO> shares = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class CommissionAuditItemDTO {
        private Long productId;
        private String name;
        private Integer quantity;
        private BigDecimal price = BigDecimal.ZERO;
    }

    @Getter
    @Setter
    public static class CommissionAuditShareDTO {
        private Long userBusinessId;
        private String username;
        private BigDecimal splitPercent = BigDecimal.ZERO;
        private BigDecimal commissionAmount = BigDecimal.ZERO;
        private Boolean paid = false;
    }
}
