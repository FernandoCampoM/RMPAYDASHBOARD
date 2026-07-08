package com.retailmanager.rmpaydashboard.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Terminal {

    @Id
    private String terminalId;
    @Column(nullable = true)
    private String serial;
    @Column(nullable = true)
    private String name;

    @Column(nullable = false)
    private boolean enable;

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "businessId")
    private Business business;
    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "serviceId")
    private Service service;
    @Column(nullable = true)
    private Instant expirationDate;

    private Instant lastTransmision;

    private boolean automaticPayments;
    private boolean isPayment;
    private boolean isPrincipal;

    @Column(nullable = true)
    private Instant registerDate;
    @Column(nullable = true)
    private Instant lastPayment;
    @Column(nullable = true)
    private Double lastPaymentValue;

    // Otros campos y métodos según se necesite
}
