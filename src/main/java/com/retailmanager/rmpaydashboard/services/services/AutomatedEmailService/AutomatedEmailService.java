package com.retailmanager.rmpaydashboard.services.services.AutomatedEmailService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.BusinessConfiguration;
import com.retailmanager.rmpaydashboard.models.AutomatedEmailLog;
import com.retailmanager.rmpaydashboard.repositories.BusinessConfigurationRepository;
import com.retailmanager.rmpaydashboard.repositories.AutomatedEmailLogRepository;
import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ShiftDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.BatchCloseReportDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.DailySummaryDTO;
import com.retailmanager.rmpaydashboard.services.services.EmailService.IEmailService;
import com.retailmanager.rmpaydashboard.services.services.ReportsServices.IReportService;

// ── NEW: automated email service ──────────────────
// Purpose : Coordinates configured automated DailySummary emails per business.
// Depends on : BusinessConfiguration values, IReportService.getDailySummary, IEmailService.sendHtmlEmailWithAttachmentAndCCO.
// Does NOT modify : Existing report endpoint contracts, EmailService send behavior, BackgroundRoutines.
@Service
public class AutomatedEmailService {

    @Autowired
    private BusinessConfigurationRepository businessConfigurationRepository;

    @Autowired
    private AutomatedEmailLogRepository automatedEmailLogRepository;

    @Autowired
    private IReportService reportService;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private DailySummaryEmailTemplate dailySummaryEmailTemplate;
    @Autowired
private LowInventoryEmailTemplate lowInventoryEmailTemplate;
@Autowired
private ShiftClosingEmailTemplate shiftClosingEmailTemplate;
@Autowired
private BatchCloseReportEmailTemplate batchCloseReportEmailTemplate;

    @Transactional
    public void processDailySummaryEmails() {
        LocalDateTime serverNow = LocalDateTime.now();
        LocalTime currentMinute = serverNow.toLocalTime().withSecond(0).withNano(0);
        LocalDate reportDate = serverNow.toLocalDate().minusDays(1);

        List<BusinessConfiguration> activeConfigurations = businessConfigurationRepository.findByConfigKey(
                AutomatedEmailConstants.DAILY_SUMMARY_ACTIVE_KEY);
        for (BusinessConfiguration activeConfiguration : activeConfigurations) {
            Business business = activeConfiguration.getBusiness();
            if (business == null || !business.isEnable() || !isTrue(activeConfiguration.getValue())) {
                continue;
            }

            BusinessConfiguration timeConfiguration = businessConfigurationRepository.findByKey(
                    AutomatedEmailConstants.DAILY_SUMMARY_TIME_KEY,
                    business.getBusinessId());
            LocalTime scheduledTime = parseScheduledTime(timeConfiguration);
            if (scheduledTime == null || currentMinute.isBefore(scheduledTime)) {
                continue;
            }

            sendDailySummaryIfPending(business, reportDate, scheduledTime);
        }
    }

    private void sendDailySummaryIfPending(Business business, LocalDate reportDate, LocalTime scheduledTime) {
    boolean alreadySent = automatedEmailLogRepository.existsByBusiness_BusinessIdAndEmailTypeAndReportDate(
            business.getBusinessId(),
            AutomatedEmailConstants.DAILY_SUMMARY_TYPE,
            reportDate
    );

    if (alreadySent) {
        return;
    }

    try {
        Long businessId = business.getBusinessId();

        String smtpHost = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_SMTP_HOST_KEY,
                businessId
        );

        int smtpPort = getRequiredConfigurationIntValue(
                AutomatedEmailConstants.EMAIL_SMTP_PORT_KEY,
                businessId
        );

        String smtpUsername = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_FROM_KEY,
                businessId
        );

        String smtpPassword = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_PASS_KEY,
                businessId
        );

        String fromEmail = getFromEmail(businessId);

        List<String> recipients = getRecipients(businessId);
        if (recipients.isEmpty()) {
            saveLog(business, reportDate, scheduledTime, "SKIPPED", "Email.UserTo has no valid recipients.");
            return;
        }

        DailySummaryDTO summary = getDailySummary(businessId, reportDate);

        String htmlBody = dailySummaryEmailTemplate.build(business, reportDate, summary);

        String subject = "Resumen diario - " + business.getName() + " - " + reportDate;

        emailService.sendHtmlEmailWithAttachmentAndCCO(
                smtpHost,
                smtpPort,
                smtpUsername,
                smtpPassword,
                true,
                true,
                fromEmail,
                recipients,
                subject,
                htmlBody,
                List.of(),
                null,
                null
        );

        saveLog(business, reportDate, scheduledTime, "SENT", null);

    } catch (Exception ex) {
        saveLog(business, reportDate, scheduledTime, "ERROR", ex.getMessage());
    }
}
private int getRequiredConfigurationIntValue(String key, Long businessId) {
    String value = getRequiredConfigurationValue(key, businessId);

    try {
        return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
        throw new IllegalStateException("Invalid numeric email configuration: " + key + " = " + value);
    }
}
private String getRequiredConfigurationValue(String key, Long businessId) {
    BusinessConfiguration configuration = businessConfigurationRepository.findByKey(key, businessId);

    if (configuration == null || configuration.getValue() == null || configuration.getValue().isBlank()) {
        throw new IllegalStateException("Missing required email configuration: " + key);
    }

    return configuration.getValue().trim();
}

    private DailySummaryDTO getDailySummary(Long businessId, LocalDate reportDate) {
        ZoneId serverZone = ZoneId.systemDefault();
        Instant startUtc = reportDate.atStartOfDay(serverZone).toInstant();
        Instant endUtc = reportDate.plusDays(1).atStartOfDay(serverZone).minusNanos(1).toInstant();
        ResponseEntity<?> response = reportService.getDailySummary(businessId, startUtc, endUtc);
        return (DailySummaryDTO) response.getBody();
    }

    private List<String> getRecipients(Long businessId) {
        BusinessConfiguration recipientsConfiguration = businessConfigurationRepository.findByKey(
                AutomatedEmailConstants.EMAIL_RECIPIENTS_KEY,
                businessId);
        if (recipientsConfiguration == null || recipientsConfiguration.getValue() == null) {
            return List.of();
        }
        return Arrays.stream(recipientsConfiguration.getValue().split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .filter(value -> value.contains("@"))
                .distinct()
                .toList();
    }

    private String getFromEmail(Long businessId) {
        BusinessConfiguration fromConfiguration = businessConfigurationRepository.findByKey(
                AutomatedEmailConstants.EMAIL_FROM_KEY,
                businessId);
        if (fromConfiguration == null || fromConfiguration.getValue() == null) {
            return null;
        }
        return fromConfiguration.getValue().trim();
    }

    private LocalTime parseScheduledTime(BusinessConfiguration timeConfiguration) {
        if (timeConfiguration == null || timeConfiguration.getValue() == null || timeConfiguration.getValue().isBlank()) {
            return null;
        }
        String value = timeConfiguration.getValue().trim();
        try {
            if (value.matches("^\\d{1,2}$")) {
                return LocalTime.of(Integer.parseInt(value), 0);
            }
            if (value.matches("^\\d{1,2}:\\d{2}$")) {
                String[] parts = value.split(":");
                return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
            return LocalTime.parse(value).withSecond(0).withNano(0);
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isTrue(String value) {
        return value != null && (
                "true".equalsIgnoreCase(value.trim())
                        || "1".equals(value.trim())
                        || "yes".equalsIgnoreCase(value.trim()));
    }

    private void saveLog(Business business, LocalDate reportDate, LocalTime scheduledTime, String status, String errorMessage) {
        AutomatedEmailLog log = new AutomatedEmailLog();
        log.setBusiness(business);
        log.setEmailType(AutomatedEmailConstants.DAILY_SUMMARY_TYPE);
        log.setReportDate(reportDate);
        log.setScheduledTime(String.format("%02d:%02d", scheduledTime.getHour(), scheduledTime.getMinute()));
        log.setSentAt(Instant.now());
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        automatedEmailLogRepository.save(log);
    }
    @Transactional
public void processLowInventoryEmails() {
    LocalDateTime serverNow = LocalDateTime.now();
    LocalTime currentMinute = serverNow.toLocalTime().withSecond(0).withNano(0);
    LocalDate reportDate = serverNow.toLocalDate();

    List<BusinessConfiguration> activeConfigurations = businessConfigurationRepository.findByConfigKey(
            AutomatedEmailConstants.DAILY_LOW_INVENTORY_ACTIVE_KEY);

    for (BusinessConfiguration activeConfiguration : activeConfigurations) {
        Business business = activeConfiguration.getBusiness();

        if (business == null || !business.isEnable() || !isTrue(activeConfiguration.getValue())) {
            continue;
        }

        BusinessConfiguration timeConfiguration = businessConfigurationRepository.findByKey(
                AutomatedEmailConstants.DAILY_LOW_INVENTORY_TIME_KEY,
                business.getBusinessId());

        LocalTime scheduledTime = parseScheduledTime(timeConfiguration);

        if (scheduledTime == null || currentMinute.isBefore(scheduledTime)) {
            continue;
        }

        sendLowInventoryIfPending(business, reportDate, scheduledTime);
    }
}
private void sendLowInventoryIfPending(Business business, LocalDate reportDate, LocalTime scheduledTime) {
    boolean alreadySent = automatedEmailLogRepository.existsByBusiness_BusinessIdAndEmailTypeAndReportDate(
            business.getBusinessId(),
            AutomatedEmailConstants.LOW_INVENTORY_TYPE,
            reportDate
    );

    if (alreadySent) {
        return;
    }

    try {
        Long businessId = business.getBusinessId();

        String smtpHost = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_SMTP_HOST_KEY,
                businessId
        );

        int smtpPort = getRequiredConfigurationIntValue(
                AutomatedEmailConstants.EMAIL_SMTP_PORT_KEY,
                businessId
        );

        String smtpUsername = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_FROM_KEY,
                businessId
        );

        String smtpPassword = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_PASS_KEY,
                businessId
        );

        String fromEmail = getFromEmail(businessId);

        List<String> recipients = getRecipients(businessId);
        if (recipients.isEmpty()) {
            saveLowInventoryLog(business, reportDate, scheduledTime, "SKIPPED", "Email.UserTo has no valid recipients.");
            return;
        }

        List<ProductDTO> lowInventoryProducts = getLowInventoryProducts(businessId);

        if (lowInventoryProducts.isEmpty()) {
            saveLowInventoryLog(business, reportDate, scheduledTime, "SKIPPED", "No low inventory products found.");
            return;
        }

        String htmlBody = lowInventoryEmailTemplate.build(
                business,
                reportDate,
                lowInventoryProducts
        );

        String subject = "Inventario bajo - " + business.getName() + " - " + reportDate;

        emailService.sendHtmlEmailWithAttachmentAndCCO(
                smtpHost,
                smtpPort,
                smtpUsername,
                smtpPassword,
                true,
                true,
                fromEmail,
                recipients,
                subject,
                htmlBody,
                List.of(),
                null,
                null
        );

        saveLowInventoryLog(business, reportDate, scheduledTime, "SENT", null);

    } catch (Exception ex) {
        saveLowInventoryLog(business, reportDate, scheduledTime, "ERROR", ex.getMessage());
    }
}
private List<ProductDTO> getLowInventoryProducts(Long businessId) {
    ResponseEntity<?> response = reportService.getLowInventory(businessId);

    if (!(response.getBody() instanceof List<?> products)) {
        return List.of();
    }

    return products.stream()
            .filter(ProductDTO.class::isInstance)
            .map(ProductDTO.class::cast)
            .toList();
}
private void saveLowInventoryLog(Business business, LocalDate reportDate, LocalTime scheduledTime, String status, String errorMessage) {
    AutomatedEmailLog log = new AutomatedEmailLog();
    log.setBusiness(business);
    log.setEmailType(AutomatedEmailConstants.LOW_INVENTORY_TYPE);
    log.setReportDate(reportDate);
    log.setScheduledTime(String.format("%02d:%02d", scheduledTime.getHour(), scheduledTime.getMinute()));
    log.setSentAt(Instant.now());
    log.setStatus(status);
    log.setErrorMessage(errorMessage);
    automatedEmailLogRepository.save(log);
}

@Transactional
public void sendShiftClosingReport(
        Long businessId,
        String businessName,
        ShiftDTO shift
) {
    System.out.println("SMTP: Enviando email de cierre de turno...");
    try {
        BusinessConfiguration activeConfiguration = businessConfigurationRepository.findByKey(AutomatedEmailConstants.SHIFT_CLOSING_REPORT_ACTIVE_KEY, businessId);
        if (activeConfiguration == null || !isTrue(activeConfiguration.getValue())) {
            System.out.println("Shift closing report email is not active for businessId: " + businessId);
            return;
        }
        String smtpHost = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_SMTP_HOST_KEY,
                businessId
        );

        int smtpPort = getRequiredConfigurationIntValue(
                AutomatedEmailConstants.EMAIL_SMTP_PORT_KEY,
                businessId
        );

        String smtpUsername = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_FROM_KEY,
                businessId
        );

        String smtpPassword = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_PASS_KEY,
                businessId
        );

        String fromEmail = getFromEmail(businessId);

        List<String> recipients = getRecipients(businessId);
        if (recipients.isEmpty()) {
            throw new IllegalStateException("Email.UserTo has no valid recipients.");
        }

        String htmlBody = shiftClosingEmailTemplate.build(
                businessName,
                businessId,
                shift
        );

        String subject = "Cierre de turno - " + businessName + " - " + shift.userName();

        emailService.sendHtmlEmailWithAttachmentAndCCO(
                smtpHost,
                smtpPort,
                smtpUsername,
                smtpPassword,
                true,
                true,
                fromEmail,
                recipients,
                subject,
                htmlBody,
                List.of(),
                null,
                null
        );

    } catch (Exception ex) {
        System.out.println("com.retailmanager.rmpaydashboard.services.services.AutomatedEmailService.AutomatedEmailService.sendShiftClosingReport Error enviando email de cierre de turno: " + ex.getMessage());
        throw new IllegalStateException("Error enviando cierre de turno: " + ex.getMessage(), ex);
    }
}
@Transactional
public ResponseEntity<?> sendBatchCloseReport(BatchCloseReportDTO report) {
    System.out.println("SMTP: Enviando email de cierre de batch...");
    try {
        Long businessId = report.businessId();
        BusinessConfiguration activeConfiguration = businessConfigurationRepository.findByKey(AutomatedEmailConstants.BATCH_CLOSING_REPORT_ACTIVE_KEY, businessId);
        if (activeConfiguration == null || !isTrue(activeConfiguration.getValue())) {
            HashMap<String, String> response = new HashMap<>();
            response.put("message", "Batch close report email is not active.");
            return ResponseEntity.ok().body(response);
        }
        
        String smtpHost = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_SMTP_HOST_KEY,
                businessId
        );

        int smtpPort = getRequiredConfigurationIntValue(
                AutomatedEmailConstants.EMAIL_SMTP_PORT_KEY,
                businessId
        );

        String smtpUsername = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_FROM_KEY,
                businessId
        );

        String smtpPassword = getRequiredConfigurationValue(
                AutomatedEmailConstants.EMAIL_PASS_KEY,
                businessId
        );

        String fromEmail = getFromEmail(businessId);

        List<String> recipients = getRecipients(businessId);

        if (recipients.isEmpty()) {
            throw new IllegalStateException("Email.UserTo has no valid recipients.");
        }

        String htmlBody = batchCloseReportEmailTemplate.build(report);

        String subject = "Cierre de batch - "
                + report.businessName()
                + " - "
                + report.batchId();

        emailService.sendHtmlEmailWithAttachmentAndCCO(
                smtpHost,
                smtpPort,
                smtpUsername,
                smtpPassword,
                true,
                true,
                fromEmail,
                recipients,
                subject,
                htmlBody,
                List.of(),
                null,
                null
        );
        HashMap<String, String> response = new HashMap<>();
        response.put("message", "Batch close report email sent successfully.");
        return ResponseEntity.ok().body(response);
    } catch (Exception ex) {
        throw new IllegalStateException(
                "Error enviando reporte de cierre de batch: " + ex.getMessage(),
                ex
        );
    }
}
}
