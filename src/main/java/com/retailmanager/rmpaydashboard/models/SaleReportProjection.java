package com.retailmanager.rmpaydashboard.models;

import java.math.BigDecimal;

public interface SaleReportProjection {
    BigDecimal getSaleATH();
    BigDecimal getSaleCash();
    BigDecimal getSaleDebit();
    BigDecimal getSaleCredit();

    BigDecimal getRefundATH();
    BigDecimal getRefundDebit();
    BigDecimal getRefundCash();
    BigDecimal getRefundCredit();

    BigDecimal getStateTax();
    BigDecimal getCityTax();
    BigDecimal getReduceTax();
}
