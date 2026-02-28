package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.Instant;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.retailmanager.rmpaydashboard.models.Transactions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class TransactionDTO {

    @NotBlank(message = "{transaction.id.notblank}")
 private String id;
 private String account;

 @NotNull(message = "{transaction.amount.notnull}")
 private Double amount;
 private String authCode;

 private String batchNo;

 private String cardType;

 private Double changeChash;

 private Instant date;

 private String entryMode;

 private String globalUId;

 @NotBlank(message = "{transaction.paymentType.notblank}")
 private String paymentType;

 private String refId;

 private String state;

 @NotNull(message = "{transaction.saleId.notnull}")
 private String saleId;
 @NotNull(message = "{transaction.remoto.notnull}")
 private Integer remoto;

 private SaleDTO infoSale;
    
public Transactions toTransactions(){
    Transactions transaction = new Transactions();
    transaction.setId(this.id);
    transaction.setAccount(this.account);
    transaction.setAmount(this.amount);
    transaction.setAuthCode(this.authCode);
    transaction.setBatchNo(this.batchNo);
    transaction.setCardType(this.cardType);
    transaction.setChangeChash(this.changeChash);
    transaction.setDate(this.date);
    transaction.setEntryMode(this.entryMode);
    transaction.setGlobalUId(this.globalUId);
    transaction.setPaymentType(this.paymentType);
    transaction.setRefId(this.refId);
    transaction.setState(this.state);
    transaction.setRemoto(this.remoto);
    return transaction;
}
public static TransactionDTO fromTransactions(Transactions transaction){
    TransactionDTO transactionDTO=new TransactionDTO();
    transactionDTO.setId(transaction.getId());
    transactionDTO.setAccount(transaction.getAccount());
    transactionDTO.setAmount(transaction.getAmount());
    transactionDTO.setAuthCode(transaction.getAuthCode());
    transactionDTO.setBatchNo(transaction.getBatchNo());
    transactionDTO.setCardType(transaction.getCardType());
    transactionDTO.setChangeChash(transaction.getChangeChash());
    transactionDTO.setDate(transaction.getDate());
    transactionDTO.setEntryMode(transaction.getEntryMode());
    transactionDTO.setGlobalUId(transaction.getGlobalUId());
    transactionDTO.setPaymentType(transaction.getPaymentType());
    transactionDTO.setRefId(transaction.getRefId());
    transactionDTO.setState(transaction.getState());
    transactionDTO.setSaleId(transaction.getSale().getSaleID());
    transactionDTO.setRemoto(transaction.getRemoto());
    return transactionDTO;
}
    // Constructor, getters y setters
}
