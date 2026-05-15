package com.retailmanager.rmpaydashboard.models.Interface;


public interface HourlySalesProjection {

    Integer getHour();

    Integer getTransactions();

    Double getSales();
}