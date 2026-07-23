package com.retailmanager.rmpaydashboard.repositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Terminal;

import jakarta.transaction.Transactional;

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
    public List<Terminal> findByBusinessAndExpirationDateLessThan(Business business, Instant date);

    @Query("SELECT b FROM Terminal b WHERE b.lastPayment BETWEEN :startDate AND :endDate")
    List<Terminal> findAllByActivations(Instant startDate, Instant endDate);

    @Query("Select COUNT(t) from Terminal t where t.enable = true and t.business.businessId = :businessId")
    int countActiveTerminals(Long businessId);

    List<Terminal> findByExpirationDateBefore(Instant date);
    @Modifying
    @Transactional
    @Query("UPDATE Terminal t SET t.enable= false where t.terminalId = :terminalId")
     int deactivateExpiredTerminals(String terminalId);

    @Modifying
    @Transactional
    @Query("UPDATE Terminal t SET t.automaticPayments= :status where t.terminalId = :terminalId")
     int updateAutomaticPayments(String terminalId, Boolean status);

    @Query("Select t from Terminal t where t.business.priorNotification IS NULL  and t.expirationDate=:targetDate")
     List<Terminal> getBusinessForPriorNotification(Instant targetDate);
     @Query("Select t from Terminal t where t.business.lastDayNotification IS NULL  and t.expirationDate=:targetDate")
     List<Terminal> getBusinessForLastDayNotification(Instant targetDate);
     @Query("Select t from Terminal t where t.business.afterNotification IS NULL and t.expirationDate=:targetDate")
     List<Terminal> getBusinessForAfterNotification(Instant targetDate);
    
     @Query("Select t from Terminal t where t.automaticPayments=True  and t.expirationDate<=:targetDate and t.business.businessId = :businessId")
     List<Terminal> findTerminalsForPayment(Instant targetDate, Long businessId);
     @Query("Select t from Terminal t where t.automaticPayments=True  and t.expirationDate>:targetDate and t.business.businessId = :businessId")
     List<Terminal> terminalsThatAreNotPaid(Instant targetDate, Long businessId);


     @Query("SELECT COUNT(t) FROM Terminal t")
long countAllTerminals();

@Query("SELECT COUNT(t) FROM Terminal t WHERE t.enable = false")
long countDeactivatedTerminals();

@Query("""
       SELECT COUNT(t)
       FROM Terminal t
       WHERE t.enable = true
       AND t.lastTransmision IS NOT NULL
       AND t.lastTransmision > :inactiveLimit
       """)
long countCurrentlyActiveTerminals(Instant inactiveLimit);

@Query("""
       SELECT COUNT(t)
       FROM Terminal t
       WHERE t.enable = true
       AND (t.lastTransmision IS NULL OR t.lastTransmision <= :inactiveLimit)
       """)
long countInactiveTerminals(Instant inactiveLimit);
@Query("""
       SELECT COUNT(t)
       FROM Terminal t
       WHERE t.registerDate BETWEEN :startDate AND :endDate
       """)
long countNewTerminalsBetween(Instant startDate, Instant endDate);

@Query("""
       SELECT COUNT(DISTINCT t.business.user.userID)
       FROM Terminal t
       WHERE t.enable = false
       AND t.expirationDate IS NOT NULL
       AND t.expirationDate BETWEEN :startDate AND :endDate
       """)
long countDeactivatedClientsBetween(Instant startDate, Instant endDate);
@Query("""
       SELECT COUNT(DISTINCT t.business.businessId)
       FROM Terminal t
       WHERE t.isPrincipal = true
       AND t.enable = false
       AND t.expirationDate IS NOT NULL
       AND t.expirationDate < :now
       """)
long countBusinessesWithExpiredAndDeactivatedPrincipalTerminal(Instant now);
@Query("""
       SELECT COUNT(DISTINCT t.business.businessId)
       FROM Terminal t
       WHERE t.isPrincipal = true
       AND t.enable = true
       AND t.expirationDate IS NOT NULL
       AND t.expirationDate >= :now
       """)
long countBusinessesWithActivePrincipalTerminal(Instant now);
@Query("""
       SELECT COUNT(t)
       FROM Terminal t
       WHERE t.expirationDate IS NOT NULL
       AND t.expirationDate >= :asOf
       """)
long countActiveTerminalsAt(Instant asOf);
@Query("""
       SELECT COUNT(t)
       FROM Terminal t
       WHERE t.expirationDate IS NOT NULL
       AND t.expirationDate >= :asOf
       AND (
           t.lastTransmision IS NULL
           OR t.lastTransmision <= :inactiveLimit
       )
       """)
long countInactiveTerminalsAt(Instant asOf, Instant inactiveLimit);
@Query("""
       SELECT COUNT(t)
       FROM Terminal t
       WHERE 
        t.expirationDate IS NOT NULL
       AND t.expirationDate < :asOf
       """)
long countDeactivatedTerminalsAt(Instant asOf);
@Query("""
       SELECT COUNT(DISTINCT b.user.userID)
       FROM Business b
       WHERE NOT EXISTS (
           SELECT 1
           FROM Terminal t
           WHERE t.business = b
           AND t.enable = true
           AND t.expirationDate IS NOT NULL
           AND t.expirationDate >= :asOf
       )
       """)
long countClientsWithoutActiveMembershipAt(Instant asOf);
}
