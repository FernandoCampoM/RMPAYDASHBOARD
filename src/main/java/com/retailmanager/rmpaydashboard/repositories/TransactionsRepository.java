package com.retailmanager.rmpaydashboard.repositories;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpaydashboard.models.Transactions;

public interface  TransactionsRepository extends CrudRepository<Transactions, String>{
    @Query( "select t from Transactions t " +
            "where t.sale.business.businessId=:businessId " +
            "and t.sale.saleEndDate between :startDate and :endDate " +
            "order by t.sale.saleEndDate desc")
    public List<Transactions> getTransactionsByRange(@Param("businessId") Long businessId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query( "select t from Transactions t " +
            "where t.sale.business.businessId=:businessId " +
            "and t.sale.saleEndDate >= :startDate and t.sale.saleEndDate < :endDate " +
            "order by t.sale.saleEndDate desc")
    List<Transactions> getTransactionsByRange(@Param("businessId") Long businessId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query( "select t from Transactions t where t.sale.business.merchantId=:merchantId and t.date between :startDate and :endDate")
    public List<Transactions> getTransactionsByMerchantIdAndDateBetween(String merchantId, LocalDateTime startDate, LocalDateTime endDate);
    @Query( "select t from Transactions t where t.sale.business.merchantId=:merchantId and t.sale.terminal.terminalId=:terminalId and t.date between :startDate and :endDate")
    public List<Transactions> getTransactionsByMerchantIdAndTerminalIdAndDateBetween(String merchantId, String terminalId, LocalDateTime startDate, LocalDateTime endDate);
    @Query( "select t from Transactions t where t.sale.business.merchantId=:merchantId and t.sale.terminal.terminalId=:terminalId")
    public List<Transactions> getTransactionsByMerchantIdAndTerminalId(String merchantId, String terminalId);
    @Query( "select t from Transactions t where t.sale.business.merchantId=:merchantId and t.date >= :startDate order by t.date desc")
    public List<Transactions> getRecentTransactionsByMerchantId(@Param("merchantId") String merchantId, @Param("startDate") Instant startDate);
    @Query( "select t from Transactions t where t.sale.business.merchantId=:merchantId and t.sale.terminal.terminalId=:terminalId and t.date >= :startDate order by t.date desc")
    public List<Transactions> getRecentTransactionsByMerchantIdAndTerminalId(@Param("merchantId") String merchantId, @Param("terminalId") String terminalId, @Param("startDate") Instant startDate);
    @Query( "select t from Transactions t where t.sale.business.businessId=:businessId and t.date between :startDate and :endDate")
    public List<Transactions> getTransactionsByBusinessIdAndDateBetween(Long businessId, LocalDateTime startDate, LocalDateTime endDate);
}
