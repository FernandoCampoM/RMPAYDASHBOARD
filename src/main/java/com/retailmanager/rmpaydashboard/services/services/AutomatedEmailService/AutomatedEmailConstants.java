package com.retailmanager.rmpaydashboard.services.services.AutomatedEmailService;

// ── NEW: automated email constants ──────────────────
// Purpose : Centralizes BusinessConfiguration keys used by automated emails.
// Depends on : Existing BusinessConfiguration.configKey values.
// Does NOT modify : BusinessConfiguration, Sys_general_config, existing report services.
public final class AutomatedEmailConstants {

    public static final String DAILY_SUMMARY_TYPE = "DAILY_SUMMARY";
    public static final String EMAIL_FROM_KEY = "Email.User";
    public static final String EMAIL_PASS_KEY = "Email.Pass";
    public static final String EMAIL_RECIPIENTS_KEY = "Email.UserTo";
    public static final String EMAIL_SMTP_HOST_KEY = "Email.smtpHost";
    public static final String EMAIL_SMTP_PORT_KEY = "Email.smtpPort";
    public static final String DAILY_SUMMARY_ACTIVE_KEY = "Email.DailySummary.active";
    public static final String DAILY_SUMMARY_TIME_KEY = "Email.DailySummary.time";
    public static final String LOW_INVENTORY_TYPE = "LOW_INVENTORY";
public static final String DAILY_LOW_INVENTORY_ACTIVE_KEY = "Email.LowInventory.active";
public static final String DAILY_LOW_INVENTORY_TIME_KEY = "Email.LowInventory.time";
public static final String SHIFT_CLOSING_REPORT_ACTIVE_KEY = "Email.BatchClosure.active";

    private AutomatedEmailConstants() {
    }
}
