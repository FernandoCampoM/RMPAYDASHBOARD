package com.retailmanager.rmpaydashboard.services.DTO;

import com.retailmanager.rmpaydashboard.models.SaleReportProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.retailmanager.rmpaydashboard.models.SaleReport;

import jakarta.validation.constraints.DecimalMin; // Para JSR 380 (Jakarta Validation)
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
// Si usas Java 8 y Spring Boot < 2.3 (javax.validation):
// import javax.validation.constraints.DecimalMin;
// import javax.validation.constraints.Digits;
// import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleReportDTO {

    // Validaciones para saleCash
    @NotNull(message = "{saleReport.field.notNull}")
    @DecimalMin(value = "0.0", message = "{saleReport.field.decimalMin}")
    @Digits(integer = 19, fraction = 2, message = "{saleReport.field.digits}")
    private BigDecimal saleCash;

    // Validaciones para saleCredit
    @NotNull(message = "{saleReport.field.notNull}")
    @DecimalMin(value = "0.0", message = "{saleReport.field.decimalMin}")
    @Digits(integer = 19, fraction = 2, message = "{saleReport.field.digits}")
    private BigDecimal saleCredit;

    // Validaciones para saleDebit
    @NotNull(message = "{saleReport.field.notNull}")
    @DecimalMin(value = "0.0", message = "{saleReport.field.decimalMin}")
    @Digits(integer = 19, fraction = 2, message = "{saleReport.field.digits}")
    private BigDecimal saleDebit;

    // Validaciones para saleATH
    @NotNull(message = "{saleReport.field.notNull}")
    @DecimalMin(value = "0.0", message = "{saleReport.field.decimalMin}")
    @Digits(integer = 19, fraction = 2, message = "{saleReport.field.digits}")
    private BigDecimal saleATH;

    // Validaciones para refundCash
    @NotNull(message = "{saleReport.field.notNull}")
    @DecimalMin(value = "0.0", message = "{saleReport.field.decimalMin}") // Los reembolsos no pueden ser negativos, solo 0 o positivos
    @Digits(integer = 19, fraction = 2, message = "{saleReport.field.digits}")
    private BigDecimal refundCash;

    // Validaciones para refundCredit
    @NotNull(message = "{saleReport.field.notNull}")
    @DecimalMin(value = "0.0", message = "{saleReport.field.decimalMin}")
    @Digits(integer = 19, fraction = 2, message = "{saleReport.field.digits}")
    private BigDecimal refundCredit;

    // Validaciones para refundDebit
    @NotNull(message = "{saleReport.field.notNull}")
    @DecimalMin(value = "0.0", message = "{saleReport.field.decimalMin}")
    @Digits(integer = 19, fraction = 2, message = "{saleReport.field.digits}")
    private BigDecimal refundDebit;

    // Validaciones para refundATH
    @NotNull(message = "{saleReport.field.notNull}")
    @DecimalMin(value = "0.0", message = "{saleReport.field.decimalMin}")
    @Digits(integer = 19, fraction = 2, message = "{saleReport.field.digits}")
    private BigDecimal refundATH;

    // Validaciones para stateTax (más decimales para impuestos)
    @NotNull(message = "{saleReport.field.notNull}")
    @DecimalMin(value = "0.0", message = "{saleReport.field.decimalMin}")
    @Digits(integer = 19, fraction = 4, message = "{saleReport.field.digits}") // Aumenta la fracción si lo necesitas
    private BigDecimal stateTax;

    // Validaciones para cityTax (más decimales para impuestos)
    @NotNull(message = "{saleReport.field.notNull}")
    @DecimalMin(value = "0.0", message = "{saleReport.field.decimalMin}")
    @Digits(integer = 19, fraction = 4, message = "{saleReport.field.digits}") // Aumenta la fracción si lo necesitas
    private BigDecimal cityTax;

    // Validaciones para reduceTax
    @NotNull(message = "{saleReport.field.notNull}")
    @DecimalMin(value = "0.0", message = "{saleReport.field.decimalMin}")
    @Digits(integer = 19, fraction = 2, message = "{saleReport.field.digits}")
    private BigDecimal reduceTax;

    public SaleReportDTO(SaleReport saleReport) {
        this.saleCash = saleReport.getSaleCash();
        this.saleCredit = saleReport.getSaleCredit();
        this.saleDebit = saleReport.getSaleDebit();
        this.saleATH = saleReport.getSaleATH();
        this.refundCash = saleReport.getRefundCash();
        this.refundCredit = saleReport.getRefundCredit();
        this.refundDebit = saleReport.getRefundDebit();
        this.refundATH = saleReport.getRefundATH();
        this.stateTax = saleReport.getStateTax();
        this.cityTax = saleReport.getCityTax();
        this.reduceTax = saleReport.getReduceTax();
    }

    public SaleReportDTO(SaleReportProjection saleReport) {
        this.saleCash = saleReport.getSaleCash();
        this.saleCredit = saleReport.getSaleCredit();
        this.saleDebit = saleReport.getSaleDebit();
        this.saleATH = saleReport.getSaleATH();
        this.refundCash = saleReport.getRefundCash();
        this.refundCredit = saleReport.getRefundCredit();
        this.refundDebit = saleReport.getRefundDebit();
        this.refundATH = saleReport.getRefundATH();
        this.stateTax = saleReport.getStateTax();
        this.cityTax = saleReport.getCityTax();
        this.reduceTax = saleReport.getReduceTax();
    }
}