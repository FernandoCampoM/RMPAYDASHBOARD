package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ATHMovilData {
    private  String urlPayment;
    private  String urlFindPayment;
    private  String urlAuthorization;
    private  String  publicToken;
    private String urlCancel;
}
