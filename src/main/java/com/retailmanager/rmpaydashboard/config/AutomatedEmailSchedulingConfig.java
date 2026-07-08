package com.retailmanager.rmpaydashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// ── NEW: automated email scheduling configuration ──────────────────
// Purpose : Enables Spring scheduled tasks for automated email routines.
// Depends on : Spring scheduling infrastructure.
// Does NOT modify : BackgroundRoutines, BackgroundRoutinesService, RmpaydashboardApplication.
@Configuration
@EnableScheduling
public class AutomatedEmailSchedulingConfig {
}
