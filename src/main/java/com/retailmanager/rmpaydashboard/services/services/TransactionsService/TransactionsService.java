package com.retailmanager.rmpaydashboard.services.services.TransactionsService;

import com.retailmanager.rmpaydashboard.controller.ShiftController;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Sale;
import com.retailmanager.rmpaydashboard.models.Shift;
import com.retailmanager.rmpaydashboard.models.Transactions;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.SaleRepository;
import com.retailmanager.rmpaydashboard.repositories.ShiftReporsitory;
import com.retailmanager.rmpaydashboard.repositories.TransactionsRepository;
import com.retailmanager.rmpaydashboard.services.DTO.TransactionDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.ShiftReport;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.ShiftTransactionDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.TransactionDetailDTO;

@Service
public class TransactionsService implements ITransactionService {
private final ShiftController shiftController;
@Autowired
    private TransactionsRepository transactionsRepository;
    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private BusinessRepository businessRepository;


    @Autowired
    private ShiftReporsitory shiftRepository;
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    TransactionsService(ShiftController shiftController) {
        this.shiftController = shiftController;
    }
        /**
         * Saves a transaction in the database.
         *
         * @param transactionDTO the transaction to save
         * @return a ResponseEntity containing the saved transaction, or an exception if the transaction already exists or the sale for the transaction does not exist
         * @throws EntidadYaExisteException if the transaction already exists in the database
         * @throws EntidadNoExisteException if the sale for the transaction does not exist in the database
         */
    @Override
    @Transactional
    public ResponseEntity<?> saveTransaction(TransactionDTO transactionDTO) {
        if(this.transactionsRepository.existsById(transactionDTO.getId())){
            throw new EntidadYaExisteException("La transacción con id "+transactionDTO.getId()+" ya existe en la base de datos");
        }
        Sale sale = this.saleRepository.findById(transactionDTO.getSaleId())
                .orElseThrow(() -> new EntidadNoExisteException("La venta con id "+transactionDTO.getSaleId()+" no existe en la base de datos"));

        Transactions transaction = transactionDTO.toTransactions();
        transaction.setSale(sale);
        transaction=this.transactionsRepository.save(transaction);
        TransactionDTO transactionDTOrta=TransactionDTO.fromTransactions(transaction);
        return new ResponseEntity<>(transactionDTOrta, HttpStatus.CREATED);
    }

        /**
         * Saves a list of transactions in the database.
         *
         * @param transactionDTOs the list of transactions to save
         * @return a ResponseEntity containing the saved transactions, or an exception if one of the transactions already exists or the sale for one of the transactions does not exist
         * @throws EntidadYaExisteException if any of the transactions already exists in the database
         * @throws EntidadNoExisteException if any of the sales for the transactions do not exist in the database
         */
    @Override
    @Transactional
    public ResponseEntity<?> saveTransactions(List<TransactionDTO> transactionDTOs) {
        List<TransactionDTO> transactionDTOrtas = new ArrayList<>();
        for (TransactionDTO transactionDTO : transactionDTOs) {
            if (this.transactionsRepository.existsById(transactionDTO.getId())) {
                throw new EntidadYaExisteException("La transacción con id " + transactionDTO.getId() + " ya existe en la base de datos");
            }
            Sale sale = this.saleRepository.findById(transactionDTO.getSaleId())
                    .orElseThrow(() -> new EntidadNoExisteException("La venta con id " + transactionDTO.getSaleId() + " no existe en la base de datos"));

            Transactions transaction = transactionDTO.toTransactions();
            transaction.setSale(sale);
            transaction = this.transactionsRepository.save(transaction);
            TransactionDTO transactionDTOrta = TransactionDTO.fromTransactions(transaction);
            transactionDTOrtas.add(transactionDTOrta);
        }
        return new ResponseEntity<>(transactionDTOrtas, HttpStatus.CREATED);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTransactionsByMerchantId(String merchantId, LocalDateTime startDate,
        LocalDateTime endDate) {
        if(!this.businessRepository.findOneByMerchantId(merchantId).isPresent()) {
            throw new EntidadNoExisteException("El negocio con merchantId " + merchantId + " no existe en la base de datos");
        }
        List<Transactions> transactions = this.transactionsRepository.getTransactionsByMerchantIdAndDateBetween(merchantId, startDate, endDate);
        List<TransactionDTO> transactionDTOs = transactions.stream().map(TransactionDTO::fromTransactions).collect(Collectors.toList());
        return new ResponseEntity<>(transactionDTOs, HttpStatus.OK);
    }

    /**
     * Retrieves a list of transactions for a given business within a specified date range.
     *
     * @param businessId the ID of the business
     * @param startDate the start date of the date range
     * @param endDate the end date of the date range
     * @return a ResponseEntity containing a list of TransactionDTO objects representing the transactions
     *         for the specified business within the date range
     * @throws EntidadNoExisteException if the business with the given ID does not exist in the database
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTransactionsByBusinessId(Long businessId, LocalDateTime startDate,
            LocalDateTime endDate) {
        if(!this.businessRepository.existsById(businessId)) {
            throw new EntidadNoExisteException("El negocio con id " + businessId + " no existe en la base de datos");
        }
        List<Transactions> transactions = this.transactionsRepository.getTransactionsByBusinessIdAndDateBetween(businessId, startDate, endDate);
        List<TransactionDTO> transactionDTOs = transactions.stream().map(TransactionDTO::fromTransactions).collect(Collectors.toList());
        return new ResponseEntity<>(transactionDTOs, HttpStatus.OK);
    }

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id the ID of the transaction to retrieve
     * @return a ResponseEntity containing the transaction with the given ID, or
     *         an exception if the transaction does not exist
     * @throws EntidadNoExisteException if the transaction with the given ID does
     *                                  not exist in the database
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTransactionById(String id) {
        if(!this.transactionsRepository.existsById(id)) {
            throw new EntidadNoExisteException("La transacción con id " + id + " no existe en la base de datos");
        }
        Transactions transaction = this.transactionsRepository.findById(id).get();
        TransactionDTO transactionDTO = TransactionDTO.fromTransactions(transaction);
        return new ResponseEntity<>(transactionDTO, HttpStatus.OK);
    }

    /**
     * Updates a transaction in the database.
     *
     * @param transactionId the ID of the transaction to update
     * @param transactionDTO the updated transaction information
     * @return a ResponseEntity containing the updated transaction or an error message
     * @throws EntidadNoExisteException if the transaction with the given ID does not exist in the database
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateTransaction(String transactionId, TransactionDTO transactionDTO) {
        
        Sale sale = this.saleRepository.findById(transactionDTO.getSaleId())
                .orElseThrow(() -> new EntidadNoExisteException("La venta con id "+transactionDTO.getSaleId()+" no existe en la base de datos"));

        Transactions transaction = this.transactionsRepository.findById(transactionId)
                .orElseThrow(() -> new EntidadNoExisteException("La transacción con id "+transactionId+" no existe en la base de datos"));
        transaction.setAccount(transactionDTO.getAccount());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setDate(transactionDTO.getDate());
        transaction.setAuthCode(transactionDTO.getAuthCode());
        transaction.setBatchNo(transactionDTO.getBatchNo());
        transaction.setCardType(transactionDTO.getCardType());
        transaction.setChangeChash(transactionDTO.getChangeChash());
        transaction.setEntryMode(transactionDTO.getEntryMode());
        transaction.setGlobalUId(transactionDTO.getGlobalUId());
        transaction.setPaymentType(transactionDTO.getPaymentType());
        transaction.setRefId(transactionDTO.getRefId());
        transaction.setRemoto(transactionDTO.getRemoto());
        transaction.setState(transactionDTO.getState());
        


        transaction.setSale(sale);
        transaction=this.transactionsRepository.save(transaction);
        TransactionDTO transactionDTOrta=TransactionDTO.fromTransactions(transaction);
        return new ResponseEntity<>(transactionDTOrta, HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTransactionsByMerchantId(String merchantId, String terminalId, LocalDateTime startDate,
            LocalDateTime endDate) {
       if(!this.businessRepository.findOneByMerchantId(merchantId).isPresent()) {
            throw new EntidadNoExisteException("El negocio con merchantId " + merchantId + " no existe en la base de datos");
        }
        List<Transactions> transactions = new ArrayList<>();
        if(startDate!=null && endDate!=null){
            transactions = this.transactionsRepository.getTransactionsByMerchantIdAndTerminalIdAndDateBetween(merchantId, terminalId, startDate, endDate);
        }else{
            transactions = this.transactionsRepository.getTransactionsByMerchantIdAndTerminalId(merchantId, terminalId);
        }
        List<TransactionDTO> transactionDTOs = transactions.stream().map(TransactionDTO::fromTransactions).collect(Collectors.toList());
        return new ResponseEntity<>(transactionDTOs, HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRecentTransactionsByMerchantId(String merchantId, String terminalId, int days) {
        if(!this.businessRepository.findOneByMerchantId(merchantId).isPresent()) {
            throw new EntidadNoExisteException("El negocio con merchantId " + merchantId + " no existe en la base de datos");
        }

        int safeDays = Math.max(days, 1);
        Instant startDate = Instant.now().minusSeconds(safeDays * 86400L);
        List<Transactions> transactions;
        if (terminalId != null && !terminalId.isEmpty()) {
            transactions = this.transactionsRepository.getRecentTransactionsByMerchantIdAndTerminalId(merchantId, terminalId, startDate);
        } else {
            transactions = this.transactionsRepository.getRecentTransactionsByMerchantId(merchantId, startDate);
        }

        List<TransactionDTO> transactionDTOs = transactions.stream().map(TransactionDTO::fromTransactions).collect(Collectors.toList());
        return new ResponseEntity<>(transactionDTOs, HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRecentTransactionsByMerchantId(String merchantId, String terminalId, int days, int page, int size) {
        if(!this.businessRepository.findOneByMerchantId(merchantId).isPresent()) {
            throw new EntidadNoExisteException("El negocio con merchantId " + merchantId + " no existe en la base de datos");
        }

        int safeDays = Math.max(days, 1);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 500);
        Instant startDate = Instant.now().minusSeconds(safeDays * 86400L);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "date"));

        Page<Transactions> transactions;
        if (terminalId != null && !terminalId.isEmpty()) {
            transactions = this.transactionsRepository.getRecentTransactionsByMerchantIdAndTerminalId(merchantId, terminalId, startDate, pageable);
        } else {
            transactions = this.transactionsRepository.getRecentTransactionsByMerchantId(merchantId, startDate, pageable);
        }

        Page<TransactionDTO> transactionDTOs = transactions.map(TransactionDTO::fromTransactions);
        return new ResponseEntity<>(transactionDTOs, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateStatus(String transactionId, String status) {
        
        Transactions transaction = this.transactionsRepository.findById(transactionId)
                .orElseThrow(() -> new EntidadNoExisteException("La transacción con id "+transactionId+" no existe en la base de datos"));
       transaction.setState(status);
        transaction=this.transactionsRepository.save(transaction);
        TransactionDTO transactionDTOrta=TransactionDTO.fromTransactions(transaction);
        return new ResponseEntity<>(transactionDTOrta, HttpStatus.OK);
    }
    @Override
    @Transactional(readOnly = true)
public ResponseEntity<?> getTransactionDetails(
        Long businessId,
        Instant startDate,
        Instant endDate) {

    List<TransactionDetailDTO> response =
            saleRepository.getTransactionDetails(
                    businessId,
                    startDate,
                    endDate
            )
            .stream()
            .map(item -> TransactionDetailDTO.builder()
                    .saleCreationDate(Instant.parse(item.getSaleCreationDate()))
                    .transactionNumber(item.getGlobalUId())
                    .method(item.getPaymentType())
                    .subtotal(item.getSaleSubtotal())
                    .taxes(item.getTaxes())
                    .total(item.getSaleTotalAmount())
                    .build())
            .toList();

    return new ResponseEntity<>(response, HttpStatus.OK);
}

@Override    
@Transactional(readOnly = true)
    public ResponseEntity<?> getTransactionDetailsByShifts(
        Long userBusinessId,
        String shiftId){
            if(shiftId==null || shiftId.isEmpty()){
                throw new IllegalArgumentException("El shiftId no puede ser nulo o vacío");
                
            }
            Shift shift = shiftRepository.findById(shiftId)
            .orElseThrow(() -> new EntidadNoExisteException("Shift with ID " + shiftId + " does not exist."));
            
            List<ShiftTransactionDTO> response =
            saleRepository.getShiftTransactions(
                    shiftId,
                    userBusinessId
            )
            .stream()
            .map(item -> ShiftTransactionDTO.builder()
                    .shiftId(item.getShiftId())
                    .userBusinessId(item.getUserBusinessId())
                    .globalUId(item.getGlobalUId())
                    .amount(item.getAmount())
                    .transactionDate(Instant.parse(item.getTransactionDate()))
                    .terminalId(item.getTerminalId())
                    .build())
            .toList();
            ShiftReport shiftReport = ShiftReport.builder()
                    .employee(shift.getUserBusiness().getUserBusinessId().toString())
                    .deviceId(shift.getTerminal().getTerminalId())
                    .shiftStart(shift.getStartTime().toString())
                    .shiftEnd(shift.getEndTime()==null? "Shift still open":shift.getEndTime().toString())
                    .cuadre(shift.getBalanceFinal())
                    .transactions(response)
                    .numTransactions(response.size())
                    .build();
    return new ResponseEntity<>(shiftReport, HttpStatus.OK);
        }
}
