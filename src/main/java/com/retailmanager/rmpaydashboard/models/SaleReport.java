package com.retailmanager.rmpaydashboard.models;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity // Marca esta clase como una entidad JPA
@Data // Lombok: Genera getters, setters, toString, equals y hashCode
@NoArgsConstructor // Lombok: Genera un constructor sin argumentos (requerido por JPA)
@AllArgsConstructor // Lombok: Genera un constructor con todos los argumentos
@Builder // Lombok: Permite usar el patrón de diseño Builder
public class SaleReport implements Serializable {

    // Clave primaria para SaleReport
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 19, scale = 2)
    private BigDecimal saleCash;

    @Column(precision = 19, scale = 2)
    private BigDecimal saleCredit;

    @Column( precision = 19, scale = 2)
    private BigDecimal saleDebit;

    @Column( precision = 19, scale = 2)
    private BigDecimal saleATH;

    @Column( precision = 19, scale = 2)
    private BigDecimal refundCash;

    @Column( precision = 19, scale = 2)
    private BigDecimal refundCredit;

    @Column( precision = 19, scale = 2)
    private BigDecimal refundDebit;

    @Column( precision = 19, scale = 2)
    private BigDecimal refundATH;

    // Aquí he puesto una precisión mayor (ej. 19, 4) ya que los impuestos pueden tener más decimales
    // pero puedes ajustarlo según tus necesidades.
    @Column( precision = 19, scale = 4)
    private BigDecimal stateTax;

    @Column( precision = 19, scale = 4)
    private BigDecimal cityTax;

    @Column( precision = 19, scale = 2)
    private BigDecimal reduceTax;

    // Relación OneToOne inversa con Shift.
    // MappedBy indica que la relación es gestionada por el campo 'saleReport' en la clase Shift.
    // No necesitamos @JoinColumn aquí porque la clave foránea está en la tabla 'shifts'.
    @OneToOne(mappedBy = "saleReport")
    private Shift shift; // Referencia a la entidad Shift a la que pertenece este SaleReport

    public SaleReport(SaleReportProjection saleReport, Shift shift){
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
        this.shift = shift;
    }
}