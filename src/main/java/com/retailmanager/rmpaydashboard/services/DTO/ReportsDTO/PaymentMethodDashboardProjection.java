package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;

public interface PaymentMethodDashboardProjection {
    String getCode();
    Long getTransactionCount();
    BigDecimal getAmount();
}