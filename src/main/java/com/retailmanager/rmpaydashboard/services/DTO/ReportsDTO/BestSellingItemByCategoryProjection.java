package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

public interface BestSellingItemByCategoryProjection {

    Long getProductId();

    String getBarcode();

    String getName();

    String getCode();

    String getCategory();

    Double getPrice();

    Double getCost();

    Integer getQuantity();

    Double getTotalAmount();

    Double getProfit();

}