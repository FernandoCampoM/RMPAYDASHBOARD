package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftReport {

    private String employee;

    private String deviceId;

    private String shiftStart;

    private String shiftEnd;

    private BigDecimal cuadre;

    private Integer numTransactions;

    private List<ShiftTransactionDTO> transactions;

}
