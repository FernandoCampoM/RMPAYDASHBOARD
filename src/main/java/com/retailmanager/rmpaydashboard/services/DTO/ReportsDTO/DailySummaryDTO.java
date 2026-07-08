package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DailySummaryDTO {
    private BigDecimal totalSales=BigDecimal.ZERO;
    private BigDecimal totalRefunds = BigDecimal.ZERO;
    private BigDecimal benefit = BigDecimal.ZERO;
    private BigDecimal totalTips = BigDecimal.ZERO;
    private BigDecimal stateTax = BigDecimal.ZERO;
    private BigDecimal estimatedRedTax = BigDecimal.ZERO;
    private BigDecimal municipalTax = BigDecimal.ZERO;
    //retorna una lista de objetos category, totalAmount
    List<CategoryNetSalesProjection> salesByCategory= new ArrayList<>();
    List<CategoryNetSalesProjection> earningsByCategory= new ArrayList<>();
    //retorna una lista de objetos name, quantity, totalAmount, benefit
    List<BestSellingItemProjection> bestSellingProducts= new ArrayList<>();
    private List<PaymentNetProjection> bestSellingPayMethods= new ArrayList<>();
    private List<HashMap<String,String>> refundsSummay= new ArrayList<>();
    
}
