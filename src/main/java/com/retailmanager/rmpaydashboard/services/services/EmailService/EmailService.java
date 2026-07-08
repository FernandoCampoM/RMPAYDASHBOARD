package com.retailmanager.rmpaydashboard.services.services.EmailService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import jakarta.mail.internet.MimeMessage;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConfigurationNotFoundException;
import com.retailmanager.rmpaydashboard.models.FileModel;
import com.retailmanager.rmpaydashboard.repositories.FileRepository;
import com.retailmanager.rmpaydashboard.repositories.Sys_general_configRepository;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalsDoPaymentDTO;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import jakarta.annotation.PostConstruct;

@Service
public class EmailService implements IEmailService{
    private  String SENDGRID_API_KEY = "";
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private Sys_general_configRepository sys_general_configRepository;
    private EmailConfigData emailConfigData;
    /**
     * Método de inicialización que carga la configuración de correo electrónico y maneja los casos en los que
     * no hay una configuración válida.
     */
    @PostConstruct
    public void init() {
        // Aquí puedes ejecutar el código que necesitas después de que sys_general_configRepository haya sido inicializado
        this.emailConfigData = loadAndValid();
        if (this.emailConfigData != null) {
            this.SENDGRID_API_KEY = emailConfigData.getAppKey();
        } else {
            // Si no hay una configuración válida, maneja la situación según tus necesidades
            this.SENDGRID_API_KEY = "";
        }
    }
    
    
    
    
    @Override
    public void sendHtmlEmail(List<String> toList, String subject, String htmlBody) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendHtmlEmail'");
    }

    /**
     * Sends an HTML email with attachments and carbon copy recipients.
     *
     * @param  toList             list of email recipients
     * @param  subject            email subject
     * @param  htmlBody           HTML content of the email
     * @param  cc                 list of carbon copy recipients
     * @param  attachmentData     data of the attachment
     * @param  attachmentFileName name of the attachment file
     */
    @Override
    public void sendHtmlEmailWithAttachmentAndCCO(List<String> toList, String subject, String htmlBody, List<String> cc,
            byte[] attachmentData, String attachmentFileName)  {
                try {
        Email from = new Email(emailConfigData.getEmailFrom()); 

        // Configurar destinatarios
        Personalization personalization = new Personalization();
        for (String recipient : toList) {
            personalization.addTo(new Email(recipient));
        }

        // Configurar destinatarios en copia (CC)
        if (cc != null) {
            for (String ccRecipient : cc) {
                personalization.addCc(new Email(ccRecipient));
            }
        }

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject);
        mail.addContent(new Content("text/html", htmlBody));
        mail.addPersonalization(personalization);

        // Adjuntar archivos
        if (attachmentData != null && attachmentFileName != null && !attachmentFileName.isEmpty()) {
            InputStream pdfInputStream = new ByteArrayInputStream(attachmentData);
                    Attachments attachments = new Attachments.Builder(attachmentFileName, pdfInputStream)
                                                         .withType("application/"+obtenerExtension(attachmentFileName))
                                                         .build();
                    mail.addAttachments(attachments);
                    System.out.println("Adjuntando archivo: " + attachmentFileName);
            }
        

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        System.out.println("Enviando correo...");
            Response response = sg.api(request);
            if (response.getStatusCode() != 202) {
                System.out.println("Error al enviar el correo: " + response.getBody());
            }
            if(response.getStatusCode() == 202){
                System.out.println("Correo enviado exitosamente");
            }
        } catch (IOException ex) {
            System.out.println("Error al enviar el correo: " + ex.getMessage());
        }catch (Exception ex) {
            System.out.println("Error al enviar el correo: " + ex.getMessage());
        }
        
    }

    /**
     * Notify payment via ATH Movil.
     *
     * @param  emailData  the email body data
     * @return            void
     */
    @Override
    public void notifyPaymentATHMovil(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
        cc.add(emailConfigData.getEmailTo());
        String subject = "RECIBO #" + emailData.getInvoiceNumber() + " DE PAGO VIA ATH MOVIL";
        String htmlBody = createBodyEmailATHMovil(emailData);
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    }

    /**
     * Notify the payment bank account via email.
     *
     * @param  emailData  the email body data
     * @return            void
     */
    @Override
    public void notifyPaymentBankAccount(EmailBodyData emailData) {
        try {
            List<String> toList = Arrays.asList(emailData.getEmail());
            List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
            cc.add(emailConfigData.getEmailTo());
            String subject = "RECIBO #" + emailData.getInvoiceNumber() + " DE PAGO VIA CUENTA BANCARIA";
            String htmlBody = createBodyEmailBankAccount(emailData);
            Optional<FileModel> fileModel=this.fileRepository.findById(emailData.getChequeVoidId());
            byte[] file=null;
            if(fileModel.isPresent()){
                file=fileModel.get().getContenido();
            }
            sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, file, fileModel.get().getNombre());
        }catch(Exception ex){
            System.out.println("Error al enviar el correo a: "+emailData.getEmail()+" Error: " + ex.getMessage());
        }
        
    }

    /**
     * Notify new register with email data.
     *
     * @param  emailData  email data for the new register
     * @return            void
     */
    @Override
    public void notifyNewRegister(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
        cc.add(emailConfigData.getEmailTo());
        String subject = "NUEVO CLIENTE REGISTRADO EN RMPAY";
        
        String htmlBody = createBodyNewRegistry(emailData);
        htmlBody=htmlBody.replace("-paymethod-", emailData.getPaymethod());
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    
    }
    @Override
    public void notifyNewBusiness(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
        cc.add(emailConfigData.getEmailTo());
        String subject = "NUEVO NEGOCIO REGISTRADO EN RMPAY";
        
        String htmlBody = createBodyNewRegistry(emailData);
        htmlBody=htmlBody.replace("-paymethod-", emailData.getPaymethod());
        htmlBody=htmlBody.replace("NUEVO CLIENTE REGISTRADO EN RMPAY", "NUEVO NEGOCIO REGISTRADO EN RMPAY");
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
     
    }
    @Override
    public void notifyNewTerminal(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
        cc.add(emailConfigData.getEmailTo());
        String subject = "NUEVO TERMINAL REGISTRADO EN RMPAY PARA EL NEGOCIO "+emailData.getBusinessName();
        
        String htmlBody = createBodyNewRegistry(emailData);
        htmlBody=htmlBody.replace("-paymethod-", emailData.getPaymethod());
        htmlBody=htmlBody.replace("NUEVO CLIENTE REGISTRADO EN RMPAY", "NUEVO TERMINAL REGISTRADO EN RMPAY PARA EL NEGOCIO "+emailData.getBusinessName());
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
     
    }

    /**
     * Notify rejected payment with email data.
     *
     * @param  emailData  the email body data
     * @return            void
     */
    @Override
    public void notifyRejectedPayment(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
        cc.add(emailConfigData.getEmailTo());
        String subject = "EL PAGO HA SIDO RECHAZADO";
        
        String htmlBody = createBodyPaymentRejected(emailData);
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    
    }

    /**
     * Notifies an error during registration via email.
     *
     * @param  emailData  the email body data
     */
    @Override
    public void notifyErrorRegister(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailConfigData.getEmailTo());
        List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
        String subject = "CLIENTE SE HA INTENTADO REGISTRAR EN RMPAY PERO FALLÓ";
        if(emailData.isBuyTerminal()){
            subject = "CLIENTE "+emailData.getBusinessName().toUpperCase()+" HA INTENTADO COMPRAR UNA TERMINAL EN RMPAY PERO FALLÓ";
        }
        emailData.setSubject(subject);
        String htmlBody = createBodyRegistrationError(emailData);
        htmlBody=htmlBody.replace("-paymethod-", emailData.getPaymethod());
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    }
    @Override
    public void notifyErrorPayment(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailConfigData.getEmailTo());
        List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
        String subject = "OCURRIÓ UN ERROR EN RMPAYAL PROCESAR EL PAGO DEL CLIENTE: "+emailData.getBusinessName().toUpperCase();
        if(emailData.isBuyTerminal()){
            subject = "CLIENTE "+emailData.getBusinessName().toUpperCase()+" HA INTENTADO COMPRAR UNA TERMINAL EN RMPAY PERO FALLÓ";
        }
        emailData.setSubject(subject);
        String htmlBody = createBodyRegistrationError(emailData);
        htmlBody=htmlBody.replace("-paymethod-", emailData.getPaymethod());
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    }

    /**
     * Notify payment via credit card.
     *
     * @param  emailData   the email body data
     * @return             void
     */
    @Override
    public void notifyPaymentCreditCard(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
        cc.add(emailConfigData.getEmailTo());
        String subject = "RECIBO #" + emailData.getInvoiceNumber() + " DE PAGO VIA TARJETA";
        if(emailData.getInvoiceNumber()!=0){
            subject = "RECIBO #"+emailData.getInvoiceNumber()+" DE PAGO CON TARJETA ";
        }
        String htmlBody = createBodyEmailCreditCard(emailData);
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    }
    /**
     * Notifies the user about a payment with a discount available.
     *
     * @param  emailData  the email body data containing the email and invoice number
     * @return            void
     */
    @Override
    public void notifyPaymentDiscount(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
        cc.add(emailConfigData.getEmailTo());
        String subject = "RECIBO #" + emailData.getInvoiceNumber() + " DE PAGO CON DESCUENTO DISPONIBLE";
        if(emailData.getInvoiceNumber()!=0){
            subject = "RECIBO #"+emailData.getInvoiceNumber()+" DE PAGO CON DESCUENTO DISPONIBLE";
        }
        String htmlBody = createBodyEmailDiscount(emailData);
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    }
    /**
     * Notify payment via token.
     *
     * @param  emailData   the email body data
     * @return             void
     */
    @Override
    public void notifyPaymentToken(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
        cc.add(emailConfigData.getEmailTo());
        String subject = "RECIBO #" + emailData.getInvoiceNumber() + " DE PAGO VIA TOKEN DE PAGO";
        if(emailData.getInvoiceNumber()!=0){
            subject = "RECIBO #"+emailData.getInvoiceNumber()+" DE PAGO CON TOKEN DE PAGO";
        }
        String htmlBody = createBodyPaymentToken(emailData);
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    }
    @Override
    public void priorNotificationEmail(String email, String userName, String businessName,List<String> services) {
        
        List<String> toList = Arrays.asList(email);
        String htmlBody=createBodyPaymentNotification(userName,businessName,services,"10");
        String subject = "SU MEMBRESÍA VA A TERMINAR, ¡QUEDAN 10 DÍAS!";
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, null, null, null);
    }




    @Override
    public void lastDayNotificationEmail(String email, String userName, String businessName,List<String> services) {
        List<String> toList = Arrays.asList(email);
        String htmlBody=createBodyPaymentNotification(userName,businessName,services,"5");
        String subject = "SU MEMBRESÍA VA A TERMINAR, ¡QUEDAN 5 DÍAS!";
sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, null, null, null);
    }




    @Override
    public void beforeNotificationEmail(String email, String userName, String businessName,List<String> services) {
        
        List<String> toList = Arrays.asList(email);
        String htmlBody=createBodyPaymentNotification(userName,businessName,services,"1");
        String subject = "SU MEMBRESÍA VA A TERMINAR, ¡HOY ÚLTIMO DÍA!";
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, null, null, null);
    }
    /**
     * Creates an email body for a credit card payment with the provided email data.
     *
     * @param  emailData   the email data used to create the email body
     * @return             the email body for the credit card payment
     */
    private String createBodyEmailCreditCard(EmailBodyData emailData) {
        LocalDate fechaActual = LocalDate.now();
        String mes = "";
        switch (emailData.getExpDateMonth()) {
            case "1":
                mes = "Enero";
                break;
            case "2":
                mes = "Febrero";
                break;
            case "3":
                mes = "Marzo";
                break;
            case "4":
                mes = "Abril";
                break;
            case "5":
                mes = "Mayo";
                break;
            case "6":
                mes = "Junio";
                break;
            case "7":
                mes = "Julio";
                break;
            case "8":
                mes = "Agosto";
                break;
            case "9":
                mes = "Septiembre";
                break;
            case "10":
                mes = "Octubre";
                break;
            case "11":
                mes = "Noviembre";
                break;
            case "12":
                mes = "Diciembre";
                break;
        }
        DecimalFormat formato = new DecimalFormat("0.00");

        String mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ")";
        if (emailData.isAutomaticPayments()) {
            mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ") y pagos siguientes automatizados";
        }
        String message = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "    <style>"
                + "        .row{"
                + "            margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;"
                + "        }"
                + "        .col1{"
                + "            flex: 0 0 41.66667%;max-width: 41.66667%; text-align:right; margin:0px;"
                + "        }"
                + "        .col2{"
                + "            flex: 0 0 41.66667%;max-width: 41.66667%;text-align:left;margin:0 0 0 10px"
                + "        }"
                + "    </style>"
                + "</head>"
                + "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;\">"
                + "    <div>"
                + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">"
                + "            <tbody>"
                + "                <tr>"
                + "                    <td style=\"padding: 0in 0in 0in 0in\">"
                + "                        <div align=\"center\">"
                + "                            <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">"
                + "                                <tbody>"
                + "                                    <tr>"
                + "                                        <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: gainsboro\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">"
                + "                                                            <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">"
                + "                                                                    <u></u><u></u></span></p>"
                + "                                                        </td>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><u></u> <u></u></p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: white\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <div class=\"container\" style=\"max-width: 600px;margin: 0 auto;padding: 40px;background-color: white;"
                + "                                                        border: 3px solid black;\">"
                + "                                                            <div class=\"header\" style=\" display: flex;"
                + "                                                            justify-content: space-between;"
                + "                                                            align-items: center;\">"
                + "                                                                <div class=\"info-column\" style=\" flex: 2;"
                + "                                                                text-align: right;\">"
                + "                                                                </div>"
                + "                                                                <div class=\"\" style=\"flex: 2;"
                + "                                                                text-align: center; text-align: right;"
                + "                                                                margin-left: 10%;\">"
                + "                                                                    <img src='" + this.emailConfigData.getRMPAYLogo() +"'" 
                + "                                                                        alt='Logo' class='logo' style=\"max-width: 300px;"
                + "                                                                        height: auto;"
                + "                                                                        margin: 0 auto;\">"
                + "                                                                </div>"
                + "                                                            </div><br>"
                + "                                                            <u>RECIBO #" + emailData.getInvoiceNumber() + " DE PAGO VIA TARJETA DE CREDITO</u>"
                + "                                                            <br>"
                + "                                                             <div style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">"
                + "                                                                <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\"> "
                + "                                                                    <p>Fecha de solicitud: " + fechaActual.toString() + "</p>"
                + "                                                                </div>"
                + "                                                            </div>"
                + "                                                               "
                + "                                                           "
                + "                                                            <u>Información de Cliente:</u>"
                + "                                                            <br><br>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                               "
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right; margin:0px;\"><strong>NOMBRE: </strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" + emailData.getName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right;margin:0px\"><strong>NEGOCIO: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" + emailData.getBusinessName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%; text-align:right;margin:0px\"><strong># MERCHANT: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px;\">" + emailData.getMerchantId() + ",</p>"
                + "                                                                </div> <br>"
                + "                                                            <div style=\" margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">"
                + "                                                                <u>Servicios solicitados:</u><p style=\"margin:0 0 0 10px;\"><strong>" + mensageServicio + "</strong> </p>"
                + "                                                            </div>"
                + "                                                           "
                + "                                                            <div style=\"margin-right: 0px;width: 100%;margin-left: -15px; display: flex;flex-wrap: wrap; \">"
                + "                                                                <div  style=\"flex: 0 0 100%; max-width: 100%;width: 100%; text-align:right; margin:0px;\">";
        if (emailData.getRejectedPayments().isEmpty()) {
            for(TerminalsDoPaymentDTO terminal : emailData.getTerminalsDoPayment()) {
                message=message+"<p><strong>" + terminal.getServiceDescription() + "</strong></p>";
                
            }
            if(emailData.getDiscount()!=0.0) {
                message=message+"<p><strong>DESCUENTO: -$" + String.valueOf(formato.format(emailData.getDiscount())) + "</strong></p>";
            }
            message=message+"<p><strong>- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -</strong></p>";
            message=message+"<p><strong>SUBTOTAL DE PAGO: $" + String.valueOf(formato.format(emailData.getSubTotal())) + "</strong></p>";
            message=message+"<p><strong>STATE TAX 4%: $" + String.valueOf(formato.format(emailData.getStateTax())) + "</strong></p>";
            message=message+"<p><strong><u>__________________________________</u></strong></p>";
            message=message+"<p><strong>TOTAL DE PAGO: $" + String.valueOf(formato.format(emailData.getAmount())) + "</strong></p>";
        } else {
            for (int i = 0; i < emailData.getRejectedPayments().size(); i++) {
                message = message + "                                               <p><strong>VALOR FACTURA ANTERIOR #" + emailData.getRejectedPayments().get(i).getInvoiceNumber() + ": $" + emailData.getRejectedPayments().get(i).getTotalAmount() + "</strong></p>"
                        + "                                                                <p><strong>PAGO RECHAZADO: $" + formato.format(25.00) + "<br><u>__________________________________</u></strong></p>"
                        + "                                                                <p><strong>TOTAL DE FACTURA: $" + (emailData.getRejectedPayments().get(i).getTotalAmount() + 25.00) + "</strong></p><br>";
            }
            message = message + "                                                    <p><strong>TOTAL DE PAGO: $" + (emailData.getAmount()) + "</strong></p><br>";
        }
        message = message + "                                                   </div>"
                + "                                                            </div>"                                                         
                + "                                                            <br><br>"
                + "                                                            <u>Información de Pago:</u>"
                + "                                                            <div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Método de Pago</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">Tarjeta de Crédito</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">  "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Nombre de la tarjeta:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getNameoncard() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Número de Tarjeta de Crédito:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getCreditcarnumber() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Fecha de Expiración:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + mes + " " + emailData.getExpDateYear() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>CVV2:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getSecuritycode() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Tipo de Tarjeta:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getCardType() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Referencia de Pago:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getReferenceNumber() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Total pagado:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">$" + emailData.getAmount() + "</p>"
                + "                                                            </div>"
                + "                                                            <div class=\"content\" style=\"margin-top: 20px;"
                + "                                                            text-align: justify;\">"
                + "                                                                <p>"
                + "                                                                    Quedamos a su disposición para cualquier consulta que pueda hacernos. No dude en ponerse en contacto con nuestro"
                + "                                                                    equipo de soporte al cliente en <a href=\"mailto:info@retailmanagerpr.com\">info@retailmanagerpr.com</a> o al 1-787-466-2091 en cualquier momento."
                + "                                                                    <br><br>"
                + "                                                                    Atentamente,"
                + "                                                                </p>"
                + "                                                            </div>"
                + "                                                            <div style=\"display: flex;"
                + "                                                            align-items: center;"
                + "                                                            margin-top: 0px;\">"
                + "                                                                <div class=\"logo-column\" style=\"flex: 1;"
                + "                                                                text-align: center;"
                + "                                                                margin-left: 40%;\">"
                + "                                                                    <img src=\"" + this.emailConfigData.getRMLogo() +"\"" 
                + "                                                                        alt=\"Logo 1\" class=\"logo\" style=\"max-width: 170px;"
                + "                                                                        height: auto;"
                + "                                                                        margin: 0 auto;\">"
                + "                                                                </div>"
                + "                                                                <div class=\"info-column\" style=\" flex: 1;"
                + "                                                                text-align: center; margin-left: 0;\">"
                + "                                                                    <strong>"
                + "                                                                        <p>787-466-2091 <br>"
                + "                                                                            601 Ave. Andalucia<br>"
                + "                                                                            San Juan PR 00920</p>"
                + "                                                                    </strong>"
                + "                                                                </div>"
                + "                                                            </div>"
                + "                                                            <div class=\"info-column\" style=\" flex: 1;"
                + "                                                                text-align: center; margin-left: 0;\">"
                + "                                                                    <p>Copyright © IvuControlPR Todos los derechos reservados.</p>"
                + "                                                            </div>"
                + "                                                        </div>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%; background: #666c74\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td width=\"100%\" valign=\"top\""
                + "                                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">"
                + "                                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td style=\"padding: 5.25pt 0in 0in 0in\">"
                + "                                                                <p class=\"v1MsoNormal\"><span"
                + "                                                                        style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por"
                + "                                                                        favor no responder a este email. Los correos"
                + "                                                                        electrónicos enviados a esta dirección no serán"
                + "                                                                        respondidos. <br /><br />Copyright ©"
                + "                                                                        IvuControlPR Todos los derechos"
                + "                                                                        reservados.</span><u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + "                                            <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\">"
                + "                                                <u></u><u></u>"
                + "                                            </p>"
                + "                                        </td>"
                + "                                    </tr>"
                + "                                </tbody>"
                + "                            </table>"
                + "                        </div>"
                + "                    </td>"
                + "                </tr>"
                + "            </tbody>"
                + "        </table>"
                + "    </div>"
                + "</body>"
                + "</html>";
        return message;
    }
    /**
     * Creates an HTML email message for a payment token.
     *
     * @param  emailData   the email body data used to populate the message
     * @return             the formatted HTML email message
     */
    private String createBodyPaymentToken(EmailBodyData emailData) {
        LocalDate fechaActual = LocalDate.now();
        String mes = "";
        switch (emailData.getExpDateMonth()) {
            case "1":
                mes = "Enero";
                break;
            case "2":
                mes = "Febrero";
                break;
            case "3":
                mes = "Marzo";
                break;
            case "4":
                mes = "Abril";
                break;
            case "5":
                mes = "Mayo";
                break;
            case "6":
                mes = "Junio";
                break;
            case "7":
                mes = "Julio";
                break;
            case "8":
                mes = "Agosto";
                break;
            case "9":
                mes = "Septiembre";
                break;
            case "10":
                mes = "Octubre";
                break;
            case "11":
                mes = "Noviembre";
                break;
            case "12":
                mes = "Diciembre";
                break;
        }
        DecimalFormat formato = new DecimalFormat("0.00");

        String mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ")";
        if (emailData.isAutomaticPayments()) {
            mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ") y pagos siguientes automatizados";
        }
        String message = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "    <style>"
                + "        .row{"
                + "            margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;"
                + "        }"
                + "        .col1{"
                + "            flex: 0 0 41.66667%;max-width: 41.66667%; text-align:right; margin:0px;"
                + "        }"
                + "        .col2{"
                + "            flex: 0 0 41.66667%;max-width: 41.66667%;text-align:left;margin:0 0 0 10px"
                + "        }"
                + "    </style>"
                + "</head>"
                + "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;\">"
                + "    <div>"
                + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">"
                + "            <tbody>"
                + "                <tr>"
                + "                    <td style=\"padding: 0in 0in 0in 0in\">"
                + "                        <div align=\"center\">"
                + "                            <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">"
                + "                                <tbody>"
                + "                                    <tr>"
                + "                                        <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: gainsboro\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">"
                + "                                                            <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">"
                + "                                                                    <u></u><u></u></span></p>"
                + "                                                        </td>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><u></u> <u></u></p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: white\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <div class=\"container\" style=\"max-width: 600px;margin: 0 auto;padding: 40px;background-color: white;"
                + "                                                        border: 3px solid black;\">"
                + "                                                            <div class=\"header\" style=\" display: flex;"
                + "                                                            justify-content: space-between;"
                + "                                                            align-items: center;\">"
                + "                                                                <div class=\"info-column\" style=\" flex: 2;"
                + "                                                                text-align: right;\">"
                + "                                                                </div>"
                + "                                                                <div class=\"\" style=\"flex: 2;"
                + "                                                                text-align: center; text-align: right;"
                + "                                                                margin-left: 10%;\">"
                + "                                                                    <img src='" + this.emailConfigData.getRMPAYLogo() +"'" 
                + "                                                                        alt='Logo' class='logo' style=\"max-width: 300px;"
                + "                                                                        height: auto;"
                + "                                                                        margin: 0 auto;\">"
                + "                                                                </div>"
                + "                                                            </div><br>"
                + "                                                            <u>RECIBO #" + emailData.getInvoiceNumber() + " DE PAGO VIA TOKEN DE PAGO</u>"
                + "                                                            <br>"
                + "                                                             <div style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">"
                + "                                                                <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\"> "
                + "                                                                    <p>Fecha de solicitud: " + fechaActual.toString() + "</p>"
                + "                                                                </div>"
                + "                                                            </div>"
                + "                                                               "
                + "                                                           "
                + "                                                            <u>Información de Cliente:</u>"
                + "                                                            <br><br>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                               "
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right; margin:0px;\"><strong>NOMBRE: </strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" + emailData.getName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right;margin:0px\"><strong>NEGOCIO: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" + emailData.getBusinessName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%; text-align:right;margin:0px\"><strong># MERCHANT: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px;\">" + emailData.getMerchantId() + ",</p>"
                + "                                                                </div> <br>"
                + "                                                            <div style=\" margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">"
                + "                                                                <u>Servicios solicitados:</u><p style=\"margin:0 0 0 10px;\"><strong>" + mensageServicio + "</strong> </p>"
                + "                                                            </div>"
                + "                                                           "
                + "                                                            <div style=\"margin-right: 0px;width: 100%;margin-left: -15px; display: flex;flex-wrap: wrap; \">"
                + "                                                                <div  style=\"flex: 0 0 100%; max-width: 100%;width: 100%; text-align:right; margin:0px;\">";
        if (emailData.getRejectedPayments().isEmpty()) {
            for(TerminalsDoPaymentDTO terminal : emailData.getTerminalsDoPayment()) {
                message=message+"<p><strong>" + terminal.getServiceDescription() + "</strong></p>";
                
            }
            if(emailData.getDiscount()!=0.0) {
                message=message+"<p><strong>DESCUENTO: -$" + String.valueOf(formato.format(emailData.getDiscount())) + "</strong></p>";
            }
            message=message+"<p><strong>- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -</strong></p>";
            message=message+"<p><strong>SUBTOTAL DE PAGO: $" + String.valueOf(formato.format(emailData.getSubTotal())) + "</strong></p>";
            message=message+"<p><strong>STATE TAX 4%: $" + String.valueOf(formato.format(emailData.getStateTax())) + "</strong></p>";
            message=message+"<p><strong><u>__________________________________</u></strong></p>";
            message=message+"<p><strong>TOTAL DE PAGO: $" + String.valueOf(formato.format(emailData.getAmount())) + "</strong></p>";
        } else {
            for (int i = 0; i < emailData.getRejectedPayments().size(); i++) {
                message = message + "                                               <p><strong>VALOR FACTURA ANTERIOR #" + emailData.getRejectedPayments().get(i).getInvoiceNumber() + ": $" + emailData.getRejectedPayments().get(i).getTotalAmount() + "</strong></p>"
                        + "                                                                <p><strong>PAGO RECHAZADO: $" + formato.format(25.00) + "<br><u>__________________________________</u></strong></p>"
                        + "                                                                <p><strong>TOTAL DE FACTURA: $" + (emailData.getRejectedPayments().get(i).getTotalAmount() + 25.00) + "</strong></p><br>";
            }
            message = message + "                                                    <p><strong>TOTAL DE PAGO: $" + (emailData.getAmount()) + "</strong></p><br>";
        }
        message = message + "                                                   </div>"
                + "                                                            </div>"                                                         
                + "                                                            <br><br>"
                + "                                                            <u>Información de Pago:</u>"
                + "                                                            <div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Método de Pago</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">TOKEN DE PAGO</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">  "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Nombre de la tarjeta:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getNameoncard() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Número de Tarjeta de Crédito:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getCreditcarnumber() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Fecha de Expiración:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + mes + " " + emailData.getExpDateYear() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Referencia de Pago:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getReferenceNumber() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Total pagado:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">$" + emailData.getAmount() + "</p>"
                + "                                                            </div>"
                + "                                                            <div class=\"content\" style=\"margin-top: 20px;"
                + "                                                            text-align: justify;\">"
                + "                                                                <p>"
                + "                                                                    Quedamos a su disposición para cualquier consulta que pueda hacernos. No dude en ponerse en contacto con nuestro"
                + "                                                                    equipo de soporte al cliente en <a href=\"mailto:info@retailmanagerpr.com\">info@retailmanagerpr.com</a> o al 1-787-466-2091 en cualquier momento."
                + "                                                                    <br><br>"
                + "                                                                    Atentamente,"
                + "                                                                </p>"
                + "                                                            </div>"
                + "                                                            <div style=\"display: flex;"
                + "                                                            align-items: center;"
                + "                                                            margin-top: 0px;\">"
                + "                                                                <div class=\"logo-column\" style=\"flex: 1;"
                + "                                                                text-align: center;"
                + "                                                                margin-left: 40%;\">"
                + "                                                                    <img src=\"" + this.emailConfigData.getRMLogo() +"\"" 
                + "                                                                        alt=\"Logo 1\" class=\"logo\" style=\"max-width: 170px;"
                + "                                                                        height: auto;"
                + "                                                                        margin: 0 auto;\">"
                + "                                                                </div>"
                + "                                                                <div class=\"info-column\" style=\" flex: 1;"
                + "                                                                text-align: center; margin-left: 0;\">"
                + "                                                                    <strong>"
                + "                                                                        <p>787-466-2091 <br>"
                + "                                                                            601 Ave. Andalucia<br>"
                + "                                                                            San Juan PR 00920</p>"
                + "                                                                    </strong>"
                + "                                                                </div>"
                + "                                                            </div>"
                + "                                                            <div class=\"info-column\" style=\" flex: 1;"
                + "                                                                text-align: center; margin-left: 0;\">"
                + "                                                                    <p>Copyright © IvuControlPR Todos los derechos reservados.</p>"
                + "                                                            </div>"
                + "                                                        </div>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%; background: #666c74\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td width=\"100%\" valign=\"top\""
                + "                                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">"
                + "                                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td style=\"padding: 5.25pt 0in 0in 0in\">"
                + "                                                                <p class=\"v1MsoNormal\"><span"
                + "                                                                        style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por"
                + "                                                                        favor no responder a este email. Los correos"
                + "                                                                        electrónicos enviados a esta dirección no serán"
                + "                                                                        respondidos. <br /><br />Copyright ©"
                + "                                                                        IvuControlPR Todos los derechos"
                + "                                                                        reservados.</span><u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + "                                            <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\">"
                + "                                                <u></u><u></u>"
                + "                                            </p>"
                + "                                        </td>"
                + "                                    </tr>"
                + "                                </tbody>"
                + "                            </table>"
                + "                        </div>"
                + "                    </td>"
                + "                </tr>"
                + "            </tbody>"
                + "        </table>"
                + "    </div>"
                + "</body>"
                + "</html>";
        return message;
    }
    
    private String createBodyEmailDiscount(EmailBodyData emailData) {
        LocalDate fechaActual = LocalDate.now();
       
        
        DecimalFormat formato = new DecimalFormat("#.00");

        String mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ")";
        if (emailData.isAutomaticPayments()) {
            mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ") y pagos siguientes automatizados";
        }
        String message = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "    <style>"
                + "        .row{"
                + "            margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;"
                + "        }"
                + "        .col1{"
                + "            flex: 0 0 41.66667%;max-width: 41.66667%; text-align:right; margin:0px;"
                + "        }"
                + "        .col2{"
                + "            flex: 0 0 41.66667%;max-width: 41.66667%;text-align:left;margin:0 0 0 10px"
                + "        }"
                + "    </style>"
                + "</head>"
                + "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;\">"
                + "    <div>"
                + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">"
                + "            <tbody>"
                + "                <tr>"
                + "                    <td style=\"padding: 0in 0in 0in 0in\">"
                + "                        <div align=\"center\">"
                + "                            <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">"
                + "                                <tbody>"
                + "                                    <tr>"
                + "                                        <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: gainsboro\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">"
                + "                                                            <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">"
                + "                                                                    <u></u><u></u></span></p>"
                + "                                                        </td>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><u></u> <u></u></p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: white\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <div class=\"container\" style=\"max-width: 600px;margin: 0 auto;padding: 40px;background-color: white;"
                + "                                                        border: 3px solid black;\">"
                + "                                                            <div class=\"header\" style=\" display: flex;"
                + "                                                            justify-content: space-between;"
                + "                                                            align-items: center;\">"
                + "                                                                <div class=\"info-column\" style=\" flex: 2;"
                + "                                                                text-align: right;\">"
                + "                                                                </div>"
                + "                                                                <div class=\"\" style=\"flex: 2;"
                + "                                                                text-align: center; text-align: right;"
                + "                                                                margin-left: 10%;\">"
                + "                                                                    <img src='" + this.emailConfigData.getRMPAYLogo() +"'" 
                + "                                                                        alt='Logo' class='logo' style=\"max-width: 300px;"
                + "                                                                        height: auto;"
                + "                                                                        margin: 0 auto;\">"
                + "                                                                </div>"
                + "                                                            </div><br>"
                + "                                                            <u>RECIBO #" + emailData.getInvoiceNumber() + " DE PAGO VIA DESCUENTO DISPONIBLE</u>"
                + "                                                            <br>"
                + "                                                             <div style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">"
                + "                                                                <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\"> "
                + "                                                                    <p>Fecha de solicitud: " + fechaActual.toString() + "</p>"
                + "                                                                </div>"
                + "                                                            </div>"
                + "                                                               "
                + "                                                           "
                + "                                                            <u>Información de Cliente:</u>"
                + "                                                            <br><br>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                               "
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right; margin:0px;\"><strong>NOMBRE: </strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" + emailData.getName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right;margin:0px\"><strong>NEGOCIO: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" + emailData.getBusinessName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%; text-align:right;margin:0px\"><strong># MERCHANT: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px;\">" + emailData.getMerchantId() + ",</p>"
                + "                                                                </div> <br>"
                + "                                                            <div style=\" margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">"
                + "                                                                <u>Servicios solicitados:</u><p style=\"margin:0 0 0 10px;\"><strong>" + mensageServicio + "</strong> </p>"
                + "                                                            </div>"
                + "                                                           "
                + "                                                            <div style=\"margin-right: 0px;width: 100%;margin-left: -15px; display: flex;flex-wrap: wrap; \">"
                + "                                                                <div  style=\"flex: 0 0 100%; max-width: 100%;width: 100%; text-align:right; margin:0px;\">";
        if (emailData.getRejectedPayments().isEmpty()) {
            for(TerminalsDoPaymentDTO terminal : emailData.getTerminalsDoPayment()) {
                message=message+"<p><strong>" + terminal.getServiceDescription() + "</strong></p>";
                
            }
            if(emailData.getDiscount()!=0.0) {
                message=message+"<p><strong>DESCUENTO: $" + String.valueOf(formato.format(emailData.getDiscount())) + "</strong></p>";
            }
            message=message+"<p><strong>- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -</strong></p>";
            message=message+"<p><strong>SUBTOTAL DE PAGO: $" + String.valueOf(formato.format(emailData.getSubTotal())) + "</strong></p>";
            message=message+"<p><strong>STATE TAX 4%: $" + String.valueOf(formato.format(emailData.getStateTax())) + "</strong></p>";
                message=message+"<p><strong><u>__________________________________</u></strong></p>";
            message=message+"<p><strong>TOTAL DE PAGO: $" + String.valueOf(formato.format(emailData.getAmount())) + "</strong></p>";
        } else {
            for (int i = 0; i < emailData.getRejectedPayments().size(); i++) {
                message = message + "                                               <p><strong>VALOR FACTURA ANTERIOR #" + emailData.getRejectedPayments().get(i).getInvoiceNumber() + ": $" + emailData.getRejectedPayments().get(i).getTotalAmount() + "</strong></p>"
                        + "                                                                <p><strong>PAGO RECHAZADO: $" + formato.format(25.00) + "<br><u>__________________________________</u></strong></p>"
                        + "                                                                <p><strong>TOTAL DE FACTURA: $" + (emailData.getRejectedPayments().get(i).getTotalAmount() + 25.00) + "</strong></p><br>";
            }
            message = message + "                                                    <p><strong>TOTAL DE PAGO: $" + (emailData.getAmount()) + "</strong></p><br>";
        }
        message = message + "                                                   </div>"
                + "                                                            </div>"                                                         
                + "                                                            <br><br>"
                + "                                                            <u>Información de Pago:</u>"
                + "                                                            <div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Método de Pago</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">DESCUENTO</p>"
                + "                                                            </div>"
                + "                                                            <div class=\"content\" style=\"margin-top: 20px;"
                + "                                                            text-align: justify;\">"
                + "                                                                <p>"
                + "                                                                    Quedamos a su disposición para cualquier consulta que pueda hacernos. No dude en ponerse en contacto con nuestro"
                + "                                                                    equipo de soporte al cliente en <a href=\"mailto:info@retailmanagerpr.com\">info@retailmanagerpr.com</a> o al 1-787-466-2091 en cualquier momento."
                + "                                                                    <br><br>"
                + "                                                                    Atentamente,"
                + "                                                                </p>"
                + "                                                            </div>"
                + "                                                            <div style=\"display: flex;"
                + "                                                            align-items: center;"
                + "                                                            margin-top: 0px;\">"
                + "                                                                <div class=\"logo-column\" style=\"flex: 1;"
                + "                                                                text-align: center;"
                + "                                                                margin-left: 40%;\">"
                + "                                                                    <img src=\"" + this.emailConfigData.getRMLogo() +"\"" 
                + "                                                                        alt=\"Logo 1\" class=\"logo\" style=\"max-width: 170px;"
                + "                                                                        height: auto;"
                + "                                                                        margin: 0 auto;\">"
                + "                                                                </div>"
                + "                                                                <div class=\"info-column\" style=\" flex: 1;"
                + "                                                                text-align: center; margin-left: 0;\">"
                + "                                                                    <strong>"
                + "                                                                        <p>787-466-2091 <br>"
                + "                                                                            601 Ave. Andalucia<br>"
                + "                                                                            San Juan PR 00920</p>"
                + "                                                                    </strong>"
                + "                                                                </div>"
                + "                                                            </div>"
                + "                                                            <div class=\"info-column\" style=\" flex: 1;"
                + "                                                                text-align: center; margin-left: 0;\">"
                + "                                                                    <p>Copyright © IvuControlPR Todos los derechos reservados.</p>"
                + "                                                            </div>"
                + "                                                        </div>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%; background: #666c74\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td width=\"100%\" valign=\"top\""
                + "                                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">"
                + "                                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td style=\"padding: 5.25pt 0in 0in 0in\">"
                + "                                                                <p class=\"v1MsoNormal\"><span"
                + "                                                                        style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por"
                + "                                                                        favor no responder a este email. Los correos"
                + "                                                                        electrónicos enviados a esta dirección no serán"
                + "                                                                        respondidos. <br /><br />Copyright ©"
                + "                                                                        IvuControlPR Todos los derechos"
                + "                                                                        reservados.</span><u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + "                                            <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\">"
                + "                                                <u></u><u></u>"
                + "                                            </p>"
                + "                                        </td>"
                + "                                    </tr>"
                + "                                </tbody>"
                + "                            </table>"
                + "                        </div>"
                + "                    </td>"
                + "                </tr>"
                + "            </tbody>"
                + "        </table>"
                + "    </div>"
                + "</body>"
                + "</html>";
        return message;
    }
    /**
     * Creates an email body for a bank account with the provided data.
     *
     * @param  emailData  the email body data
     * @return            the constructed email message
     */
    private String createBodyEmailBankAccount(EmailBodyData emailData) {
        LocalDate fechaActual = LocalDate.now();
        String mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ")";
        if (emailData.isAutomaticPayments()) {
            mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ") y pagos siguientes automatizados";
        }

        DecimalFormat formato = new DecimalFormat("#.00");
        String message = "<!DOCTYPE html>"
                + "<html>"
                + ""
                + "<head>"
                + "   "
                + "</head>"
                + ""
                + "<body style=\"font-family: Arial, sans-serif;"
                + "margin: 0;"
                + "padding: 0;"
                + "background-color: #f4f4f4;\">"
                + "    <div>"
                + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">"
                + "            <tbody>"
                + "                <tr>"
                + "                    <td style=\"padding: 0in 0in 0in 0in\">"
                + "                        <div align=\"center\">"
                + "                            <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">"
                + "                                <tbody>"
                + "                                    <tr>"
                + "                                        <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: gainsboro\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">"
                + "                                                            <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">"
                + "                                                                    <u></u><u></u></span></p>"
                + "                                                        </td>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><u></u> <u></u></p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: white\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <div class=\"container\" style=\"max-width: 600px;"
                + "                                                        margin: 0 auto;"
                + "                                                        padding: 40px;"
                + "                                                        background-color: white;"
                + "                                                        border: 3px solid black;"
                + "                                                        /* Añade el borde negro */\">"
                + "                                                            <div class=\"header\" style=\" display: flex;"
                + "                                                            justify-content: space-between;"
                + "                                                            align-items: center;\">"
                + "                                                                <div class=\"info-column\" style=\" flex: 2;"
                + "                                                                text-align: right;\">"
                + "                                                                </div>"
                + "                                                                <div class=\"\" style=\"flex: 2;"
                + "                                                                text-align: center; text-align: right;"
                + "                                                                margin-left: 10%;\">"
                + "                                                                    <img src='" + this.emailConfigData.getRMPAYLogo() +"'" 
                + "                                                                        alt='Logo' class='logo' style=\"max-width: 300px;"
                + "                                                                        height: auto;"
                + "                                                                        margin: 0 auto; \">"
                + "                                                                </div>"
                + "                                                            </div><br>"
                + "                                                            <u>RECIBO #" + emailData.getInvoiceNumber() + " DE PAGO VIA CUENTA BANCARIA</u>"
                + "                                                            <br><br>"
                + "                                                            <div style=\"width:100%;margin-right: 0px; display: flex;flex-wrap: wrap; \">"
                + "                                                                <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\">"
                + "                                                                    <p>Fecha de solicitud: " + fechaActual.toString() + "</p>"
                + "                                                                </div>"
                + "                                                            </div>"
                + "                                                            <br>"
                + "                                                            <u>Información de Cliente:</u>"
                + "                                                            <br><br>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                               "
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right; margin:0px;\"><strong>NOMBRE: </strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" + emailData.getName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right;margin:0px\"><strong>NEGOCIO: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" + emailData.getBusinessName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%; text-align:right;margin:0px\"><strong># MERCHANT: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px;\">" + emailData.getMerchantId() + ",</p>"
                + "                                                                </div> <br>"
                + "                                                            <div style=\" margin-right: 0px; display: flex;flex-wrap: wrap;\">"
                + "                                                                <u>Servicios solicitados:</u><p style=\"margin:0 0 0 10px;\"><strong>" + mensageServicio + "</strong> </p>"
                + "                                                            </div>"
                + "                                                           "
                + "                                                            <div style=\"width:100%;margin-right: 0px; display: flex;flex-wrap: wrap; \">"
                + "                                                                <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\">";
        if (emailData.getRejectedPayments().isEmpty()) {
            for(TerminalsDoPaymentDTO terminal : emailData.getTerminalsDoPayment()) {
                message=message+"<p><strong>" + terminal.getServiceDescription() + "</strong></p>";
                
            }
            if(emailData.getDiscount()!=0.0) {
                
                message=message+"<p><strong>DESCUENTO: $" + String.valueOf(formato.format(emailData.getDiscount())) + "</strong></p>";
            }
            message=message+"<p><strong>- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -</strong></p>";
            message=message+"<p><strong>SUBTOTAL DE PAGO: $" + String.valueOf(formato.format(emailData.getSubTotal())) + "</strong></p>";
            message=message+"<p><strong>STATE TAX 4%: $" + String.valueOf(formato.format(emailData.getStateTax())) + "</strong></p>";
                message=message+"<p><strong><u>__________________________________</u></strong></p>";
            message=message+"<p><strong>TOTAL DE PAGO: $" + String.valueOf(formato.format(emailData.getAmount())) + "</strong></p>";
        } else {
            for (int i = 0; i < emailData.getRejectedPayments().size(); i++) {
                message = message + "                                               <p><strong>VALOR FACTURA ANTERIOR #" + emailData.getRejectedPayments().get(i).getInvoiceNumber() + ": $" + emailData.getRejectedPayments().get(i).getTotalAmount() + "</strong></p>"
                        + "                                                                <p><strong>PAGO RECHAZADO: $" + formato.format(25.00) + "<br><u>__________________________________</u></strong></p>"
                        + "                                                                <p><strong>TOTAL DE FACTURA: $" + (emailData.getRejectedPayments().get(i).getTotalAmount() + 25.00) + "</strong></p><br>";
            }
            message = message + "                                                    <p><strong>TOTAL DE PAGO: $" + (emailData.getAmount()) + "</strong></p><br>";
        }

        message = message + "                                             </div>"
                + "                                                            </div>"
                + "                                                            <br><br>"
                + "                                                            <u>Información de Pago:</u><br><br>"
                + "                                                           "
                + "                                                            <div style=\" width:100%;margin-right: 0px; display: flex;flex-wrap: wrap;\">"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Método de Pago: </strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">Cuenta Bancaria</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px; display: flex;flex-wrap: wrap;\">  "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Nombre de cuenta: </strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getAccountNameBank() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Número de cuenta:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getAccountNumberBank() + "</p>"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px; display: flex;flex-wrap: wrap;\"> "
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Número de ruta y transito:</strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getRouteNumberBank() + "</p>"
                + "                                                            </div>"
                + ""
                + "                                                            <div class=\"content\" style=\"margin-top: 20px;"
                + "                                                            text-align: justify;\">"
                + "                                                                <p>"
                + "                                                                    El pago estará siendo procesado por el equipo de contabilidad, una vez esté confirmado el mismo sera aplicado a su cuenta."
                + "                                                                    <br><br>"
                + "                                                                    Los pagos por ACH pueden tardar de 3 a 5 dias laborales para reflejarse en su cuenta de IVU control."
                + "                                                                    <br><br>"
                + "                                                                    Quedamos a su disposición para cualquier consulta que pueda tener. No dude en ponerse en contacto con nuestro"
                + "                                                                    equipo de soporte al cliente en <a href=\"mailto:info@retailmanagerpr.com\">info@retailmanagerpr.com</a> o al 1-787-466-2091 en cualquier momento."
                + "                                                                    <br><br>"
                + "                                                                    Atentamente,"
                + "                                                                </p>"
                + ""
                + "                                                            </div>"
                + "                                                            <div style=\"display: flex;"
                + "                                                           "
                + "                                                            align-items: center;"
                + "                                                            margin-top: 0px;\">"
                + ""
                + "                                                                <div class=\"logo-column\" style=\"flex: 1;"
                + "                                                                text-align: center;"
                + "                                                                 margin-left: 40%;\">"
                + "                                                                   <img src=\"" + this.emailConfigData.getRMLogo() +"\"" 
                + "                                                                        alt=\"Logo 1\" class=\"logo\" style=\" max-width: 170px;"
                + "                                                                        height: auto;"
                + "                                                                        margin: 0 auto;\">"
                + "                                                                </div>"
                + "                                                                <div class=\"info-column\" style=\" flex: 1;"
                + "                                                                text-align: center; margin-left: 0;\">"
                + "                                                                    <strong>"
                + "                                                                        <p>787-466-2091 <br>"
                + "                                                                            601 Ave. Andalucia<br>"
                + "                                                                            San Juan PR 00920</p>"
                + "                                                                    </strong>"
                + ""
                + "                                                                </div>"
                + "                                                            </div>"
                + "                                                            <div class=\"info-column\" style=\" flex: 1;"
                + "                                                                text-align: center; margin-left: 0;\">"
                + "                                                                        <p>Copyright © IvuControlPR Todos los derechos reservados.</p>"
                + "                                                                </div>"
                + "                                                        </div>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%; background: #666c74\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td width=\"100%\" valign=\"top\""
                + "                                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">"
                + "                                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + ""
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td style=\"padding: 5.25pt 0in 0in 0in\">"
                + "                                                                <p class=\"v1MsoNormal\"><span"
                + "                                                                        style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por"
                + "                                                                        favor no responder a este email. Los correos"
                + "                                                                        electrónicos enviados a esta dirección no serán"
                + "                                                                        respondidos. <br /><br />Copyright ©"
                + "                                                                        IvuControlPR Todos los derechos"
                + "                                                                        reservados.</span><u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + "                                            <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\">"
                + "                                                <u></u><u></u>"
                + "                                            </p>"
                + "                                        </td>"
                + "                                    </tr>"
                + "                                </tbody>"
                + "                            </table>"
                + "                        </div>"
                + "                    </td>"
                + "                </tr>"
                + "            </tbody>"
                + "        </table>"
                + "    </div>"
                + ""
                + "</body>"
                + ""
                + "</html>";
        return message;
    }
    
    /**
     * Creates the body of an email for ATH Movil.
     *
     * @param  emailData   the data needed to create the email body
     * @return             the HTML body of the email
     */
    private String createBodyEmailATHMovil(EmailBodyData emailData) {
        LocalDate fechaActual = LocalDate.now();
        DecimalFormat formato = new DecimalFormat("#.00");
        String message = "<!DOCTYPE html>"
                + "<html>"
                + ""
                + "<head>"
                + "   "
                + "</head>"
                + ""
                + "<body style=\"font-family: Arial, sans-serif;"
                + "margin: 0;"
                + "padding: 0;"
                + "background-color: #f4f4f4;\">"
                + "    <div>"
                + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">"
                + "            <tbody>"
                + "                <tr>"
                + "                    <td style=\"padding: 0in 0in 0in 0in\">"
                + "                        <div align=\"center\">"
                + "                            <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">"
                + "                                <tbody>"
                + "                                    <tr>"
                + "                                        <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: gainsboro\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">"
                + "                                                            <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">"
                + "                                                                    <u></u><u></u></span></p>"
                + "                                                        </td>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><u></u> <u></u></p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: white\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <div class=\"container\" style=\"max-width: 600px;"
                + "                                                        margin: 0 auto;"
                + "                                                        padding: 40px;"
                + "                                                        background-color: white;"
                + "                                                        border: 3px solid black;"
                + "                                                        /* Añade el borde negro */\">"
                + "                                                            <div class=\"header\" style=\" display: flex;"
                + "                                                            justify-content: space-between;"
                + "                                                            align-items: center;\">"
                + "                                                                <div class=\"info-column\" style=\" flex: 2;"
                + "                                                                text-align: right;\">"
                + "                                                                </div>"
                + "                                                                <div class=\"\" style=\"flex: 2;"
                + "                                                                text-align: center; text-align: right;"
                + "                                                                margin-left: 10%;\">"
                + "                                                                    <img src='" + this.emailConfigData.getRMPAYLogo() +"'" 
                + "                                                                        alt='Logo' class='logo' style=\"max-width: 300px;"
                + "                                                                        height: auto;"
                + "                                                                        margin: 0 auto; \">"
                + "                                                                </div>"
                + "                                                            </div><br>"
                + "                                                            <u>RECIBO #" + emailData.getInvoiceNumber() + " DE PAGO VIA <img src='https://www.ivucontrolpr.com/static/media/ath-movile-logo.png'"
                + "                                                                alt='Logo' class='logo' style=\"max-width: 100px;"
                + "                                                                height: auto;"
                + "                                                                margin: 0 auto; \"></u>"
                + "                                                                <div style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">"
                + "                                                                    <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\">"
                + "                                                                        <p>Fecha de solicitud: " + fechaActual.toString() + "</p>"
                + "                                                                    </div>"
                + "                                                                </div>"
                + "                                                            <u>Información de Cliente:</u>"
                + "                                                            <br><br>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                               "
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right; margin:0px;\"><strong>NOMBRE: </strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" + emailData.getName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right;margin:0px\"><strong>NEGOCIO: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" + emailData.getBusinessName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%; text-align:right;margin:0px\"><strong># MERCHANT: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px;\">" + emailData.getMerchantId() + ",</p>"
                + "                                                                </div> "
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%; text-align:right;margin:0px\"><strong>Referencia de pago: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px;\">" + emailData.getReferenceNumber() + ",</p>"
                + "                                                                </div> "
                + "                                                            <br><div style=\" margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">"
                + "                                                                <u>Servicios solicitados:</u>"
                + "                                                            </div>"
                + "                                                           "
                + "                                                            <div style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">"
                + "                                                                <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:center; margin:0px;\">";
//Valida si el pago corresponde a un pago rechazado
        if (emailData.getRejectedPayments().isEmpty()) {
            for(TerminalsDoPaymentDTO terminal : emailData.getTerminalsDoPayment()) {
                message=message+"<p><strong>" + terminal.getServiceDescription() + "</strong></p>";
                
            }
            if(emailData.getDiscount()!=0.0) {
                message=message+"<p><strong>DESCUENTO: $" + String.valueOf(formato.format(emailData.getDiscount())) + "</strong></p>";
            }
            message=message+"<p><strong>- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -</strong></p>";
            message=message+"<p><strong>SUBTOTAL DE PAGO: $" + String.valueOf(formato.format(emailData.getSubTotal())) + "</strong></p>";
            message=message+"<p><strong>STATE TAX 4%: $" + String.valueOf(formato.format(emailData.getStateTax())) + "</strong></p>";
                message=message+"<p><strong><u>__________________________________</u></strong></p>";
            message=message+"<p><strong>TOTAL DE PAGO: $" + String.valueOf(formato.format(emailData.getAmount())) + "</strong></p>";
        } else {
            for (int i = 0; i < emailData.getRejectedPayments().size(); i++) {
                message = message + "                                               <p><strong>VALOR FACTURA ANTERIOR #" + emailData.getRejectedPayments().get(i).getInvoiceNumber() + ": $" + emailData.getRejectedPayments().get(i).getTotalAmount() + "</strong></p>"
                        + "                                                                <p><strong>PAGO RECHAZADO: $" + formato.format(25.00) + "<br><u>__________________________________</u></strong></p>"
                        + "                                                                <p><strong>TOTAL DE FACTURA: $" + (emailData.getRejectedPayments().get(i).getTotalAmount() + 25.00) + "</strong></p><br>";
            }
            message = message + "                                                    <p><strong>TOTAL DE PAGO: $" + (emailData.getAmount()) + "</strong></p><br>";
        }
        message = message + "                                          </div>"
                + "                                                            </div>"
                + "                                                            <div class=\"content\" style=\"margin-top: 20px;"
                + "                                                            text-align: justify;\">"
                + "                                                                <p>"
                + "                                                                    El pago estará siendo revisado por el equipo de contabilidad, una vez esté confirmado el mismo sera aplicado a su cuenta."
                + "                                                                    <br><br>"
                + "                                                                    Los pagos por ATH móvil pueden tardar 1 o 2 dias laborales para reflejarse en su cuenta de IVU control."
                + "                                                                    <br><br>"
                + "                                                                    Quedamos a su disposición para cualquier consulta que pueda tener. No dude en ponerse en contacto con nuestro"
                + "                                                                    equipo de soporte al cliente en <a href=\"mailto:info@retailmanagerpr.com\">info@retailmanagerpr.com</a> o al 1-787-466-2091 en cualquier momento."
                + "                                                                    <br><br>"
                + "                                                                    Atentamente,"
                + "                                                                </p>"
                + ""
                + "                                                            </div>"
                + "                                                            <div style=\"display: flex;"
                + "                                                           "
                + "                                                            align-items: center;"
                + "                                                            margin-top: 0px;\">"
                + ""
                + "                                                                <div class=\"logo-column\" style=\"flex: 1;"
                + "                                                                text-align: center;"
                + "                                                                 margin-left: 40%;\">"
                + "                                                                    <img src=\"" + this.emailConfigData.getRMLogo() +"\"" 
                + "                                                                        alt=\"Logo 1\" class=\"logo\" style=\" max-width: 170px;"
                + "                                                                        height: auto;"
                + "                                                                        margin: 0 auto;\">"
                + "                                                                </div>"
                + "                                                                <div class=\"info-column\" style=\" flex: 1;"
                + "                                                                text-align: center; margin-left: 0;\">"
                + "                                                                    <strong>"
                + "                                                                        <p>787-466-2091 <br>"
                + "                                                                            601 Ave. Andalucia<br>"
                + "                                                                            San Juan PR 00920</p>"
                + "                                                                    </strong>"
                + ""
                + "                                                                </div>"
                + "                                                            </div>"
                + "                                                            <div class=\"info-column\" style=\" flex: 1;"
                + "                                                                text-align: center; margin-left: 0;\">"
                + "                                                                        <p>Copyright © IvuControlPR Todos los derechos reservados.</p>"
                + "                                                                </div>"
                + "                                                        </div>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%; background: #666c74\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td width=\"100%\" valign=\"top\""
                + "                                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">"
                + "                                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + ""
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td style=\"padding: 5.25pt 0in 0in 0in\">"
                + "                                                                <p class=\"v1MsoNormal\"><span"
                + "                                                                        style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por"
                + "                                                                        favor no responder a este email. Los correos"
                + "                                                                        electrónicos enviados a esta dirección no serán"
                + "                                                                        respondidos. <br /><br />Copyright ©"
                + "                                                                        IvuControlPR Todos los derechos"
                + "                                                                        reservados.</span><u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + "                                            <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\">"
                + "                                                <u></u><u></u>"
                + "                                            </p>"
                + "                                        </td>"
                + "                                    </tr>"
                + "                                </tbody>"
                + "                            </table>"
                + "                        </div>"
                + "                    </td>"
                + "                </tr>"
                + "            </tbody>"
                + "        </table>"
                + "    </div>"
                + ""
                + "</body>"
                + ""
                + "</html>";
        return message;
    }
    /**
     * A method to create a body registration error email message.
     *
     * @param  emailData   the email body data used to populate the message
     * @return             the formatted HTML email message
     */
    private String createBodyRegistrationError(EmailBodyData emailData){
        LocalDate fechaActual = LocalDate.now();
        String mes = "";
        switch (emailData.getExpDateMonth()) {
            case "1":
                mes = "Enero";
                break;
            case "2":
                mes = "Febrero";
                break;
            case "3":
                mes = "Marzo";
                break;
            case "4":
                mes = "Abril";
                break;
            case "5":
                mes = "Mayo";
                break;
            case "6":
                mes = "Junio";
                break;
            case "7":
                mes = "Julio";
                break;
            case "8":
                mes = "Agosto";
                break;
            case "9":
                mes = "Septiembre";
                break;
            case "10":
                mes = "Octubre";
                break;
            case "11":
                mes = "Noviembre";
                break;
            case "12":
                mes = "Diciembre";
                break;
        }
        String msg="<!DOCTYPE html>" +
"<html>" +
"" +
"<head> </head>" +
"" +
"<body style=\"font-family: Arial, sans-serif;margin: 0;padding: 0;background-color: #f4f4f4;\">" +
"   <div>" +
"      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">" +
"         <tbody>" +
"            <tr>" +
"               <td style=\"padding: 0in 0in 0in 0in\">" +
"                  <div align=\"center\">" +
"                     <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">" +
"                        <tbody>" +
"                           <tr>" +
"                              <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">" +
"                                 <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span> </p>" +
"                                 <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"" +
"                                    style=\"width: 100.0%; background: gainsboro\">" +
"                                    <tbody>" +
"                                       <tr>" +
"                                          <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">" +
"                                             <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">" +
"                                                   <u></u><u></u></span></p>" +
"                                          </td>" +
"                                       </tr>" +
"                                    </tbody>" +
"                                 </table>" +
"                                 <p class=\"v1MsoNormal\"><u></u> <u></u></p>" +
"                                 <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"" +
"                                    style=\"width: 100.0%; background: white\">" +
"                                    <tbody>" +
"                                       <tr>" +
"                                          <div class=\"container\"" +
"                                             style=\"max-width: 600px;                                                        margin: 0 auto;                                                        padding: 40px;                                                        background-color: white;                                                        border: 3px solid black;                                                        /* Añade el borde negro */\">" +
"                                             <div class=\"header\"" +
"                                                style=\" display: flex;                                                        justify-content: space-between;                                                        align-items: center;\">" +
"                                                <div class=\"info-column\"" +
"                                                   style=\" flex: 2;                                                        text-align: right;\">" +
"                                                </div>" +
"                                                <div class=\"\"" +
"                                                   style=\"flex: 2;                                                        text-align: center; text-align: right;                                                        margin-left: 10%;\">" +
"                                                   <img src='" + this.emailConfigData.getRMPAYLogo() +"'" +
"                                                      alt='Logo' class='logo'" +
"                                                      style=\"max-width: 300px;                                                                        height: auto;                                                                        margin: 0 auto; \">" +
"                                                </div>" +
"                                             </div><br> <u><strong>" + emailData.getSubject() + "</strong> </u>" +
"                                             <div" +
"                                                style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">" +
"                                                <div" +
"                                                   style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\">" +
"                                                   <p>Fecha de solicitud: " + fechaActual.toString() +"</p>"+
"                                                </div>" +
"                                             </div>" +
"                                             <u>Información de Cliente:</u> <br><br>" +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%; text-align:right; margin:0px;\">" +
"                                                   <strong>NOMBRE: </strong>" +
"                                                </p>" +
"                                                <p" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" +
"                                                   " + emailData.getName() +" ,</p>" +
"                                             </div>" +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%; text-align:right;margin:0px\">" +
"                                                   <strong>NEGOCIO: </strong>" +
"                                                </p>" +
"                                                <p class=\"col2\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" +
"                                                   " + emailData.getBusinessName() +",</p>" +
"                                             </div>" +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">" +
"                                                   <strong># MERCHANT: </strong>" +
"                                                </p>" +
"                                                <p class=\"col2\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">" +
"                                                   " + emailData.getMerchantId()+" ,</p>" +
"                                             </div> <br>" +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">" +
"                                                   <strong> SERVICIOS SOLICITADOS: </strong>" +
"                                                </p>" +
"                                                <p class=\"col2\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">" +
"                                                   " + emailData.getServiceDescription() +",</p>" +
"                                             </div> <br>" +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">" +
"                                                   <strong>CANTIDAD DE TERMINALES: </strong>" +
"                                                </p>" +
"                                                <p class=\"col2\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">" +
"                                                   " + emailData.getAdditionalTerminals() +",</p>" +
"                                             </div> <br>" +
"                                             <u>Información de Pago:</u> <br><br>" +
"                                             -paymethod-";
if(emailData.getPaymethod()!=null){
    if(emailData.getPaymethod().compareTo("TOKEN")==0 || emailData.getPaymethod().compareTo("CREDIT-CARD")==0){
        msg=msg+"<div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">  "
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Nombre de la tarjeta:</strong></p>"
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getNameoncard() + "</p>"
        + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Número de Tarjeta de Crédito:</strong></p>"
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getCreditcarnumber() + "</p>"
        + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Fecha de Expiración:</strong></p>"
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + mes + " " + emailData.getExpDateYear() + "</p>"
        + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Referencia de Pago:</strong></p>"
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getReferenceNumber() + "</p>"
        + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Total pagado:</strong></p>"
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">$" + emailData.getAmount() + "</p>"
        + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\"> "
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Error:</strong></p>"
        + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">$" + emailData.getErrorMessage() + "</p>"
        + "                                                            </div>";
    }
}
msg=msg+"                                             <p>Cordilmente, <br> Equipo de Soporte de RMPAY</p>" +
"                                             <div" +
"                                                style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">" +
"                                                <div" +
"                                                   style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:center; margin:0px;\">" +
"" +
"" +
"                                                   <div" +
"                                                      style=\"display: flex;" +
"                                                                                                              " +
"                                                                                                               align-items: center;" +
"                                                                                                               margin-top: 0px;\">" +
"                                                      <div class=\"logo-column\"" +
"                                                         style=\"flex: 1; text-align: center; margin-left: 40%;\">" +
"                                                         <img src=\"" + this.emailConfigData.getRMLogo() +"\"" +
"                                                            alt=\" Logo 1\" class=\"logo\" style=\" max-width: 170px;" +
"                                                            height: auto; margin: 0 auto;\">" +
"                                                      </div>" +
"                                                      <div class=\"info-column\"" +
"                                                         style=\" flex: 1;" +
"                                                                                                                      text-align: center; margin-left: 0;\">" +
"                                                         <strong>" +
"                                                            <p>787-466-2091 <br> 601 Ave. Andalucia<br> San" +
"                                                               Juan PR 00920</p>" +
"                                                         </strong>" +
"                                                      </div>" +
"                                                   </div>" +
"                                                   <div class=\"info-column\"" +
"                                                      style=\" flex: 1;" +
"                                                                                                                   text-align: center; margin-left: 0;\">" +
"                                                      <p>Copyright © IvuControlPR Todos los derechos reservados.</p>" +
"                                                   </div>" +
"                                                </div>" +
"                                       </tr>" +
"                                    </tbody>" +
"                                 </table>" +
"                                 <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span> </p>" +
"                                 <div align=\"center\">" +
"                                    <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"" +
"                                       style=\"width: 100.0%; background: #666c74\">" +
"                                       <tbody>" +
"                                          <tr>" +
"                                             <td width=\"100%\" valign=\"top\"" +
"                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">" +
"                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>" +
"                                             </td>" +
"                                          </tr>" +
"                                       </tbody>" +
"                                    </table>" +
"                                 </div>" +
"                                 <div align=\"center\">" +
"                                    <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"" +
"                                       style=\"width: 100.0%\">" +
"                                       <tbody>" +
"                                          <tr>" +
"                                             <td style=\"padding: 5.25pt 0in 0in 0in\">" +
"                                                <p class=\"v1MsoNormal\"><span" +
"                                                      style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por" +
"                                                      favor no responder a este email. Los correos electrónicos enviados" +
"                                                      a esta dirección no serán respondidos. <br /><br />Copyright ©" +
"                                                      IvuControlPR Todos los derechos reservados.</span><u></u><u></u>" +
"                                                </p>" +
"                                             </td>" +
"                                          </tr>" +
"                                       </tbody>" +
"                                    </table>" +
"                                 </div>" +
"                                 <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\"> <u></u><u></u> </p>" +
"                              </td>" +
"                           </tr>" +
"                        </tbody>" +
"                     </table>" +
"                  </div>" +
"               </td>" +
"            </tr>" +
"         </tbody>" +
"      </table>" +
"   </div>" +
"</body>" +
"" +
"</html>";
        return msg;
    }
    
    /**
     * Creates a body for a payment rejected email based on the provided email data.
     *
     * @param  emailData  the email data used to create the email body
     * @return           the HTML body for the payment rejected email
     */
    private String createBodyPaymentRejected(EmailBodyData emailData){
        if (emailData.getAdditionalTerminals() == 0) {
            emailData.setAdditionalTerminals(1);
        }
        DecimalFormat formato = new DecimalFormat("#.00");

        emailData.setAmount(emailData.getAmount() + 25);

        String body = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "</head>"
                + "<body style=\"font-family: Arial, sans-serif;"
                + "margin: 0;"
                + "padding: 0;"
                + "background-color: #f4f4f4;\">"
                + "    <div>"
                + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">"
                + "            <tbody>"
                + "                <tr>"
                + "                    <td style=\"padding: 0in 0in 0in 0in\">"
                + "                        <div align=\"center\">"
                + "                            <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">"
                + "                                <tbody>"
                + "                                    <tr>"
                + "                                        <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: gainsboro\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">"
                + "                                                            <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">"
                + "                                                                    <u></u><u></u></span></p>"
                + "                                                        </td>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><u></u> <u></u></p>"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                style=\"width: 100.0%; background: white\">"
                + "                                                <tbody>"
                + "                                                    <tr>"
                + "                                                        <div class=\"container\" style=\"max-width: 600px;"
                + "                                                        margin: 0 auto;"
                + "                                                        padding: 40px;"
                + "                                                        background-color: white;"
                + "                                                        border: 3px solid black;"
                + "                                                        /* Añade el borde negro */\">"
                + "                                                            <div class=\"header\" style=\" display: flex;"
                + "                                                            justify-content: space-between;"
                + "                                                            align-items: center;\">"
                + "                                                                <div class=\"info-column\" style=\" flex: 2;"
                + "                                                                text-align: right;\">"
                + "                                                                </div>"
                + "                                                                <div class=\"\" style=\"flex: 2;"
                + "                                                                text-align: center; text-align: right;"
                + "                                                                margin-left: 10%;\">"
                + "                                                                    <img src='" + this.emailConfigData.getRMPAYLogo() +"'" +
                "                                                                        alt='Logo' class='logo' style=\"max-width: 300px;"
                + "                                                                        height: auto;"
                + "                                                                        margin: 0 auto; \">"
                + "                                                                </div>"
                + "                                                            </div><br>"
                + "                                                            <u>Información de Cliente:</u>"
                + "                                                            <br><br>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right; margin:0px;\"><strong>NOMBRE: </strong></p>"
                + "                                                                <p style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" + emailData.getName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px; \">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%; text-align:right;margin:0px\"><strong>NEGOCIO: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" + emailData.getBusinessName() + ",</p>"
                + "                                                            </div>"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;\">"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%; text-align:right;margin:0px\"><strong># MERCHANT: </strong></p>"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;"
                + "                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px;\">" + emailData.getMerchantId() + ",</p>"
                + "                                                                </div> <br>"
                + "                                                            <div class=\"content\" style=\"margin-top: 20px;"
                + "                                                            text-align: justify;\">"
                + "                                                                <p>"
                + "                                                                    Estimado Cliente de ivucontrolpr.com,"
                + "                                                                    <br><br>"
                + "                                                                    Esperamos que se encuentre bien. Queremos informarle que recientemente se intento procesar un pago a traves de"
                + "                                                                    ACH en su cuenta, el cual lamentablemente fue rechazado. Como resultado se aplicara una penalidad de $25 por el pago rechazado,"
                + "                                                                    que se añadira al saldo de su cuenta."
                + "                                                                </p>"
                + "                                                                <div style=\"width:100%;margin-right: 0px; display: flex;flex-wrap: wrap; \">"
                + "                                                                    <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\">"
                + "                                                                        <p><strong>VALOR FACTURA ANTERIOR #" + emailData.getInvoiceNumber() + ": $" + formato.format(emailData.getAmount() - 25.00) + "</strong></p>"
                + "                                                                        <p><strong>PAGO RECHAZADO: $" + formato.format(25.00) + "<br><u>__________________________________</u></strong></p>"
                + "                                                                        <p><strong>TOTAL DE PAGO: $" + formato.format(emailData.getAmount()) + "</strong>"
                + "                                                                    </div>"
                + "                                                                </div>"
                + "                                                                <p>"
                + "                                                                    Para evitar futuros inconvenientes y asegurarnos de que su cuenta esté al dia, le solicitamos que nos indique"
                + "                                                                    cuándo podemos intentar nuevamente el pago a través de ACH o si tiene la intención de utilizar otro método de pago."
                + "                                                                    Su cooperación es fundamental para mantener su cuenta al corriente y sin interrupciones en los servicios que ofrecemos."
                + "                                                                    <br><br>"
                + "                                                                    Si tiene alguna pregunta o necesita asistencia adicional, no dude en ponerse en contacto con nuestro equipo de servicio al cliente."
                + "                                                                    Estamos aqui para ayudarle en todo lo que necesite."
                + "                                                                    <br><br>"
                + "                                                                    Agradecemos su atención a este asunto y esperamos poder resolverlo de manera satisfactoria para ambas partes."
                + "                                                                    <br><br>"
                + "                                                                    Atentamente,"
                + "                                                                </p>"
                + "                                                            </div>"
                + "                                                            <div style=\"width:100%;margin-right: 0px; display: flex;flex-wrap: wrap; \">"
                + "                                                                <div  style=\"flex: 0 0 50%; width:50%; max-width: 50%; text-align:left; margin:0px;\">"
                + "                                                                    <p>"
                + "                                                                        ivucontrolpr.com<br>"
                + "                                                                        Retail Manager PR LLC <br>"
                + "                                                                        Ave. Andalucia 601 San Juan PR 00920<br>"
                + "                                                                        787-466-2091<br>"
                + "                                                                        <a href=\"mailto:info@retailmanagerpr.com\">info@retailmanagerpr.com</a>"
                + "                                                                    </p>"
                + "                                                                </div>"
                + "                                                                <div  style=\"flex: 0 0 50%; width:50%; max-width: 50%; text-align:right; margin:0px;"
                + "                                                                display: flex;align-items: center;justify-content: flex-end;\">"
                + "                                                                   <img src=\"" + this.emailConfigData.getRMLogo() +"\"" 
                + "                                                                        alt=\"Logo 1\"  style=\" max-width: 170px;height: fit-content;\">"
                + "                                                                </div>"
                + "                                                            </div>"
                + "                                                        </div>"
                + "                                                    </tr>"
                + "                                                </tbody>"
                + "                                            </table>"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>"
                + "                                            </p>"
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%; background: #666c74\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td width=\"100%\" valign=\"top\""
                + "                                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">"
                + "                                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + "                                            <div align=\"center\">"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\""
                + "                                                    style=\"width: 100.0%\">"
                + "                                                    <tbody>"
                + "                                                        <tr>"
                + "                                                            <td style=\"padding: 5.25pt 0in 0in 0in\">"
                + "                                                                <p class=\"v1MsoNormal\"><span"
                + "                                                                        style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por"
                + "                                                                        favor no responder a este email. Los correos"
                + "                                                                        electrónicos enviados a esta dirección no serán"
                + "                                                                        respondidos. <br /><br />Copyright ©"
                + "                                                                        IvuControlPR Todos los derechos"
                + "                                                                        reservados.</span><u></u><u></u></p>"
                + "                                                            </td>"
                + "                                                        </tr>"
                + "                                                    </tbody>"
                + "                                                </table>"
                + "                                            </div>"
                + "                                            <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\">"
                + "                                                <u></u><u></u>"
                + "                                            </p>"
                + "                                        </td>"
                + "                                    </tr>"
                + "                                </tbody>"
                + "                            </table>"
                + "                        </div>"
                + "                    </td>"
                + "                </tr>"
                + "            </tbody>"
                + "        </table>"
                + "    </div>"
                + "</body>"
                + "</html>";
        return body;
    }

    /**
     * Creates a new email body for the registry.
     *
     * @param  emailData   the email body data used to populate the email body
     * @return             the newly created email body
     */
    private String createBodyNewRegistry(EmailBodyData emailData){
        String msg="<!DOCTYPE html>" +
"<html>" +
"" +
"<head> </head>" +
"" +
"<body style=\"font-family: Arial, sans-serif;margin: 0;padding: 0;background-color: #f4f4f4;\">" +
"   <div>" +
"      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">" +
"         <tbody>" +
"            <tr>" +
"               <td style=\"padding: 0in 0in 0in 0in\">" +
"                  <div align=\"center\">" +
"                     <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">" +
"                        <tbody>" +
"                           <tr>" +
"                              <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">" +
"                                 <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span> </p>" +
"                                 <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"" +
"                                    style=\"width: 100.0%; background: gainsboro\">" +
"                                    <tbody>" +
"                                       <tr>" +
"                                          <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">" +
"                                             <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">" +
"                                                   <u></u><u></u></span></p>" +
"                                          </td>" +
"                                       </tr>" +
"                                    </tbody>" +
"                                 </table>" +
"                                 <p class=\"v1MsoNormal\"><u></u> <u></u></p>" +
"                                 <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"" +
"                                    style=\"width: 100.0%; background: white\">" +
"                                    <tbody>" +
"                                       <tr>" +
"                                          <div class=\"container\"" +
"                                             style=\"max-width: 600px;                                                        margin: 0 auto;                                                        padding: 40px;                                                        background-color: white;                                                        border: 3px solid black;                                                        /* Añade el borde negro */\">" +
"                                             <div class=\"header\"" +
"                                                style=\" display: flex;                                                        justify-content: space-between;                                                        align-items: center;\">" +
"                                                <div class=\"info-column\"" +
"                                                   style=\" flex: 2;                                                        text-align: right;\">" +
"                                                </div>" +
"                                                <div class=\"\"" +
"                                                   style=\"flex: 2;                                                        text-align: center; text-align: right;                                                        margin-left: 10%;\">" +
"                                                   <img src='" + this.emailConfigData.getRMPAYLogo() +"'" +
"                                                      alt='Logo' class='logo'" +
"                                                      style=\"max-width: 300px;                                                                        height: auto;                                                                        margin: 0 auto; \">" +
"                                                </div>" +
"                                             </div><br> <u>NUEVO CLIENTE REGISTRADO EN RMPAY </u>" +
"                                             <div" +
"                                                style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">" +
"                                                <div" +
"                                                   style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\">" +
"                                                   <p>Fecha de solicitud: " + LocalDate.now().toString() +"</p>" +
"                                                </div>" +
"                                             </div>" +
"                                             <u>Información de Cliente:</u> <br><br>" +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%; text-align:right; margin:0px;\">" +
"                                                   <strong>NOMBRE: </strong>" +
"                                                </p>" +
"                                                <p" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" +
"                                                   " + emailData.getName() +",</p>" +
"                                             </div>" +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%; text-align:right;margin:0px\">" +
"                                                   <strong>NEGOCIO: </strong>" +
"                                                </p>" +
"                                                <p class=\"col2\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" +
"                                                   " + emailData.getBusinessName()+" ,</p>" +
"                                             </div>" +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">" +
"                                                   <strong># MERCHANT: </strong>" +
"                                                </p>" +
"                                                <p class=\"col2\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">" +
"                                                   " + emailData.getMerchantId() +",</p>" +
"                                             </div> <br>" +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">" +
"                                                   <strong># TELEFONO: </strong>" +
"                                                </p>" +
"                                                <p class=\"col2\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">" +
"                                                   " + emailData.getPhone() +",</p>" +
"                                             </div> <br>" +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">" +
"                                                   <strong># NOMBRE DEL NEGOCIO: </strong>" +
"                                                </p>" +
"                                                <p class=\"col2\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">" +
"                                                   " + emailData.getBusinessName() +",</p>" +
"                                             </div> <br>" +
"                                            " +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">" +
"                                                   <strong> SERVICIOS SOLICITADOS: </strong>" +
"                                                </p>" +
"                                                <p class=\"col2\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">" +
"                                                   " + emailData.getServiceDescription() +",</p>" +
"                                             </div> <br>" +
"                                             <div" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">" +
"                                                <p class=\"col1\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">" +
"                                                   <strong>CANTIDAD DE TERMINALES: </strong>" +
"                                                </p>" +
"                                                <p class=\"col2\"" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">" +
"                                                   " + emailData.getAdditionalTerminals()+" ,</p>" +
"                                             </div> <br>" +
"                                             <u>Información de Pago:</u> <br><br>" +
"                                             -paymethod-" +
"                                             <p>Cordilmente, <br> Equipo de Soporte de RMPAY</p>" +
"                                             <div" +
"                                                style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">" +
"                                                <div" +
"                                                   style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:center; margin:0px;\">" +
"" +
"" +
"                                                   <div" +
"                                                      style=\"display: flex;" +
"                                                                                                              " +
"                                                                                                               align-items: center;" +
"                                                                                                               margin-top: 0px;\">" +
"                                                      <div class=\"logo-column\"" +
"                                                         style=\"flex: 1;" +
"                                                                                                                      text-align: center;" +
"                                                                                                                       margin-left: 40%;\">" +
"                                                         <img src=\"" + this.emailConfigData.getRMLogo() +"\"" +
"                                                            alt=\"Logo 1\" class=\"logo\" style=\" max-width: 170px;" +
"                                                            height: auto; margin: 0 auto;\">" +
"                                                      </div>" +
"                                                      <div class=\"info-column\"" +
"                                                         style=\" flex: 1;" +
"                                                                                                                      text-align: center; margin-left: 0;\">" +
"                                                         <strong>" +
"                                                            <p>787-466-2091 <br> 601 Ave. Andalucia<br> San" +
"                                                               Juan PR 00920</p>" +
"                                                         </strong>" +
"                                                      </div>" +
"                                                   </div>" +
"                                                   <div class=\"info-column\"" +
"                                                      style=\" flex: 1;" +
"                                                                                                                   text-align: center; margin-left: 0;\">" +
"                                                      <p>Copyright © IvuControlPR Todos los derechos reservados.</p>" +
"                                                   </div>" +
"                                                </div>" +
"                                       </tr>" +
"                                    </tbody>" +
"                                 </table>" +
"                                 <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span> </p>" +
"                                 <div align=\"center\">" +
"                                    <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"" +
"                                       style=\"width: 100.0%; background: #666c74\">" +
"                                       <tbody>" +
"                                          <tr>" +
"                                             <td width=\"100%\" valign=\"top\"" +
"                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">" +
"                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>" +
"                                             </td>" +
"                                          </tr>" +
"                                       </tbody>" +
"                                    </table>" +
"                                 </div>" +
"                                 <div align=\"center\">" +
"                                    <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"" +
"                                       style=\"width: 100.0%\">" +
"                                       <tbody>" +
"                                          <tr>" +
"                                             <td style=\"padding: 5.25pt 0in 0in 0in\">" +
"                                                <p class=\"v1MsoNormal\"><span" +
"                                                      style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por" +
"                                                      favor no responder a este email. Los correos electrónicos enviados" +
"                                                      a esta dirección no serán respondidos. <br /><br />Copyright ©" +
"                                                      IvuControlPR Todos los derechos reservados.</span><u></u><u></u>" +
"                                                </p>" +
"                                             </td>" +
"                                          </tr>" +
"                                       </tbody>" +
"                                    </table>" +
"                                 </div>" +
"                                 <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\"> <u></u><u></u> </p>" +
"                              </td>" +
"                           </tr>" +
"                        </tbody>" +
"                     </table>" +
"                  </div>" +
"               </td>" +
"            </tr>" +
"         </tbody>" +
"      </table>" +
"   </div>" +
"</body>" +
"" +
"</html>";
        return msg;
    }
    public EmailConfigData loadAndValid() throws ConfigurationNotFoundException {
        EmailConfigData valueObject = new EmailConfigData();
        Object[] obj1=this.sys_general_configRepository.getEmailConfig();
        Object[] obj=(Object[]) obj1[0];
        valueObject.setAppKey(obj[0].toString());
        valueObject.setEmailFrom( obj[1].toString());
        valueObject.setEmailTo( obj[2].toString());
        valueObject.setEmailCCO( obj[3].toString());
        valueObject.setRMPAYLogo( obj[4].toString());
        valueObject.setRMLogo( obj[5].toString());
        HashMap<String, String> map = new HashMap<>();
        boolean bandera=false;
        if (valueObject!=null) {
            if(valueObject.getAppKey() == null){
                map.put("Key", "config.email.AppKey not found.");
                bandera=true;
            }
            if(valueObject.getEmailFrom() == null ){
                map.put("AppType", "config.email.emailFrom not found.");
                bandera=true;
            }
            if(valueObject.getEmailCCO() == null ){
                map.put("cid", "config.email.emailCCO not found.");
                bandera=true;
            }
            if(valueObject.getEmailTo()== null ){
                map.put("mid", "config.email.emailTo not found.");
                bandera=true;
            }
            
            if(bandera){
                Gson gson = new Gson();
                String json = gson.toJson(map);
                throw new ConfigurationNotFoundException("[Debug] Email Service Configuration not found."+json);
            }
        }
        return valueObject;
    }
    /**
     * Returns the file extension of the given file name.
     *
     * @param  nombreArchivo   the name of the file
     * @return                 the file extension
     */
    private String obtenerExtension(String nombreArchivo) {
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1);
    }
    private String createBodyPaymentNotification( String userName, String businessName, List<String> listServices, String diasRestantes) {
        String msg = "<body style=\"font-family: Arial, sans-serif;\n"
                + "margin: 0;\n"
                + "padding: 0;\n"
                + "background-color: #f4f4f4;\">\n"
                + "    <div>\n"
                + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">\n"
                + "            <tbody>\n"
                + "                <tr>\n"
                + "                    <td style=\"padding: 0in 0in 0in 0in\">\n"
                + "                        <div align=\"center\">\n"
                + "                            <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">\n"
                + "                                <tbody>\n"
                + "                                    <tr>\n"
                + "                                        <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">\n"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>\n"
                + "                                            </p>\n"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                style=\"width: 100.0%; background: gainsboro\">\n"
                + "                                                <tbody>\n"
                + "                                                    <tr>\n"
                + "                                                        <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">\n"
                + "                                                            <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">\n"
                + "                                                                    <u></u><u></u></span></p>\n"
                + "                                                        </td>\n"
                + "                                                    </tr>\n"
                + "                                                </tbody>\n"
                + "                                            </table>\n"
                + "                                            <p class=\"v1MsoNormal\"><u></u> <u></u></p>\n"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                style=\"width: 100.0%; background: white\">\n"
                + "                                                <tbody>\n"
                + "                                                    <tr>\n"
                + "                                                        <div class=\"container\" style=\"max-width: 600px;\n"
                + "                                                        margin: 0 auto;\n"
                + "                                                        padding: 20px;\n"
                + "                                                        background-color: white;\n"
                + "                                                        border: 3px solid black;\n"
                + "                                                        /* Añade el borde negro */\">\n"
                + "                                                            <div class=\"header\" style=\" display: flex;\n"
                + "                                                            justify-content: space-between;\n"
                + "                                                            align-items: center;\">\n"
                + "                                                                <div class=\"logo-column\" style=\"flex: 1;\n"
                + "                                                                text-align: center; text-align: right;\n"
                + "                                                                margin-left: 10%;\">\n"
                + "                                                                    <img src='"+emailConfigData.getRMPAYLogo()+"'\n"
                + "                                                                        alt='Logo' class='logo' style=\"max-width: 300px;\n"
                + "                                                                        height: auto;\n"
                + "                                                                        margin: 0 auto; \">\n"
                + "                                                                </div>\n"
                + "                                                            </div>\n"
                + "                                                            <div class=\"content\" style=\" margin-top: 20px;\n"
                + "                                                            text-align: center;\">\n" 
                + " <h2>Asunto: Notificación de Vencimiento de Servicio - RMpay</h2>"
        + " <p>Estimado/a "+userName+",</p> "
        + " <p>Esperamos que este mensaje le encuentre bien. Nos dirigimos a usted para informarle que los siguientes servicios asociados a su(s) terminal(es) de RMpay están próximos a vencer en los próximos "+diasRestantes+" días:</p>" 
        +" <ol style=\"list-style: none; padding: 0; counter-reset: li; text-align: center;\">";
        int i=1;
        for (String s : listServices) {
           msg += " <li style=\"counter-increment: li; margin-bottom: 10px;\">";
           msg +="     <span style=\"content: counter(li) '. '; display: inline-block; width: 1.5em; margin-left: -1.5em; text-align: right;\"></span>"+i+". "+s;
           msg +=" </li>" ;
           i++;
        }
    msg+=" </ol>"
        + " <p>Para garantizar la continuidad de su servicio sin interrupciones, le ofrecemos dos opciones sencillas para efectuar la renovación:</p> "
                + "                                                            </div>\n"
                +"                                                              <div class=\"content\" style=\" margin-top: 20px;\n"
                + "                                                            text-align: left;\">\n" 
                + "                                                     <ol>"
                + "                                                    <li>"
                + "                                                       <strong>A través de nuestra página web:</strong>"
                + "                                                        <ul>"
                + "                                                            <li>Visite <a href=\"https://rmpay.retailmanagerpr.com\">rmpay.retailmanagerpr.com</a>.</li>"
                + "                                                            <li>Inicie sesión en su cuenta.</li>"
                + "                                                            <li>Realice la renovación de su servicio desde su perfil de usuario.</li>"
                + "                                                        </ul>"
                + "                                                    </li>"
                + "                                                    <li>"
                + "                                                        <strong>Desde la aplicación RMpay:</strong>"
                + "                                                        <ul>"
                + "                                                            <li>Abra la aplicación RMpay en su dispositivo.</li>"
                + "                                                            <li>Vaya al menú de configuración.</li>"
                + "                                                            <li>Seleccione la opción de suscripción y siga las instrucciones para completar el pago.</li>"
                + "                                                        </ul>"
                + "                                                    </li>"
                + "                                                </ol>"
                + "                                                            </div>\n"
                + " <div class=\"content\" style=\" margin-top: 20px;\n"
                + "                                                            text-align: center;\">\n"
        + " <p>Le recomendamos realizar la renovación a la mayor brevedad posible para evitar cualquier interrupción en el servicio. Si tiene alguna pregunta o necesita asistencia, no dude en ponerse en contacto con nuestro equipo de soporte al cliente.\r</p>\r"
        + " <p>Agradecemos su preferencia y confianza en RMpay.\r</p>" 
        +"<p>Atentamente,</p>"
        + " <p>Retail Manager PR LLC</p> "
        + " <p>601 Ave. Andalucia San Juan PR 00920</p> "
        + " <p><a href=\"tel:7872462091\">787-246-2091</a></p> "
        + " <p><a href=\"rmpay.retailmanagerpr.com\">rmpay.retailmanagerpr.com</a></p> "
        + " <p><a href=\"mailto:info@retailmanagerpr.com\">info@retailmanagerpr.com</a></p> "
                + "                                                            </div>\n"
                + "                                                            </div>\n"
                + "                                                        </div>\n"
                + "                                                    </tr>\n"
                + "                                                </tbody>\n"
                + "                                            </table>\n"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>\n"
                + "                                            </p>\n"
                + "                                            <div align=\"center\">\n"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                    style=\"width: 100.0%; background: #666c74\">\n"
                + "                                                    <tbody>\n"
                + "                                                        <tr>\n"
                + "                                                            <td width=\"100%\" valign=\"top\"\n"
                + "                                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">\n"
                + "                                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>\n"
                + "                                                            </td>\n"
                + "                                                        </tr>\n"
                + "                                                    </tbody>\n"
                + "                                                </table>\n"
                + "                                            </div>\n"
                + "                                            \n"
                + "                                            <div align=\"center\">\n"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                    style=\"width: 100.0%\">\n"
                + "                                                    <tbody>\n"
                + "                                                        <tr>\n"
                + "                                                            <td style=\"padding: 5.25pt 0in 0in 0in\">\n"
                + "                                                                <p class=\"v1MsoNormal\"><span\n"
                + "                                                                        style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por\n"
                + "                                                                        favor no responder a este email. Los correos\n"
                + "                                                                        electrónicos enviados a esta dirección no serán\n"
                + "                                                                        respondidos. <br /><br />Copyright ©\n"
                + "                                                                        IvuControlPR Todos los derechos\n"
                + "                                                                        reservados.</span><u></u><u></u></p>\n"
                + "                                                            </td>\n"
                + "                                                        </tr>\n"
                + "                                                    </tbody>\n"
                + "                                                </table>\n"
                + "                                            </div>\n"
                + "                                            <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\">\n"
                + "                                                <u></u><u></u>\n"
                + "                                            </p>\n"
                + "                                        </td>\n"
                + "                                    </tr>\n"
                + "                                </tbody>\n"
                + "                            </table>\n"
                + "                        </div>\n"
                + "                    </td>\n"
                + "                </tr>\n"
                + "            </tbody>\n"
                + "        </table>\n"
                + "    </div>\n"
                + "    \n"
                + "</body>";
                return msg;
    }

@Override
    public void sendHtmlEmailWithAttachmentAndCCO(String fromEmail, List<String> toList, String subject, String htmlBody, List<String> cc,
            byte[] attachmentData, String attachmentFileName) {
        try {
            String sender = (fromEmail == null || fromEmail.isBlank()) ? emailConfigData.getEmailFrom() : fromEmail.trim();
            Email from = new Email(sender);

            Personalization personalization = new Personalization();
            for (String recipient : toList) {
                personalization.addTo(new Email(recipient));
            }

            if (cc != null) {
                for (String ccRecipient : cc) {
                    personalization.addCc(new Email(ccRecipient));
                }
            }

            Mail mail = new Mail();
            mail.setFrom(from);
            mail.setSubject(subject);
            mail.addContent(new Content("text/html", htmlBody));
            mail.addPersonalization(personalization);

            if (attachmentData != null && attachmentFileName != null && !attachmentFileName.isEmpty()) {
                InputStream pdfInputStream = new ByteArrayInputStream(attachmentData);
                Attachments attachments = new Attachments.Builder(attachmentFileName, pdfInputStream)
                        .withType("application/" + obtenerExtension(attachmentFileName))
                        .build();
                mail.addAttachments(attachments);
                System.out.println("Adjuntando archivo: " + attachmentFileName);
            }

            SendGrid sg = new SendGrid(SENDGRID_API_KEY);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            System.out.println("Enviando correo...");
            Response response = sg.api(request);
            if (response.getStatusCode() != 202) {
                System.out.println("Error al enviar el correo: " + response.getBody());
            }
            if (response.getStatusCode() == 202) {
                System.out.println("Correo enviado exitosamente");
            }
        } catch (IOException ex) {
            System.out.println("Error al enviar el correo: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Error al enviar el correo: " + ex.getMessage());
        }
    }

// ── NEW: SMTP email sender with HTML, CC and attachment ──────────────────
// Purpose : Sends an HTML email using SMTP with optional CC and attachment
// Depends on : obtenerExtension(attachmentFileName)
// Does NOT modify : business logic, recipient behavior, attachment behavior
@Override
public void sendHtmlEmailWithAttachmentAndCCO(
        String smtpHost,
        int smtpPort,
        String smtpUsername,
        String smtpPassword,
        boolean smtpAuth,
        boolean startTls,
        String fromEmail,
        List<String> toList,
        String subject,
        String htmlBody,
        List<String> cc,
        byte[] attachmentData,
        String attachmentFileName
) {
    try {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtpHost);
        mailSender.setPort(smtpPort);
        mailSender.setUsername(smtpUsername);
        mailSender.setPassword(smtpPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(smtpAuth));
        props.put("mail.smtp.starttls.enable", String.valueOf(startTls));
        props.put("mail.debug", "false");

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                attachmentData != null && attachmentFileName != null && !attachmentFileName.isBlank(),
                StandardCharsets.UTF_8.name()
        );

        String sender = (fromEmail == null || fromEmail.isBlank())
                ? smtpUsername
                : fromEmail.trim();

        helper.setFrom(sender);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        if (toList != null && !toList.isEmpty()) {
            helper.setTo(toList.toArray(new String[0]));
        } else {
            System.out.println("No se enviará correo porque la lista de destinatarios está vacía.");
            return;
        }

        if (cc != null && !cc.isEmpty()) {
            helper.setCc(cc.toArray(new String[0]));
        }

        if (attachmentData != null && attachmentFileName != null && !attachmentFileName.isBlank()) {
            ByteArrayResource attachmentResource = new ByteArrayResource(attachmentData);

            helper.addAttachment(
                    attachmentFileName,
                    attachmentResource,
                    "application/" + obtenerExtension(attachmentFileName)
            );

            System.out.println("Adjuntando archivo: " + attachmentFileName);
        }

        System.out.println("Enviando correo por SMTP...");
        mailSender.send(message);
        System.out.println("Correo enviado exitosamente SMTP");

    } catch (Exception ex) {
        System.out.println("Error al enviar el correo por SMTP: " + ex.getMessage());
    }
}
    @Override
    public void testEmailService(EmailBodyData emailData) {
       List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = new ArrayList<String>();
        cc.add(emailConfigData.getEmailCCO());
        cc.add(emailConfigData.getEmailTo());
        String subject = "RECIBO #" + emailData.getInvoiceNumber() + " DE PAGO VIA TOKEN DE PAGO";
        if(emailData.getInvoiceNumber()!=0){
            subject = "RECIBO #"+emailData.getInvoiceNumber()+" DE PAGO CON TOKEN DE PAGO";
        }
        String htmlBody = createBodyPaymentToken(emailData);
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    }




    




    
}
