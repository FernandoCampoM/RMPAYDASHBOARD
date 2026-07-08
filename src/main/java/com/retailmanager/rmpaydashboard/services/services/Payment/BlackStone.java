
package com.retailmanager.rmpaydashboard.services.services.Payment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConfigurationNotFoundException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConsumeAPIException;
import com.retailmanager.rmpaydashboard.repositories.Sys_general_configRepository;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.CreditCardPayment;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.PaymentDetailsDTO;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ReqBlackStoneData;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponseJSON;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponsePayment;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.TokenPayment;
import com.retailmanager.rmpaydashboard.utils.ConverterJson;

@Service
public class BlackStone implements IBlackStoneService {
    @Autowired
    private Sys_general_configRepository serviceDBConfig;

    /**
     * Performs a payment transaction using a credit card.
     *
     * @param Amount                the payment amount
     * @param ZipCode               the zip code of the cardholder
     * @param CardNumber            the credit card number
     * @param ExpDate               the expiration date of the credit card
     * @param NameOnCard            the name on the credit card
     * @param CVN                   the card verification number
     * @param Track2                the track2 data of the credit card
     * @param UserTransactionNumber the transaction number
     * @return the payment response
     */
    public ResponsePayment paymentWithCreditCard(String Amount, String ZipCode, String CardNumber, String ExpDate,
            String NameOnCard,
            String CVN, String Track2, String UserTransactionNumber) throws ConsumeAPIException {
        ReqBlackStoneData reqBS = new ReqBlackStoneData();

        try {
            reqBS = loadAndValid();
        } catch (ConfigurationNotFoundException e) {
            System.err.println("Error: " + e);
            reqBS = null;
        }
        if (reqBS != null) {
            String AppKey = reqBS.getAppKey();
            String AppType = reqBS.getAppType();
            String mid = reqBS.getMid(); // un id cualqiera
            String cid = reqBS.getCid(); // un id cualqiera
            String UserName = reqBS.getUserName();
            String Password = reqBS.getPassword();

            CreditCardPayment creditCardPayment = new CreditCardPayment(AppKey, AppType, mid, cid, UserName, Password,
                    Amount,
                    ZipCode, CardNumber, ExpDate, NameOnCard, CVN, Track2, UserTransactionNumber, "1", "", "", "1");

            String response = ConsumerApi.consumeAPI(reqBS.getUrl(), creditCardPayment);
            return ConverterJson.convertStr2RespPay(response);
        } else {
            return null;
        }

    }

    /**
     * Load and validate the ReqBlackStoneData object and its properties.
     *
     * @throws NotFoundException if any required properties are not found in the
     *                           configuration
     * @return the validated ReqBlackStoneData object
     */
    public ReqBlackStoneData loadAndValid() throws ConfigurationNotFoundException {
        ReqBlackStoneData valueObject = new ReqBlackStoneData();
        Object[] obj1 = serviceDBConfig.getBlackStoneConfig();
        Object[] obj = (Object[]) obj1[0];
        valueObject.setAppKey(obj[0].toString());
        valueObject.setUrl(obj[1].toString());
        valueObject.setAppType(obj[2].toString());
        valueObject.setMid(obj[3].toString());
        valueObject.setCid(obj[4].toString());
        valueObject.setUserName(obj[5].toString());
        valueObject.setPassword(obj[6].toString());
        valueObject.setUrlForToken(obj[7].toString());
        valueObject.setUrlForPymentWithToken(obj[8].toString());

        HashMap<String, String> map = new HashMap<>();
        boolean bandera = false;
        if (valueObject != null) {
            if (valueObject.getAppKey() == null) {
                map.put("AppKey", "config.blackstone.AppKey not found.");
                bandera = true;
            }
            if (valueObject.getAppType() == null) {
                map.put("AppType", "config.blackstone.AppType not found.");
                bandera = true;
            }
            if (valueObject.getCid() == null) {
                map.put("cid", "config.blackstone.CID not found.");
                bandera = true;
            }
            if (valueObject.getMid() == null) {
                map.put("mid", "config.blackstone.MID not found.");
                bandera = true;
            }
            if (valueObject.getUserName() == null) {
                map.put("username", "config.blackstone.UserName not found.");
                bandera = true;
            }
            if (valueObject.getPassword() == null) {
                map.put("password", "config.blackstone.Password not found.");
                bandera = true;
            }
            if (valueObject.getUrl() == null) {
                map.put("url", "config.blackstone.URL not found.");
                bandera = true;
            }
            if (valueObject.getUrlForToken() == null) {
                map.put("UrlForToken", "config.blackstone.UrlForToken not found.");
                bandera = true;
            }
            if (valueObject.getUrlForPymentWithToken() == null) {
                map.put("UrlPaymentWithToken", "config.blackstone.UrlPaymentWithToken not found.");
                bandera = true;
            }
            if (bandera) {
                Gson gson = new Gson();
                String json = gson.toJson(map);
                throw new ConfigurationNotFoundException("[Debug] Blackstone Configuration not found." + json);
            }
        }
        return valueObject;
    }

    /**
     * Retrieves a token for a credit card payment using the BlackStone API.
     *
     * @param ZipCode               the zip code of the cardholder
     * @param CardNumber            the credit card number
     * @param ExpDate               the expiration date of the card
     * @param NameOnCard            the name on the credit card
     * @param CVN                   the card verification number
     * @param Track2                the track 2 data of the card (optional)
     * @param UserTransactionNumber the user-defined transaction number
     * @return a ResponseJSON object containing the token, or null if there was an
     *         error
     */
    @Override
    public ResponseJSON getToken(String ZipCode, String CardNumber, String ExpDate, String NameOnCard, String CVN,
            String Track2, String UserTransactionNumber) {
        String miIp = "";
        try {
            InetAddress ip = InetAddress.getLocalHost();
            miIp = ip.getHostAddress();
        } catch (UnknownHostException e) {
            System.out
                    .println("Error: com.retailmanager.rmpaydashboard.services.services.Payment.BlackStone.getToken(): "
                            + e.getMessage());
        }
        ReqBlackStoneData reqBS = new ReqBlackStoneData();

        try {
            reqBS = loadAndValid();
        } catch (ConfigurationNotFoundException e) {
            System.err.println("Error: " + e);
            reqBS = null;
        }
        if (reqBS != null) {
            String AppKey = reqBS.getAppKey();
            String AppType = reqBS.getAppType();
            String mid = reqBS.getMid(); // un id cualqiera
            String cid = reqBS.getCid(); // un id cualqiera
            String UserName = reqBS.getUserName();
            String Password = reqBS.getPassword();

            // CreditCardPayment creditCardPayment = new CreditCardPayment(AppKey, AppType,
            // mid, cid, UserName, Password, Amount,
            // ZipCode, CardNumber, ExpDate, NameOnCard, CVN, Track2, UserTransactionNumber,
            // "1", "", "", "1");

            PaymentDetailsDTO pd = new PaymentDetailsDTO();
            pd.setAccount(CardNumber);
            pd.setExpDate(ExpDate);
            pd.setNameOnCard(NameOnCard);
            pd.setStreet("");
            pd.setZipCode(ZipCode);
            pd.setCv(CVN);
            pd.setClientRef(UserTransactionNumber);
            pd.setDescription("Token for Credit Card Payment");
            pd.setExistingToken("");
            pd.setAppKey(AppKey);
            pd.setAppType(Integer.parseInt(AppType));
            pd.setMid(Integer.parseInt(mid));
            pd.setCid(Integer.parseInt(cid));
            pd.setUserName(UserName);
            pd.setPassword(Password);
            pd.setHostUserName("");
            pd.setHostPassword("");
            pd.setLogId(null);
            pd.setUserId(null);
            pd.setIpAddress(miIp);
            try {

                String response = ConsumerApi.consumeAPI(reqBS.getUrlForToken(), pd);
                System.out.println("Response from getToken: " + response);
                return ConverterJson.convertStr2RespJson(response);
            } catch (ConsumeAPIException e) {
                System.err.println("Error: " + e);
                return null;
            }
        } else {
            return null;
        }

    }

    /**
     * Performs a payment transaction using a token.
     *
     * @param Amount                the payment amount
     * @param ZipCode               the zip code of the cardholder
     * @param token                 the token for the payment
     * @param ExpDate               the expiration date of the card
     * @param NameOnCard            the name on the card
     * @param CVN                   the card verification number
     * @param Track2                the track 2 data of the card
     * @param UserTransactionNumber the user-defined transaction number
     * @return a ResponsePayment object containing the payment response or null if
     *         an error occurred
     * @throws ConsumeAPIException if there was an error consuming the API
     */
    @Override
    public ResponsePayment paymentWithToken(String Amount, String ZipCode, String token, String ExpDate,
            String NameOnCard, String CVN, String Track2, String UserTransactionNumber) throws ConsumeAPIException {
        ReqBlackStoneData reqBS = new ReqBlackStoneData();

        try {
            reqBS = loadAndValid();
        } catch (ConfigurationNotFoundException e) {
            System.err.println("Error: " + e);
            reqBS = null;
        }
        if (reqBS != null) {
            String AppKey = reqBS.getAppKey();
            String AppType = reqBS.getAppType();
            String mid = reqBS.getMid(); // un id cualqiera
            String cid = reqBS.getCid(); // un id cualqiera
            String UserName = reqBS.getUserName();
            String Password = reqBS.getPassword();
            System.out.println("AppKey: " + AppKey);
System.out.println("AppType: " + AppType);
System.out.println("Merchant ID (mid): " + mid);
System.out.println("Client ID (cid): " + cid);
System.out.println("UserName: " + UserName);
System.out.println("Password: " + Password);
System.out.println("Amount: " + Amount);
System.out.println("ZipCode: " + ZipCode);
System.out.println("Token: " + token);
System.out.println("NameOnCard: " + NameOnCard);
System.out.println("CVN: " + CVN);
System.out.println("Track2: " + Track2);
System.out.println("UserTransactionNumber: " + UserTransactionNumber);
System.out.println("URL: "+reqBS.getUrlForPymentWithToken());

            // El transaction type debe ser 2 para el payment con token
            TokenPayment creditCardPayment = new TokenPayment(AppKey, AppType, mid, cid, UserName, Password, Amount,
                    ZipCode, token, "", NameOnCard, CVN, Track2, UserTransactionNumber, "2", "", "", "1");
            String response = ConsumerApi.consumeAPI(reqBS.getUrlForPymentWithToken(), creditCardPayment);
            return ConverterJson.convertStr2RespPay(response);
        } else {
            return null;
        }
    }
}
