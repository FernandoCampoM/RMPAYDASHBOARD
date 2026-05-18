package com.retailmanager.rmpaydashboard.models.Interface;

import java.time.Instant;

public interface TransactionDetailProjection {

    String getSaleCreationDate();

    String getGlobalUId();

    String getPaymentType();

    Double getSaleSubtotal();

    Double getTaxes();

    Double getSaleTotalAmount();
}