package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

public interface BestSellingItemProjection {

    Long getProductId();

    Long getQuantity();

    Double getTotalAmount();

    Double getCost();

    Double getBenefit();

    String getName();

    Double getPrice();

    String getCategory();
}