package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EarningsReportDTO {
    private BigDecimal totalSales=BigDecimal.ZERO;
    private BigDecimal subTotalSales = BigDecimal.ZERO;
    private BigDecimal benefit = BigDecimal.ZERO;
    private BigDecimal stateTax = BigDecimal.ZERO;
    private BigDecimal estimatedRedTax = BigDecimal.ZERO;
    private BigDecimal municipalTax = BigDecimal.ZERO;
    List<BestSellingCategoryProjection> earningsByCategory= new ArrayList<>();
    List<BestSellingItemProjection> bestSellingProducts= new ArrayList<>();
    
}
