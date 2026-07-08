package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.AutomatedEmailLog;

// ── NEW: automated email log repository ──────────────────
// Purpose : Reads and writes email execution logs for duplicate prevention.
// Depends on : AutomatedEmailLog fields and Spring Data derived queries.
// Does NOT modify : Existing repositories or existing query contracts.
public interface AutomatedEmailLogRepository extends CrudRepository<AutomatedEmailLog, Long> {

    boolean existsByBusiness_BusinessIdAndEmailTypeAndReportDate(Long businessId, String emailType, LocalDate reportDate);
}
