package com.retailmanager.rmpaydashboard.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.retailmanager.rmpaydashboard.services.DTO.SaleDTO;

import jakarta.persistence.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Sale {

    @Id
    
    @Column(name = "saleID")
    private String saleID;

    @Column(name = "saleCreationDate", nullable = false)
    private Instant saleCreationDate;

    @Column(name = "saleEndDate")
    private Instant saleEndDate;

    @Column(name = "saleItems", nullable = true, columnDefinition = "varchar(MAX)")
    private String items;

    @Column(name = "saleSubtotal", nullable = false)
    private double saleSubtotal;

    @Column(name = "saleStateTaxAmount", nullable = false)
    private double saleStateTaxAmount;

    @Column(name = "saleCityTaxAmount", nullable = false)
    private double saleCityTaxAmount;

    @Column(name = "saleReduceTax", nullable = false)
    private double saleReduceTax;

    @Column(name = "saleTotalAmount", nullable = false)
    private double saleTotalAmount;

    @Column(name = "saleTransactionType", nullable = false)
    private String saleTransactionType;

    @Column(name = "saleMachineID")
    private String saleMachineID;

    @Column(name = "saleIvuNumber")
    private String saleIvuNumber;

    @Column(name = "saleStatus")
    private String saleStatus;

    @Column(name = "saleChange")
    private Double saleChange;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    //@Column(name = "merchantId", nullable = false)
    //private String merchantId;

    @Column(name = "saleToRefund",columnDefinition = "varchar(MAX)")
    private String saleToRefund;

    @Column(nullable = false, columnDefinition = "float default 0")
    private Double tipAmount;
    @Column(nullable = false, columnDefinition = "float default 0")
    private Double tipPercentage;

    private Integer remoto;

    @ManyToOne(cascade=CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "businessId")
    private Business business;

    @ManyToOne(cascade=CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "terminalId")
    private Terminal terminal;
    
    @OneToMany(mappedBy = "sale",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public List<ItemForSale> itemsList = new ArrayList<>();

    @OneToMany(mappedBy = "sale",fetch = FetchType.LAZY)
    private List<Transactions> transactions = new ArrayList<>();

    public SaleDTO toDTO() {
        SaleDTO saleDTO = new SaleDTO();
        saleDTO.setSaleID(this.getSaleID());
        saleDTO.setSaleCreationDate(this.getSaleCreationDate());
        saleDTO.setSaleEndDate(this.getSaleEndDate());
        saleDTO.setItems(this.getItems());
        saleDTO.setSaleSubtotal(this.getSaleSubtotal());
        saleDTO.setSaleStateTaxAmount(this.getSaleStateTaxAmount());
        saleDTO.setSaleCityTaxAmount(this.getSaleCityTaxAmount());
        saleDTO.setSaleReduceTax(this.getSaleReduceTax());
        saleDTO.setSaleTotalAmount(this.getSaleTotalAmount());
        saleDTO.setSaleTransactionType(this.getSaleTransactionType());
        saleDTO.setSaleMachineID(this.getSaleMachineID());
        saleDTO.setSaleIvuNumber(this.getSaleIvuNumber());
        saleDTO.setSaleStatus(this.getSaleStatus());
        saleDTO.setSaleChange(this.getSaleChange());
        saleDTO.setUserId(this.getUserId());
        saleDTO.setSaleToRefund(this.getSaleToRefund());
        saleDTO.setTipAmount(this.getTipAmount());
        saleDTO.setTipPercentage(this.getTipPercentage());
        saleDTO.setBusinessId(this.getBusiness().getBusinessId());
        saleDTO.setTerminalId(this.getTerminal().getTerminalId());
        return saleDTO;
    }
}
