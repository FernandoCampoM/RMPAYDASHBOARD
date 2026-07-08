package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TransactionDetailDTO {

    private Instant saleCreationDate;

    private String transactionNumber;

    private String method;

    private Double subtotal;

    private Double taxes;

    private Double total;
}