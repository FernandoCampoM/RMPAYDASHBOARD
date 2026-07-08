
package com.retailmanager.rmpaydashboard.utils;

import com.google.gson.Gson;
import com.retailmanager.rmpaydashboard.services.services.EmailService.EmailBodyData;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMCancelPaymentResponse;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMPaymentResponse;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.FindPaymentResponse;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponseJSON;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponsePayment;

/**
 *
 * @author pomeo
 */
public class ConverterJson {
    public static ResponsePayment convertStr2RespPay(String json){
        return new Gson().fromJson(json, ResponsePayment.class);
    }
    public static ResponseJSON convertStr2RespJson(String json){
        return new Gson().fromJson(json, ResponseJSON.class);
    }
     public static ATHMPaymentResponse convertStr2RespPaymentATHM(String json){
        return new Gson().fromJson(json, ATHMPaymentResponse.class);
    }
    public static FindPaymentResponse convertStr2RespDetTransactionATHM(String json){
        return new Gson().fromJson(json, FindPaymentResponse.class);
    }
    public static ATHMCancelPaymentResponse convertStr2RespCancelATHM(String json){
        return new Gson().fromJson(json, ATHMCancelPaymentResponse.class);
    }
    public static EmailBodyData convertStr2EmailBodyData(String json){
        return new Gson().fromJson(json, EmailBodyData.class);
    }
    
}
