package com.retailmanager.rmpaydashboard.repositories;

    public interface DashboardKpiProjection {

    Double getSalesYTD();

    Double getSalesLY();

    Double getProfitYTD();

    Double getProfitLY();

    Double getAvgTicketYTD();

    Double getAvgTicketLY();

    Double getTodaySales();
    Double getYesterdaySales();
    Double getTwoDaysAgoSales();
    Double getThisWeekSales();
    Double getLastWeekSalesUntilToday();

    Double getTodayTaxes();
    Double getYesterdayTaxes();
    Double getTwoDaysAgoTaxes();
    Double getThisWeekTaxes();
    Double getLastWeekTaxesUntilToday();

    Double getTodayProfit();
    Double getYesterdayProfit();
    Double getTwoDaysAgoProfit();
    Double getThisWeekProfit();
    Double getLastWeekProfitUntilToday();

    Integer getTodayTransactions();
    Integer getTransactionsYTD();
    Double getTaxesYTD();
}