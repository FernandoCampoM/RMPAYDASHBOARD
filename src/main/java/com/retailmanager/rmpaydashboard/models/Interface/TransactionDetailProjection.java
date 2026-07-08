package com.retailmanager.rmpaydashboard.models.Interface;


public interface TransactionDetailProjection {

    String getSaleCreationDate();

    String getGlobalUId();

    String getPaymentType();

    Double getSaleSubtotal();

    Double getTaxes();

    Double getSaleTotalAmount();
}