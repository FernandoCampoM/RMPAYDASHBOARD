package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.rmpayAtTheTable.RMPayAtTheTable_Terminal;

public interface TerminalPayAtTableRepository extends CrudRepository<RMPayAtTheTable_Terminal, Long>  {
    
    @Query("SELECT r FROM RMPayAtTheTable_Terminal r WHERE r.user.userId = :userId")
    List<RMPayAtTheTable_Terminal> findByUserId(Long userId);

    Optional<RMPayAtTheTable_Terminal> findBySerialNumber(String serialNumber);
    @Query("""
       SELECT COUNT(DISTINCT t.user.userId)
       FROM RMPayAtTheTable_Terminal t
       WHERE t.registrationDate BETWEEN :startDate AND :endDate
       """)
long countDistinctUsersByRegistrationDateBetween(
        LocalDate startDate,
        LocalDate endDate
);
}
