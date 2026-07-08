package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record BatchCloseReportDTO(

        Long businessId,
        String businessName,

        String batchId,

        Instant reportDate,
        Instant batchStart,
        Instant batchEnd,

        Summary summary,

        PaymentSummary sales,

        PaymentSummary refunds,

        TaxSummary taxes,

        BatchDetails details

) {

    public record Summary(
            BigDecimal totalAmount,
            Integer totalTransactions
    ) {
    }

    public record PaymentSummary(
            BigDecimal credit,
            BigDecimal debit,
            BigDecimal total
    ) {
    }

    public record TaxSummary(
            List<TaxItem> taxes,
            BigDecimal totalTaxes
    ) {
    }

    public record TaxItem(
            String name,
            BigDecimal amount
    ) {
    }

    public record BatchDetails(
            Instant previousBatchDate,
            Instant currentBatchDate,
            Duration duration
    ) {
    }
}