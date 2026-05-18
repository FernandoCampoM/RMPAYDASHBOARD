package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftTransactionDTO {

    private String shiftId;

    private String userBusinessId;


    private String globalUId;


    private Double amount;


    private Instant transactionDate;
    private String terminalId;
}