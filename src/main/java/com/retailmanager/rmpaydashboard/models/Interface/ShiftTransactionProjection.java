package com.retailmanager.rmpaydashboard.models.Interface;

public interface ShiftTransactionProjection {

    String getShiftId();

    String getUserBusinessId();

    String getUserName();

    String getGlobalUId();

    String getPaymentType();

    Double getAmount();

    String getAuthCode();

    String getCardType();

    String getTransactionDate();

    String getSaleId();

    Double getSaleSubtotal();

    Double getTaxes();

    Double getSaleTotalAmount();

    Double getTipAmount();

    String getSaleStatus();
    String getTerminalId();
}