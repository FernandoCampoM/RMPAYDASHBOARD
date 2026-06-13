package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;

public interface DailySummaryProjection {

    // SALES
    BigDecimal getTotalSales();
    BigDecimal getSubTotalSales();
    BigDecimal getStateTaxSales();
    BigDecimal getCityTaxSales();
    BigDecimal getRedTaxSales();
    BigDecimal getTotalTips();

    // REFUNDS
    BigDecimal getTotalRefund();
    BigDecimal getSubTotalRefund();
    BigDecimal getStateTaxRefund();
    BigDecimal getCityTaxRefund();
    BigDecimal getRedTaxRefund();
    BigDecimal getTotalTipsRefund();

    // PROFIT
    BigDecimal getGrossBenefit();
}