package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

public interface BestSellingItemProjection {

    Long getProductId();

    String getName();

    Long getQuantity();

    Double getTotalAmount();

    Double getBenefit();

    Double getPrice();

    String getCategory();
}