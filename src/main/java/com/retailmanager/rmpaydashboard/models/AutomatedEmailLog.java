package com.retailmanager.rmpaydashboard.models;

import java.time.Instant;
import java.time.LocalDate;

import com.retailmanager.rmpaydashboard.models.Business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ── NEW: automated email execution log entity ──────────────────
// Purpose : Stores automated email executions to avoid duplicate daily sends.
// Depends on : Business.businessId and scheduled email business rules.
// Does NOT modify : Business, BusinessConfiguration, Sys_general_config.
@Entity
@Getter
@Setter
@NoArgsConstructor
public class AutomatedEmailLog {

    // NEW FIELD: id — safe to add, does not replace any existing field
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NEW FIELD: business — safe to add, does not replace Business.businessId
    @ManyToOne(optional = false)
    @JoinColumn(name = "businessId", nullable = false)
    private Business business;

    // NEW FIELD: emailType — safe to add, does not replace BusinessConfiguration.configKey
    @Column(nullable = false, length = 100)
    private String emailType;

    // NEW FIELD: reportDate — safe to add, does not replace any existing date field
    @Column(nullable = false)
    private LocalDate reportDate;

    // NEW FIELD: scheduledTime — safe to add, does not replace BusinessConfiguration.value
    @Column(nullable = false, length = 5)
    private String scheduledTime;

    // NEW FIELD: sentAt — safe to add, does not replace any existing timestamp
    @Column(nullable = false)
    private Instant sentAt;

    // NEW FIELD: status — safe to add, does not replace any existing status field
    @Column(nullable = false, length = 30)
    private String status;

    // NEW FIELD: errorMessage — safe to add, does not replace any existing error field
    @Column(columnDefinition = "VARCHAR(MAX)")
    private String errorMessage;

    @PrePersist
    private void prePersist() {
        if (this.sentAt == null) {
            this.sentAt = Instant.now();
        }
    }
}
