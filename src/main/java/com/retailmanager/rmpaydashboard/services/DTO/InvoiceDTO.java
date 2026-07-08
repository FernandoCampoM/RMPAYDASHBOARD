package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InvoiceDTO {
    private Long invoiceNumber;
    private LocalDate date;
    private LocalTime time;
    private String paymentMethod;
    private Integer terminals;
    private Double totalAmount;
    private Double subTotal;
    private Double stateTax;
    private Long businessId;
    
    private String referenceNumber;
    private boolean rejected = false; //Para indicar si un pago fue rechazado 
    private boolean paid=false; //Para indicar si un pago rechazado fue cobrado nuevamente
    private boolean inProcess=false; //Para indicar si el pago se encuentra en proceso
    private String terminalIds;
    private Long serviceId;
    private String paymentDescription;
    private String ATHMPaymentDetails;
}
