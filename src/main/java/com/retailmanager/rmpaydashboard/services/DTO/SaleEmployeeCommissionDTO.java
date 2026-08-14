package com.retailmanager.rmpaydashboard.services.DTO;

import java.math.BigDecimal;
import java.time.Instant;

import com.retailmanager.rmpaydashboard.models.SaleEmployeeCommission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleEmployeeCommissionDTO {
    private Long commissionId;
    private String saleId;
    private Long businessId;
    private Long userBusinessId;
    private String username;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal saleBaseAmount;
    private BigDecimal commissionPercent;
    private BigDecimal splitPercent;
    private BigDecimal commissionAmount;
    private Boolean paid;
    private Instant paidAt;
    private Instant createdAt;

    public static SaleEmployeeCommissionDTO fromEntity(SaleEmployeeCommission commission) {
        SaleEmployeeCommissionDTO dto = new SaleEmployeeCommissionDTO();
        dto.setCommissionId(commission.getCommissionId());
        dto.setSaleId(commission.getSale().getSaleID());
        dto.setBusinessId(commission.getBusiness().getBusinessId());
        dto.setUserBusinessId(commission.getUserBusiness().getUserBusinessId());
        dto.setUsername(commission.getUserBusiness().getUsername());
        dto.setProductId(commission.getProductId());
        dto.setProductName(commission.getProductName());
        dto.setQuantity(commission.getQuantity());
        dto.setSaleBaseAmount(commission.getSaleBaseAmount());
        dto.setCommissionPercent(commission.getCommissionPercent());
        dto.setSplitPercent(commission.getSplitPercent());
        dto.setCommissionAmount(commission.getCommissionAmount());
        dto.setPaid(commission.getPaid());
        dto.setPaidAt(commission.getPaidAt());
        dto.setCreatedAt(commission.getCreatedAt());
        return dto;
    }
}
