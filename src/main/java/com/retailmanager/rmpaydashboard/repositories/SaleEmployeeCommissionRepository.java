package com.retailmanager.rmpaydashboard.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpaydashboard.models.SaleEmployeeCommission;

public interface SaleEmployeeCommissionRepository extends CrudRepository<SaleEmployeeCommission, Long> {
    List<SaleEmployeeCommission> findBySale_SaleID(String saleId);

    void deleteBySale_SaleID(String saleId);

    @Query("""
        SELECT c FROM SaleEmployeeCommission c
        WHERE c.business.businessId = :businessId
          AND (
            (c.sale.saleEndDate IS NOT NULL AND c.sale.saleEndDate >= :startUtc AND c.sale.saleEndDate < :endUtc)
            OR (c.sale.saleEndDate IS NULL AND c.createdAt >= :startUtc AND c.createdAt < :endUtc)
          )
        ORDER BY c.userBusiness.username, c.sale.saleEndDate DESC
    """)
    List<SaleEmployeeCommission> findReport(
            @Param("businessId") Long businessId,
            @Param("startUtc") Instant startUtc,
            @Param("endUtc") Instant endUtc);
}
