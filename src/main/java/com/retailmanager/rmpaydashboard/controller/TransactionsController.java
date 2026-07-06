package com.retailmanager.rmpaydashboard.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.services.DTO.TransactionDTO;
import com.retailmanager.rmpaydashboard.services.services.TransactionsService.ITransactionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionsController {

    @Autowired
    private ITransactionService transactionService;

    /**
     * Save a single transaction.
     *
     * @param transactionDTO The transaction data.
     * @return ResponseEntity with the result of saving the transaction.
     */
    @PostMapping
    public ResponseEntity<?> saveTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        return transactionService.saveTransaction(transactionDTO);
    }
    @PutMapping("/{transactionId}")
    public ResponseEntity<?> updateTransaction(@PathVariable String transactionId, @Valid @RequestBody TransactionDTO transactionDTO) {
        return transactionService.updateTransaction(transactionId, transactionDTO);
    }
    @PutMapping("/{transactionId}/status/{status}")
    public ResponseEntity<?> updateTransactionStatus(@PathVariable String transactionId, @PathVariable String status) {
        return transactionService.updateStatus(transactionId, status);
    }

    /**
     * Save multiple transactions.
     *
     * @param transactionDTOs List of transactions to save.
     * @return ResponseEntity with the result of saving the transactions.
     */
    @PostMapping("/batch")
    public ResponseEntity<?> saveTransactions(@Valid @RequestBody List<TransactionDTO> transactionDTOs) {
        return transactionService.saveTransactions(transactionDTOs);
    }

    /**
     * Get transactions by merchantId within a date range.
     *
     * @param merchantId The merchant ID.
     * @param startDate  The start date (ISO format).
     * @param endDate    The end date (ISO format).
     * @return ResponseEntity with transactions found.
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<?> getTransactionsByMerchantId(
            @Valid @PathVariable @NotBlank(message = "El merchantId no puede estar vacío") String merchantId,
            @RequestParam(required=false)  String terminalId,
            @RequestParam( required = false)  LocalDate startDate,
            @RequestParam( required = false)  LocalDate endDate) {
        
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;
        if(terminalId!=null && !terminalId.isEmpty()){
            return transactionService.getTransactionsByMerchantId(merchantId, terminalId, startDateTime, endDateTime);
        }
        return transactionService.getTransactionsByMerchantId(merchantId, startDateTime, endDateTime);
    }

    @GetMapping("/merchant/{merchantId}/recent")
    public ResponseEntity<?> getRecentTransactionsByMerchantId(
            @Valid @PathVariable @NotBlank(message = "El merchantId no puede estar vacÃ­o") String merchantId,
            @RequestParam(required=false) String terminalId,
            @RequestParam(defaultValue = "2") int days) {
        return transactionService.getRecentTransactionsByMerchantId(merchantId, terminalId, days);
    }

    /**
     * Get transactions by businessId within a date range.
     *
     * @param businessId The business ID.
     * @param startDate  The start date (ISO format).
     * @param endDate    The end date (ISO format).
     * @return ResponseEntity with transactions found.
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<?> getTransactionsByBusinessId(
            @Valid @PathVariable @NotNull Long businessId,
            @RequestParam("startDate") @NotNull LocalDate startDate,
            @RequestParam("endDate") @NotNull LocalDate endDate) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return transactionService.getTransactionsByBusinessId(businessId, startDateTime, endDateTime);
    }

    /**
     * Get a transaction by its ID.
     *
     * @param id The transaction ID.
     * @return ResponseEntity with the transaction found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(
            @Valid @PathVariable @NotBlank(message = "El id no puede estar vacío") String id) {
        return transactionService.getTransactionById(id);
    }
    @GetMapping("")
public ResponseEntity<?> getTransactions(
        @RequestParam @NotNull Long businessId,
        @RequestParam @NotNull Instant startDate,
        @RequestParam(required = false)  Instant endDate) {
        if(endDate==null){
            endDate=startDate.plusSeconds(86399); // Si no se proporciona endDate, se establece al final del día de startDate
        }
    return transactionService.getTransactionDetails(
            businessId,
            startDate,
            endDate
    );
}
    @GetMapping("/shifts")
public ResponseEntity<?> getTransactionsByShift(
        @RequestParam @NotNull Long userBusinessId,
        @RequestParam @NotNull String shiftId) {
        
    return transactionService.getTransactionDetailsByShifts(
            userBusinessId,
            shiftId
    );
}
}
