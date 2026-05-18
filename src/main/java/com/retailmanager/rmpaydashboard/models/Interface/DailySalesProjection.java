package com.retailmanager.rmpaydashboard.models.Interface;

public interface DailySalesProjection {

    Integer getDayOfMonth();

    Integer getTransactions();

    Double getSales();
}