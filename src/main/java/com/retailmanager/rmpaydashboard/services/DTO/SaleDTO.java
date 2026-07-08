package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.retailmanager.rmpaydashboard.models.Sale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SaleDTO {

    private String saleID;

    private Instant saleCreationDate;
    private Instant saleEndDate;

    private String items;

    @NotNull(message = "{saleSubtotal.notNull}")
    @PositiveOrZero(message = "{saleSubtotal.positiveOrZero}")
    private Double saleSubtotal;

    @NotNull(message = "{saleStateTaxAmount.notNull}")
    @PositiveOrZero(message = "{saleStateTaxAmount.positiveOrZero}")
    private Double saleStateTaxAmount;

    @NotNull(message = "{saleCityTaxAmount.notNull}")
    @PositiveOrZero(message = "{saleCityTaxAmount.positiveOrZero}")
    private Double saleCityTaxAmount;

    @NotNull(message = "{saleReduceTax.notNull}")
    @PositiveOrZero(message = "{saleReduceTax.positiveOrZero}")
    private Double saleReduceTax;

    @NotNull(message = "{saleTotalAmount.notNull}")
    @PositiveOrZero(message = "{saleTotalAmount.positiveOrZero}")
    private Double saleTotalAmount;

    @NotBlank(message = "{saleTransactionType.notBlank}")
    private String saleTransactionType;

    private String saleMachineID;

    private String saleIvuNumber;

    private String saleStatus;

    private Double saleChange;

    @NotNull(message = "{userId.notNull}")
    @Positive(message = "{userId.positive}")
    private Integer userId;

    //@NotBlank(message = "{merchantId.notBlank}")
    //private String merchantId;
    @PositiveOrZero(message = "{sale.businessId.notBlank}")
    private Long businessId;
    private String saleToRefund;

    private String terminalId;
    @NotNull(message = "{sale.tipAmount.notNull}")
    @PositiveOrZero(message = "{sale.tipAmount.positiveOrZero}")
    private Double tipAmount;
private Double tipPercentage;

 @NotNull(message = "{sale.remoto.notnull}")
 private Integer remoto;

    public static SaleDTO fromEntity(Sale sale) {
        SaleDTO saleDTO = new SaleDTO();
        saleDTO.setSaleID(sale.getSaleID());
        saleDTO.setSaleCreationDate(sale.getSaleCreationDate());
        saleDTO.setSaleEndDate(sale.getSaleEndDate());
        saleDTO.setItems(sale.getItems());
        saleDTO.setSaleSubtotal(sale.getSaleSubtotal());
        saleDTO.setSaleStateTaxAmount(sale.getSaleStateTaxAmount());
        saleDTO.setSaleCityTaxAmount(sale.getSaleCityTaxAmount());
        saleDTO.setSaleReduceTax(sale.getSaleReduceTax());
        saleDTO.setSaleTotalAmount(sale.getSaleTotalAmount());
        saleDTO.setSaleTransactionType(sale.getSaleTransactionType());
        saleDTO.setSaleMachineID(sale.getSaleMachineID());
        saleDTO.setSaleIvuNumber(sale.getSaleIvuNumber());
        saleDTO.setSaleStatus(sale.getSaleStatus());
        saleDTO.setSaleChange(sale.getSaleChange());
        saleDTO.setUserId(sale.getUserId());
        saleDTO.setBusinessId(sale.getBusiness().getBusinessId());
        saleDTO.setSaleToRefund(sale.getSaleToRefund());
        saleDTO.setTerminalId(sale.getTerminal().getTerminalId());
        saleDTO.setTipAmount(sale.getTipAmount());
        saleDTO.setTipPercentage(sale.getTipPercentage());
        saleDTO.setRemoto(sale.getRemoto());
        return saleDTO;
    }
}
