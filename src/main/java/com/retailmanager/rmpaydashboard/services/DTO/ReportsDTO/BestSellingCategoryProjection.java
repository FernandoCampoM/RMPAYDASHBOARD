package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

public interface BestSellingCategoryProjection {

    String getCategory();

    Long getTotalItems();

    Double getTotalAmount();

    Double getTotalCost();

    Double getTotalGrossProfit();
}
