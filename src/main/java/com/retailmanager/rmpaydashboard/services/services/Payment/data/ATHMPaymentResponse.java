package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter  @Setter
    @NoArgsConstructor @AllArgsConstructor
public class ATHMPaymentResponse {
     private String status;
    private Data data;
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class Data {
            /**
             * This ID represent the ticket of the transaction to be paid with the information provided in the request.
             */
            private String ecommerceId;
            /**
             * This token identifies the transaction authorized by the user pending to be submitted by the ecommerce app.
             * Token required to later execute /authorization service
             */
            private String auth_token; // Usamos camelCase para las propiedades en Java
    }
}
