package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ATHMCancelPaymentResponse {
    /**
     * **Status**: Confirma el estado de la respuesta del servicio.
     * 
     */
    private String status;

    /**
     * **Data**: Contiene los detalles específicos de la transacción y el estado del comercio electrónico.
     */
    private String data;
}
