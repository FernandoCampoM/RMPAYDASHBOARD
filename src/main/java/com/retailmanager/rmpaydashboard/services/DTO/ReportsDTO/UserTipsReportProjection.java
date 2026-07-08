package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

public interface UserTipsReportProjection {

    Long getUserId();

    String getUsername();

    Double getTotalSales();

    Double getSubTotalSales();

    Double getTotalTips();

    Double getBenefit();
}