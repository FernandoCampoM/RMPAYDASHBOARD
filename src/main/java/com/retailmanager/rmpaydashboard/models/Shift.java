package com.retailmanager.rmpaydashboard.models;

import com.retailmanager.rmpaydashboard.models.enums.SyncStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor // Lombok: Genera un constructor sin argumentos (requerido por JPA)
@AllArgsConstructor // Lombok: Genera un constructor con todos los argumentos
@Builder // Lombok: Permite usar el patrón de diseño Builder
public class Shift {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private String shiftId;

    @Column()
    private String userName;

    @Column()
    private Instant startTime;

    @Column(nullable = true)
    private Instant endTime;
    @Column(precision = 19, scale = 2) // precision = total digitos, scale = decimales
    private BigDecimal balanceInicial;
    @Column(precision = 19, scale = 2) // precision = total digitos, scale = decimales
    private BigDecimal balanceFinal;
    @Column(precision = 19, scale = 2) // precision = total digitos, scale = decimales
    private BigDecimal cuadreFinal;

    @Column()
    private boolean openShifBalance;

    @Enumerated(EnumType.STRING)
    @Column()
    private SyncStatus syncStatus;

    @Column()
    private Instant lastSyncAt;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "saleReportId", referencedColumnName = "id") // Columna de unión en la tabla "shifts"
    private SaleReport saleReport;

    /* Esta es la relación con el empleado*/
    @ManyToOne
    @JoinColumn(name = "userBusinessId", nullable = false)
    private UsersBusiness userBusiness;
    @ManyToOne
    @JoinColumn(name = "terminalId", nullable = false)
    private Terminal terminal;


}