package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResponseJSON {
    private String Token;
    private String Identifier;
    private String msoft_code;
    private String PhardCode;
    private String AuthCode;
    private String verbiage;
    private int ResponseCode;
    private List<String> Msg;
    private String displayMessage;
}
