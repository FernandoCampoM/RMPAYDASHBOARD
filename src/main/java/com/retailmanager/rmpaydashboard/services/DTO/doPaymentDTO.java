package com.retailmanager.rmpaydashboard.services.DTO;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor  @AllArgsConstructor
public class doPaymentDTO {
    private boolean terms;
    /**
     * NUMERO DE TERMINALES que se van a pagar
     */
    @NotNull(message = "{doPayment.terminalNumber.null}")
    @Positive(message = "{doPayment.terminalNumber.positive}") 
    private Integer terminalsNumber;
    private Long businessId;
    @Size(min = 1, message = "{doPayment.terminalsDoPayment.size}")
    private List<TerminalsDoPaymentDTO> terminalsDoPayment;




    //INFORMACIÓN DEL PAGO
    @NotNull(message = "{registry.automaticPayments.null}")
    private boolean automaticPayments;
    
    private String paymethod="";
    //PARA PAGO CON TARJETA
    private String nameoncard;
    private String creditcarnumber;
    private String securitycode;    
    private String cardType;        
    private String expDateMonth;
    private String expDateYear;
    
    //PARA PAGO CON CHEQUE
    private String accountNameBank;
    private String accountNumberBank;
    private String routeNumberBank;
    private Long chequeVoidId;

    //PARA PAGO CON ATH MOVIL
    private String athPhone;
}
