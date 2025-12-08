package com.retailmanager.rmpaydashboard.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Terminal;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;


public interface TerminalRepository extends CrudRepository<Terminal, String> {
    Optional<Terminal> findOneBySerial(String serial);
    
    List<Terminal> findByBusiness(Business business);
    
    Optional<Terminal> findFirstBySerial(String serial);
    Optional<Terminal> findFirstBySerialAndBusiness(String serial,Business business);
    @Modifying
    @Query("UPDATE Terminal u SET u.enable = :enable WHERE u.terminalId = :terminalId")
    void updateEnable(String terminalId, boolean enable);
    /**
     * Obtiene los terminles expirados de un negocio
     *
     * @param  business     negocio
     * @param  date         fecha para comparar con la fecha de expiración
     * @return              description of return value
     */
    //
    public List<Terminal> findByBusinessAndExpirationDateLessThan(Business business, LocalDate date);

    @Query("SELECT b FROM Terminal b WHERE b.lastPayment BETWEEN :startDate AND :endDate")
    List<Terminal> findAllByActivations(LocalDate startDate, LocalDate endDate);

    @Query("Select COUNT(t) from Terminal t where t.enable = true and t.business.businessId = :businessId")
    int countActiveTerminals(Long businessId);

    List<Terminal> findByExpirationDateBefore(LocalDate date);
    @Modifying
    @Transactional
    @Query("UPDATE Terminal t SET t.enable= false where t.terminalId = :terminalId")
     int deactivateExpiredTerminals(String terminalId);

    @Modifying
    @Transactional
    @Query("UPDATE Terminal t SET t.automaticPayments= :status where t.terminalId = :terminalId")
     int updateAutomaticPayments(String terminalId, Boolean status);

    @Query("Select t from Terminal t where t.business.priorNotification IS NULL  and t.expirationDate=:targetDate")
     List<Terminal> getBusinessForPriorNotification(LocalDate targetDate);
     @Query("Select t from Terminal t where t.business.lastDayNotification IS NULL  and t.expirationDate=:targetDate")
     List<Terminal> getBusinessForLastDayNotification(LocalDate targetDate);
     @Query("Select t from Terminal t where t.business.afterNotification IS NULL and t.expirationDate=:targetDate")
     List<Terminal> getBusinessForAfterNotification(LocalDate targetDate);
    
     @Query("Select t from Terminal t where t.automaticPayments=True  and t.expirationDate<=:targetDate and t.business.businessId = :businessId")
     List<Terminal> findTerminalsForPayment(LocalDate targetDate, Long businessId);
     @Query("Select t from Terminal t where t.automaticPayments=True  and t.expirationDate>:targetDate and t.business.businessId = :businessId")
     List<Terminal> terminalsThatAreNotPaid(LocalDate targetDate, Long businessId);
}
