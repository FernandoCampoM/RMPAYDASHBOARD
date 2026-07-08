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
public class Transactions {

    @Id
    @Column(columnDefinition = "varchar(255)")
    private String id = "";

    @Column(columnDefinition = "varchar(max)")
    private String account = "";

    private Double amount;
    @Column(columnDefinition = "varchar(max)")
    private String authCode;
    @Column(columnDefinition = "varchar(max)")
    private String batchNo;

    private String cardType;

    private Double changeChash;

    private Instant date;
    @Column(columnDefinition = "varchar(max)")
    private String entryMode;
    @Column(columnDefinition = "varchar(max)")
    private String globalUId;
    private String paymentType;
    @Column(columnDefinition = "varchar(max)")
    private String refId;

    private String state;

    private Integer remoto;

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "saleId", nullable = false)
    private Sale sale;
    // Constructor, getters y setters

    private Double cashRounding;
}

