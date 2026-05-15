package com.retailmanager.rmpaydashboard.services.services.TransactionsService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.TransactionDTO;

public interface ITransactionService {
    public ResponseEntity<?> saveTransaction(TransactionDTO transactionDTO);
    public ResponseEntity<?> updateTransaction(String transactionId, TransactionDTO transactionDTO);
    public ResponseEntity<?> updateStatus(String transactionId, String status);
    public ResponseEntity<?> saveTransactions(List<TransactionDTO> transactionDTOs);
    public ResponseEntity<?> getTransactionsByMerchantId(String merchantId, LocalDateTime startDate, LocalDateTime endDate);
    public ResponseEntity<?> getTransactionsByMerchantId(String merchantId, String terminalId, LocalDateTime startDate, LocalDateTime endDate);
    public ResponseEntity<?> getTransactionsByBusinessId(Long businessId, LocalDateTime startDate, LocalDateTime endDate);
    public ResponseEntity<?> getTransactionById(String id);
    public ResponseEntity<?> getTransactionDetails(
        Long businessId,
        Instant startDate,
        Instant endDate);
    public ResponseEntity<?> getTransactionDetailsByShifts(
        Long userBusinessId,
        String shiftId);
}
