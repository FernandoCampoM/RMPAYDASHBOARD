package com.retailmanager.rmpaydashboard.backgroundRoutines;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.services.DTO.InvoiceDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalsDoPaymentDTO;
import com.retailmanager.rmpaydashboard.services.DTO.doPaymentDTO;
import com.retailmanager.rmpaydashboard.services.services.EmailService.IEmailService;
import com.retailmanager.rmpaydashboard.services.services.InvoiceServices.IInvoiceServices;

import jakarta.transaction.Transactional;

@Component
public class BackgroundRoutinesService {
    @Autowired
    private TerminalRepository terminalRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private IInvoiceServices invoiceService;

    @Autowired
    IEmailService emailService;
    @Modifying
    public void deactivateExpiredTerminals(){
        List<Terminal> terminals = terminalRepository.findByExpirationDateBefore(Instant.now());
        for (Terminal terminal : terminals) {
            terminalRepository.deactivateExpiredTerminals(terminal.getTerminalId());
        }
    }
    @Transactional
    public void priorNotificaionEmail(){
         HashMap<Long, Object> users = new HashMap<Long, Object>();
         Instant date = Instant.now();
         date=date.minus(Duration.ofDays(10));
         List<Terminal> terminals = terminalRepository.getBusinessForPriorNotification(date);
         for (Terminal terminal : terminals) {
            if(users.containsKey(terminal.getBusiness().getUser().getUserID())){
                HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(terminal.getBusiness().getUser().getUserID());
                List<String> services=(List<String>) bsuiness.get("services");
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }else{
                HashMap<String, Object> bsuiness = new HashMap<String, Object>();
                List<String> services = new ArrayList<String>();
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                bsuiness.put("businessName", terminal.getBusiness().getUser().getName());
                bsuiness.put("userEmail", terminal.getBusiness().getUser().getEmail());
                bsuiness.put("userName", terminal.getBusiness().getUser().getName());
                bsuiness.put("businessId", terminal.getBusiness().getBusinessId());
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }
            
            terminal.getBusiness().setPriorNotification(date);
        }
        for (Long key : users.keySet()) {
            HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(key);
            emailService.priorNotificationEmail(bsuiness.get("userEmail").toString(), bsuiness.get("userName").toString(),bsuiness.get("businessName").toString(), (List<String>) bsuiness.get("services"));
        }
         terminalRepository.saveAll(terminals);
    }
    @Transactional
    public void lastDayNotificaionEmail(){
        HashMap<Long, Object> users = new HashMap<Long, Object>();
         Instant date = Instant.now();
         date=date.minus(Duration.ofDays(5));
         List<Terminal> terminals = terminalRepository.getBusinessForLastDayNotification(date);
         for (Terminal terminal : terminals) {
            if(users.containsKey(terminal.getBusiness().getUser().getUserID())){
                HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(terminal.getBusiness().getUser().getUserID());
                List<String> services=(List<String>) bsuiness.get("services");
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }else{
                HashMap<String, Object> bsuiness = new HashMap<String, Object>();
                List<String> services = new ArrayList<String>();
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                bsuiness.put("businessName", terminal.getBusiness().getUser().getName());
                bsuiness.put("userEmail", terminal.getBusiness().getUser().getEmail());
                bsuiness.put("userName", terminal.getBusiness().getUser().getName());
                bsuiness.put("businessId", terminal.getBusiness().getBusinessId());
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }
            
            terminal.getBusiness().setLastDayNotification(date);
        }
        for (Long key : users.keySet()) {
            HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(key);
            emailService.lastDayNotificationEmail(bsuiness.get("userEmail").toString(), bsuiness.get("userName").toString(),bsuiness.get("businessName").toString(), (List<String>) bsuiness.get("services"));
        }
         terminalRepository.saveAll(terminals);
   }
   @Transactional
   public void afterNotificaionEmail(){
    HashMap<Long, Object> users = new HashMap<Long, Object>();
         Instant date = Instant.now();
         List<Terminal> terminals = terminalRepository.getBusinessForAfterNotification(date);
         for (Terminal terminal : terminals) {
            if(users.containsKey(terminal.getBusiness().getUser().getUserID())){
                HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(terminal.getBusiness().getUser().getUserID());
                List<String> services=(List<String>) bsuiness.get("services");
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }else{
                HashMap<String, Object> bsuiness = new HashMap<String, Object>();
                List<String> services = new ArrayList<String>();
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                bsuiness.put("businessName", terminal.getBusiness().getUser().getName());
                bsuiness.put("userEmail", terminal.getBusiness().getUser().getEmail());
                bsuiness.put("userName", terminal.getBusiness().getUser().getName());
                bsuiness.put("businessId", terminal.getBusiness().getBusinessId());
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }
            
            terminal.getBusiness().setAfterNotification(date);
        }
        for (Long key : users.keySet()) {
            HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(key);
            emailService.beforeNotificationEmail(bsuiness.get("userEmail").toString(), bsuiness.get("userName").toString(),bsuiness.get("businessName").toString(), (List<String>) bsuiness.get("services"));
        }
         terminalRepository.saveAll(terminals);
    }

    public void automaticPayments() {
        System.out.println("INICIO DE PAGO AUTOMATICO");
        Instant date = Instant.now();
        Iterable<Business> allBusinesses = businessRepository.findAll();
        for (Business business : allBusinesses) {
            try {

                if(business.getPaymentData()!=null && business.getPaymentData().isUsingAutomaticPayment()){
                    System.out.println("EJECUTANO PAGO PARA EL NEGOCIO: "+business.getBusinessId());
                    List<Terminal> terminalsforPayment = terminalRepository.findTerminalsForPayment(date, business.getBusinessId());
                    List<Terminal> terminalsThatAreNotPaid=terminalRepository.terminalsThatAreNotPaid(date, business.getBusinessId());
                    if(terminalsforPayment.size()>0){
                        doPaymentDTO paymentInfo = new doPaymentDTO();
                        paymentInfo.setTerminalsNumber(terminalsforPayment.size()+terminalsThatAreNotPaid.size());
                        paymentInfo.setBusinessId(business.getBusinessId());
                        paymentInfo.setAutomaticPayments(true);
                        paymentInfo.setTerms(true);
                        paymentInfo.setPaymethod("TOKEN");
                        paymentInfo.setTerminalsDoPayment(new ArrayList<>());
                        for (Terminal terminal : terminalsforPayment) {
                            TerminalsDoPaymentDTO terminalDoPayment = new TerminalsDoPaymentDTO();
                            terminalDoPayment.setTerminalId(terminal.getTerminalId());
                            terminalDoPayment.setIdService(terminal.getService().getServiceId());
                            paymentInfo.getTerminalsDoPayment().add(terminalDoPayment);
                        }
                        for(int i=0; i<3; i++){
                            System.out.println("INTENTO DE PAGO AUTOMATICO PARA EL NEGOCIO: "+business.getBusinessId()+" -- INTENTO: "+(i+1));
                            try{
                                ResponseEntity<?> res = invoiceService.doPayment(paymentInfo);
                                InvoiceDTO invoice = (InvoiceDTO)res.getBody();
                                i=3;
                                break;
                            }catch(Exception e){
                                try{
                                    ResponseEntity<?> res = invoiceService.doPayment(paymentInfo);
                                    InvoiceDTO invoice = (InvoiceDTO)res.getBody();
                                    i=3;
                                    break;
                                }catch(Exception ex){
                                    try{
                                        ResponseEntity<?> res = invoiceService.doPayment(paymentInfo);
                                        InvoiceDTO invoice = (InvoiceDTO)res.getBody();
                                        i=3;
                                        break;
                                    }catch(Exception exx){
                                        System.out.println("INTENTO DE PAGO AUTOMATICO FALLIDO PARA EL NEGOCIO: "+business.getBusinessId()+" -- INTENTO: "+(i+1));
                                        if(i==2){
                                            System.out.println("FALLO DEFINITIVO DE PAGO AUTOMATICO PARA EL NEGOCIO: "+business.getBusinessId());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error BackgroundRoutinesService.automaticPayments: Business Id: " + business.getBusinessId() + " -- " + e.getMessage());
            }
        }
    }

}
