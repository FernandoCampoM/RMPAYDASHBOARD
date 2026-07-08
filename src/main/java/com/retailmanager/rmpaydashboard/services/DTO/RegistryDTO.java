package com.retailmanager.rmpaydashboard.services.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RegistryDTO {
    //INFORMACION DEL USUARIO
    @NotEmpty(message = "{user.username.empty}")
    @Size(min = 5, message = "{user.username.min}")
    private String username;
    @NotEmpty(message = "{user.name.empty}")
    private String name;
    @NotEmpty(message = "{user.email.empty}")
    @Email(message = "{user.email.format}")
    private String email;
    @NotEmpty(message = "{user.password.empty}")
    private String password;
    @NotEmpty(message = "{user.phone.empty}")
    private String phone;
    //INFORMACION DEL NEGOCIO
    private String merchantId;
    @NotBlank(message = "{business.name.empty}")
    @Size(max = 255, message = "{business.name.max}")
    private String businessName;

    @NotBlank(message = "{business.businessPhoneNumber.empty}")
    @Size(max = 20, message = "{business.businessPhoneNumber.max}")
    private String businessPhoneNumber;

    @NotNull(message = "{business.additionalTerminals.null}")
    @Min(value = 0, message = "{business.additionalTerminals.min}")
    private Integer additionalTerminals;
    @NotNull(message = "{registry.serviceId.null}")
    private Long serviceId;
    @NotNull(message = "{registry.automaticPayments.null}")
    private boolean automaticPayments;

    
    private Long idReseller=-1L;

    @Valid
    private AddressDTO address;
    private boolean terms;
    //INFORMACIÓN DEL PAGO
    
    private String paymethod;
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

    private InvoiceDTO paymentResume;
}
