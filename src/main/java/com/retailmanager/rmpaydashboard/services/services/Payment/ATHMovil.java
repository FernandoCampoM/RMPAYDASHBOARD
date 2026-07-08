package com.retailmanager.rmpaydashboard.services.services.Payment;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConfigurationNotFoundException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConsumeAPIException;
import com.retailmanager.rmpaydashboard.repositories.Sys_general_configRepository;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMCancelPaymentResponse;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMPaymentReqData;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMPaymentResponse;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMovilData;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.FindPaymentReqData;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.FindPaymentResponse;
import com.retailmanager.rmpaydashboard.utils.ConverterJson;

@Service
public class ATHMovil  implements IATHMovilService{
     @Autowired
    private Sys_general_configRepository serviceDBConfig;
    @Override
    public ATHMPaymentResponse doPayment(ATHMPaymentReqData paymentData) throws ConsumeAPIException{
        
        ATHMovilData reqBS = new ATHMovilData();
        
        try{
            reqBS = loadAndValidATHM();   
        }catch(ConfigurationNotFoundException e){
            System.err.println("Error com.retailmanager.rmpaydashboard.services.services.Payment.ATHMovil.doPayment: " + e.getMessage());
            reqBS = null;
        }
        if(reqBS != null){
            paymentData.setPublicToken(reqBS.getPublicToken());
            
            String response = ConsumerApi.consumeAPIATHM(reqBS.getUrlPayment(), paymentData, null);
            return ConverterJson.convertStr2RespPaymentATHM(response);
        }else{
            return null;
        }
    }
    @Override
    public  FindPaymentResponse findPayment(FindPaymentReqData paymentData) throws ConsumeAPIException{
        
        ATHMovilData reqBS = new ATHMovilData();
        
        try{
            reqBS = loadAndValidATHM();
        }catch(ConfigurationNotFoundException e){
            System.err.println("Error com.retailmanager.rmpaydashboard.services.services.Payment.ATHMovil.findPayment: " + e);
            reqBS = null;
        }
        if(reqBS != null){
            paymentData.setPublicToken(reqBS.getPublicToken());
            
            String response = ConsumerApi.consumeAPIATHM(reqBS.getUrlFindPayment(), paymentData, null);
            return ConverterJson.convertStr2RespDetTransactionATHM(response);
        }else{
            return null;
        }
    }
     @Override
    public FindPaymentResponse confirmAuthorization(String authorization) throws ConsumeAPIException{

        ATHMovilData reqBS = new ATHMovilData();

        try{
            reqBS = loadAndValidATHM();   
        }catch(ConfigurationNotFoundException e){
            System.err.println("Error com.retailmanager.rmpaydashboard.services.services.Payment.ATHMovil.confirmAuthorization: " + e);
            reqBS = null;
        }
        if(reqBS != null){
            
            String response = ConsumerApi.consumeAPIATHM(reqBS.getUrlAuthorization(), null, authorization);
            return ConverterJson.convertStr2RespDetTransactionATHM(response);
        }else{
            return null;
        }
    }
     @Override
    public ATHMCancelPaymentResponse cancel(FindPaymentReqData paymentData) throws ConsumeAPIException{
  
        ATHMovilData reqBS = new ATHMovilData();    
        try{
            reqBS = loadAndValidATHM();   
        }catch(ConfigurationNotFoundException e){
            System.err.println("Error com.retailmanager.rmpaydashboard.services.services.Payment.ATHMovil.cancel: " + e);
            reqBS = null;
        }
        if(reqBS != null){
            paymentData.setPublicToken(reqBS.getPublicToken());
            
            String response = ConsumerApi.consumeAPIATHM(reqBS.getUrlCancel(), paymentData, null);
            return ConverterJson.convertStr2RespCancelATHM(response);
        }else{
            return null;
        }
    }
    /**
     * Loads and validates the ATHMovil configuration from the database.
     * @return
     * @throws ConfigurationNotFoundException
     */
    public ATHMovilData loadAndValidATHM() throws ConfigurationNotFoundException {
        ATHMovilData valueObject = new ATHMovilData();
        Object[] obj1 = serviceDBConfig.getATHMovilConfig();
        Object[] obj = (Object[]) obj1[0];

        valueObject.setUrlPayment(obj[0].toString());
        valueObject.setUrlFindPayment(obj[1].toString());
        valueObject.setUrlAuthorization(obj[2].toString());
        valueObject.setPublicToken(obj[3].toString());
        valueObject.setUrlCancel(obj[4].toString());

        HashMap<String, String> map = new HashMap<>();
        boolean bandera = false;

        if (valueObject != null) {
            if (valueObject.getUrlPayment() == null) {
                map.put("UrlPayment", "config.athmovil.UrlPayment not found.");
                bandera = true;
            }
            if (valueObject.getUrlFindPayment() == null) {
                map.put("UrlFindPayment", "config.athmovil.UrlFindPayment not found.");
                bandera = true;
            }
            if (valueObject.getUrlAuthorization() == null) {
                map.put("UrlAuthorization", "config.athmovil.UrlAuthorization not found.");
                bandera = true;
            }
            if (valueObject.getPublicToken() == null) {
                map.put("PublicToken", "config.athmovil.PublicToken not found.");
                bandera = true;
            }
            if (valueObject.getUrlCancel() == null) {
                map.put("UrlCancel", "config.athmovil.UrlCancel not found.");
                bandera = true;
            }

            if (bandera) {
                Gson gson = new Gson();
                String json = gson.toJson(map);
                throw new ConfigurationNotFoundException("[Debug] ATHMovil Configuration not found." + json);
            }
        }
        return valueObject;
    }
}
