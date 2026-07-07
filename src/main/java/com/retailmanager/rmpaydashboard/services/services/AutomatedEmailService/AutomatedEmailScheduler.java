package com.retailmanager.rmpaydashboard.services.services.AutomatedEmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// ── NEW: automated email scheduler ──────────────────
// Purpose : Runs automated email checks every server minute.
// Depends on : AutomatedEmailService.processDailySummaryEmails.
// Does NOT modify : Existing Timer-based BackgroundRoutines.
@Component
public class AutomatedEmailScheduler {

    @Autowired
    private AutomatedEmailService automatedEmailService;

    @Scheduled(cron = "0 * * * * *")
    public void runDailySummaryEmailCheck() {
        automatedEmailService.processDailySummaryEmails();
    }
}
