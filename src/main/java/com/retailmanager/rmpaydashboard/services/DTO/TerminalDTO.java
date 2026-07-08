package com.retailmanager.rmpaydashboard.services.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TerminalDTO {
    private String terminalId;

    private String serial;
    @NotBlank(message = "{terminal.name.empty}")
    @Size(max = 255, message = "{terminal.name.max}")
    private String name;
    @NotNull(message = "{terminal.enable.null}")
    private Boolean enable;
    private Long idService;
    private Long businesId;
    private Instant expirationDate;
    private Instant lastTransmision;

    //INDICA SI EL TERMINAL TIENE HABILITADOS LOS PAGOS AUTOMATICOS
    private boolean automaticPayments;
    //INDICA SI EL PAGO DEL TERMINAL SE HA REALIZADO
    private boolean isPayment;
    //INDICA SI EL TERMINAL ES EL QUE SE REALIZA EL COBRO PRINCIPAL DEL SERVICIO
    private boolean isPrincipal;
    private Instant registerDate;
    private Instant lastPayment;
}
