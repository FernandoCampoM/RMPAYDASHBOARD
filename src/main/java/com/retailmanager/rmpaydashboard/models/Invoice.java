package com.retailmanager.rmpaydashboard.models;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @NoArgsConstructor @AllArgsConstructor @Setter
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceNumber;
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    private LocalTime time;
    @Column(nullable = false)
    private String paymentMethod;
    @Column(nullable = true)
    private Integer terminals;
    @Column(nullable = false)
    private Double totalAmount;
    /*Valor antes de impuesto */

    @Column(nullable = true)
    private Double subTotal;
    @Column(nullable = true)
    private Double stateTax;
    @Column(nullable = false)
    private Long businessId;
    
    @Column(nullable = true)
    private String referenceNumber;
    private boolean rejected = false; //Para indicar si un pago fue rechazado 
    private boolean paid=false; //Para indicar si un pago rechazado fue cobrado nuevamente
    private boolean inProcess=false; //Para indicar si el pago se encuentra en proceso
    private String terminalIds;
    @Column(columnDefinition = "VARCHAR(MAX)", nullable = true)
    private String paymentDescription;
    private Long serviceId;
    @Column(columnDefinition = "VARCHAR(MAX)", nullable = true)
    private String ATHMPaymentDetails;

}
