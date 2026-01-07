package com.retailmanager.rmpaydashboard.services.services.EmailService;

import java.util.List;

public interface IEmailService {
    public void sendHtmlEmail(List<String> toList, String subject, String htmlBody);
    public void sendHtmlEmailWithAttachmentAndCCO(List<String> toList, String subject, String htmlBody, List<String> cc, byte[] attachmentData, String attachmentFileName);
    public void notifyPaymentCreditCard(EmailBodyData emailData);
    public void notifyPaymentToken(EmailBodyData emailData);
    public void notifyPaymentDiscount(EmailBodyData emailData);
    public void notifyPaymentATHMovil(EmailBodyData emailData);
    public void notifyPaymentBankAccount(EmailBodyData emailData);
    public void notifyNewRegister(EmailBodyData emailData);
    public void notifyNewBusiness(EmailBodyData emailData);
    public void notifyRejectedPayment(EmailBodyData emailData);
    public void notifyErrorRegister(EmailBodyData emailData);
    public void notifyErrorPayment(EmailBodyData emailData);
    public void notifyNewTerminal(EmailBodyData emailData);

    public void priorNotificationEmail(String email,String userName, String businessName, List<String> services);
    public void lastDayNotificationEmail(String email,String userName, String businessName, List<String> services);
    public void beforeNotificationEmail(String email,String userName, String businessName, List<String> services);
    public void testEmailService(EmailBodyData emailData);
}
