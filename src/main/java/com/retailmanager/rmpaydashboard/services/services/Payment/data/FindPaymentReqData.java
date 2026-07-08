package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FindPaymentReqData {
    /**
     * @param ecommerceId String. Este ID representa el ticket de la transacción a ser pagada con la información proporcionada en la solicitud.
     * Es un identificador único para la transacción en el sistema de comercio electrónico.
     */
    private String ecommerceId;

    /**
     * @param publicToken String. Determina la cuenta de negocio a la que se enviará el pago.
     * Es el token público asociado a la cuenta de ATH Móvil del negocio.
     */
    private String publicToken;
}
