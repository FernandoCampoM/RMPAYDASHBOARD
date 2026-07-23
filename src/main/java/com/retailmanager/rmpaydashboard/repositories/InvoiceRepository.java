package com.retailmanager.rmpaydashboard.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Invoice;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.PaymentMethodDashboardProjection;

public interface InvoiceRepository extends CrudRepository<Invoice, Long> {

    public List<Invoice> findByBusinessIdAndDateGreaterThanEqualAndDateLessThanEqualOrderByInvoiceNumberDesc(Long businessId, LocalDate startDate, LocalDate endDate);


    @Query("Select i from Invoice i  where i.date>=:startDate AND i.date<=:endDate")
    public List<Invoice> getPaymentReports(LocalDate startDate, LocalDate endDate);

    @Query("""
       SELECT COALESCE(SUM(i.totalAmount), 0)
       FROM Invoice i
       WHERE i.date BETWEEN :startDate AND :endDate
       AND i.inProcess = false
       AND (i.rejected = false OR i.rejected IS NULL)
       """)
BigDecimal sumSuccessfulPaymentsBetween(LocalDate startDate, LocalDate endDate);

@Query("""
       SELECT COUNT(i)
       FROM Invoice i
       WHERE i.date BETWEEN :startDate AND :endDate
       AND i.inProcess = false
       AND (i.rejected = false OR i.rejected IS NULL)
       """)
long countSuccessfulPaymentsBetween(LocalDate startDate, LocalDate endDate);

@Query("""
       SELECT i.paymentMethod AS code,
              COUNT(i) AS transactionCount,
              COALESCE(SUM(i.totalAmount), 0) AS amount
       FROM Invoice i
       WHERE i.date BETWEEN :startDate AND :endDate
       AND i.inProcess = false
       AND (i.rejected = false OR i.rejected IS NULL)
       GROUP BY i.paymentMethod
       """)
List<PaymentMethodDashboardProjection> findPaymentMethodsBetween(LocalDate startDate, LocalDate endDate);
}
