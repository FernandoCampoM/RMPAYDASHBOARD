package com.retailmanager.rmpaydashboard.services.services.InvoiceServices;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConsumeAPIException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.FileModel;
import com.retailmanager.rmpaydashboard.models.Invoice;
import com.retailmanager.rmpaydashboard.models.PaymentData;
import com.retailmanager.rmpaydashboard.models.Service;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.models.enums.Environment;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.FileRepository;
import com.retailmanager.rmpaydashboard.repositories.InvoiceRepository;
import com.retailmanager.rmpaydashboard.repositories.PaymentDataRepository;
import com.retailmanager.rmpaydashboard.repositories.ServiceRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.services.DTO.ConfirmPaymentDTO;
import com.retailmanager.rmpaydashboard.services.DTO.InvoiceDTO;
import com.retailmanager.rmpaydashboard.services.DTO.PaymentDataDTO;
import com.retailmanager.rmpaydashboard.services.DTO.PaymentHistoryReport;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalsDoPaymentDTO;
import com.retailmanager.rmpaydashboard.services.DTO.doPaymentDTO;
import com.retailmanager.rmpaydashboard.services.services.EmailService.EmailBodyData;
import com.retailmanager.rmpaydashboard.services.services.EmailService.IEmailService;
import com.retailmanager.rmpaydashboard.services.services.Payment.IATHMovilService;
import com.retailmanager.rmpaydashboard.services.services.Payment.IBlackStoneService;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMCancelPaymentResponse;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMPaymentReqData;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMPaymentResponse;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.FindPaymentReqData;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.FindPaymentResponse;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ItemATHM;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponseJSON;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponsePayment;
import com.retailmanager.rmpaydashboard.utils.ConverterJson;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Service
public class InvoiceServices implements IInvoiceServices {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ServiceRepository serviceDBService;
    @Autowired
    private IEmailService emailService;
    @Autowired
    private InvoiceRepository serviceDBInvoice;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    DecimalFormat formato = new DecimalFormat("#.##");

    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private IBlackStoneService blackStoneService;
    @Autowired
    private IATHMovilService athMovilService;
    String msgError = "";
    @Autowired
    private TerminalRepository serviceDBTerminal;
    @Autowired
    PaymentDataRepository paymentDataRepository;
    Gson gson = new Gson();

    @Value("${spring.profiles.active}")
    private String activeProfile;

    public boolean isProd() {
        return Environment.PROD.name().equalsIgnoreCase(activeProfile);
    }

    /**
     * Retrieves the payment history for a given business within a specified date
     * range.
     *
     * @param businessId the ID of the business
     * @param startDate  the start date of the payment history
     * @param endDate    the end date of the payment history
     * @return a ResponseEntity containing the payment history or an error message
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPaymentHistoryByBusiness(Long businessId, LocalDate startDate, LocalDate endDate) {
        if (businessId != null && startDate != null && endDate != null) {
            List<Invoice> listInvoice = this.invoiceRepository
                    .findByBusinessIdAndDateGreaterThanEqualAndDateLessThanEqualOrderByInvoiceNumberDesc(businessId,
                            startDate, endDate);
            List<InvoiceDTO> listInvoiceDTO = this.mapper.map(listInvoice, new TypeToken<List<InvoiceDTO>>() {
            }.getType());
            return new ResponseEntity<>(listInvoiceDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>("{\"message\":\"Debe especificar businessId, startDate y endDate\"}",
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Processes a payment request and generates an invoice.
     *
     * @param prmPaymentInfo the payment information DTO
     * @return the response entity containing the invoice DTO or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> doPayment(doPaymentDTO prmPaymentInfo) {
        final double stateTaxRate = 0.04;
        Double totalAmount = 0.0;
        ResponsePayment respPayment;
        String serviceReferenceNumber = null;
        String userTransactionNumber = uniqueString();
        Long serviceIdPrincipal = -1L;
        Double stateTax = 0.0;
        EmailBodyData objEmailBodyData = mapper.map(prmPaymentInfo, EmailBodyData.class);
        List<String> paymentDescription = new ArrayList<>();
        List<ItemATHM> items = new ArrayList<>(); // se usa para informarle al cliente los items que se estan pagando en ATH Movil
        Business objBusiness = this.serviceDBBusiness.findById(prmPaymentInfo.getBusinessId()).orElse(null);
        if (objBusiness == null) {
            EntidadNoExisteException objExeption = new EntidadNoExisteException(
                    "El Negocio con businessId " + prmPaymentInfo.getBusinessId() + " no existe en la Base de datos");
            throw objExeption;
        }

        try {
            objEmailBodyData.setEmail(objBusiness.getUser().getEmail());
            objEmailBodyData.setMerchantId(objBusiness.getMerchantId());
            objEmailBodyData.setBusinessName(objBusiness.getName());
            objEmailBodyData.setName(objBusiness.getUser().getName());

            prmPaymentInfo = validateData(prmPaymentInfo);
            if (prmPaymentInfo == null) {
                return new ResponseEntity<String>(msgError, HttpStatus.BAD_REQUEST);
            }
            // Validar que existan todos los Servicios
            HashMap<Long, Service> listService = new HashMap<>();
            for (TerminalsDoPaymentDTO objTerminal : prmPaymentInfo.getTerminalsDoPayment()) {
                if (!listService.containsKey(objTerminal.getIdService())) {
                    Optional<Service> optional = this.serviceDBService.findById(objTerminal.getIdService());
                    if (optional.isPresent()) {

                        listService.put(objTerminal.getIdService(), optional.get());
                    } else {
                        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Servicio con serviceId "
                                + objTerminal.getIdService() + " no existe en la Base de datos");
                        throw objExeption;
                    }
                }
                Terminal objTerminalDB = this.serviceDBTerminal.findById(objTerminal.getTerminalId()).orElse(null);
                if (objTerminalDB == null) {
                    throw new EntidadNoExisteException("El Terminal con terminalId " + objTerminal.getTerminalId()
                            + " no existe en la Base de datos");
                }
                Double amount = 0.0;
                Service objService = listService.get(objTerminal.getIdService());
                String descripcion = "";

                ItemATHM item = new ItemATHM(); // se usa para informarle al cliente los items que se estan pagando en ATH Movil

                if (objTerminalDB.isPrincipal()) {
                    descripcion = "Terminal Principal ID [" + objTerminal.getTerminalId() + "] - "
                            + objService.getServiceName() + ": $"
                            + String.valueOf(formato.format(objService.getServiceValue())) + "\n";
                    amount = objService.getServiceValue();
                    objTerminal.setPrincipal(true);
                    serviceIdPrincipal = objService.getServiceId();
                    //INFORMACIÓN PARA ATH MOVIL
                    item.setDescription(descripcion);
                    item.setName(objService.getServiceName());
                    double result = BigDecimal.valueOf(objService.getServiceValue())
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    item.setPrice(String.valueOf(result));
                    item.setTax(String.valueOf(objService.getServiceValue() * stateTaxRate));
                    item.setQuantity("1");
                    items.add(item);
                } else {
                    objTerminal.setPrincipal(false);
                    if (prmPaymentInfo.getTerminalsNumber() <= 5) {
                        descripcion = "Terminal Adicional ID [" + objTerminal.getTerminalId() + "] - "
                                + objService.getServiceName() + ": $"
                                + String.valueOf(formato.format(objService.getTerminals2to5())) + "\n";
                        amount = objService.getTerminals2to5();
                        //INFORMACIÓN PARA ATH MOVIL
                        item.setDescription(descripcion);
                        item.setName(objService.getServiceName());
                        double result = BigDecimal.valueOf(objService.getTerminals2to5())
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue();
                        item.setPrice(String.valueOf(result));
                        item.setTax(String.valueOf(objService.getTerminals2to5() * stateTaxRate));
                        item.setQuantity("1");
                        items.add(item);
                    } else if (prmPaymentInfo.getTerminalsNumber() > 5 && prmPaymentInfo.getTerminalsNumber() < 10) {
                        descripcion = "Terminal Adicional ID [" + objTerminal.getTerminalId() + "] - "
                                + objService.getServiceName() + ": $"
                                + String.valueOf(formato.format(objService.getTerminals6to9())) + "\n";
                        amount = objService.getTerminals6to9();
                        //INFORMACIÓN PARA ATH MOVIL
                        item.setDescription(descripcion);
                        item.setName(objService.getServiceName());
                        double result = BigDecimal.valueOf(objService.getTerminals6to9())
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue();
                        item.setPrice(String.valueOf(result));
                        item.setTax(String.valueOf(objService.getTerminals6to9() * stateTaxRate));
                        item.setQuantity("1");
                        items.add(item);
                    } else {
                        descripcion = "Terminal Adicional ID [" + objTerminal.getTerminalId() + "] - "
                                + objService.getServiceName() + ": $"
                                + String.valueOf(formato.format(objService.getTerminals10())) + "\n";
                        amount = objService.getTerminals10();
                        //INFORMACIÓN PARA ATH MOVIL
                        item.setDescription(descripcion);
                        item.setName(objService.getServiceName());
                        double result = BigDecimal.valueOf(objService.getTerminals10())
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue();
                        item.setPrice(String.valueOf(result));
                        item.setTax(String.valueOf(objService.getTerminals10() * stateTaxRate));
                        item.setQuantity("1");
                        items.add(item);
                    }
                }
                objTerminal.setAmount(amount);
                totalAmount += amount;
                paymentDescription.add(descripcion);
                objTerminal.setServiceDescription(descripcion);
            }
            objEmailBodyData.setDiscount(0.0);
            if (objBusiness.getDiscount() != 0.0) {
                objEmailBodyData.setReferenceNumber(
                        "DESCUENTO APLICADO:$" + String.valueOf(formato.format(objBusiness.getDiscount())));
                objEmailBodyData.setDiscount(objBusiness.getDiscount());

                if (objBusiness.getDiscount() < totalAmount) {
                    totalAmount = totalAmount - objBusiness.getDiscount();
                    objBusiness.setDiscount(0.0);
                } else {
                    prmPaymentInfo.setPaymethod("PAID-WITH-DISCOUNT");
                    objBusiness.setDiscount(objBusiness.getDiscount() - totalAmount);
                    totalAmount = 0.0;
                }
            }
            stateTax = totalAmount * 0.04;
            objEmailBodyData.setSubTotal(totalAmount);
            objEmailBodyData.setStateTax(stateTax);
            objEmailBodyData.setAmount(totalAmount + stateTax);
            totalAmount = totalAmount + stateTax;
            objBusiness.setAdditionalTerminals((Integer) prmPaymentInfo.getTerminalsNumber());
            if (serviceIdPrincipal != -1L) {
                objBusiness.setServiceId(serviceIdPrincipal);
            }
            objEmailBodyData.setServiceDescription(gson.toJson(paymentDescription));
            objEmailBodyData.setAdditionalTerminals((Integer) prmPaymentInfo.getTerminalsNumber());
            switch (prmPaymentInfo.getPaymethod()) {
                case "CREDIT-CARD":
                    respPayment = blackStoneService.paymentWithCreditCard(String.valueOf(formato.format(totalAmount)),
                            objBusiness.getAddress().getZipcode(),
                            prmPaymentInfo.getCreditcarnumber().replaceAll("-", ""),
                            prmPaymentInfo.getExpDateMonth() + prmPaymentInfo.getExpDateYear(),
                            prmPaymentInfo.getNameoncard(),
                            prmPaymentInfo.getSecuritycode(), null, userTransactionNumber);
                    if (respPayment.getResponseCode() != 200) {
                        objEmailBodyData.setReferenceNumber(respPayment.getServiceReferenceNumber());
                        objEmailBodyData.setErrorMessage(respPayment.getMsg().toString());
                        emailService.notifyErrorPayment(objEmailBodyData);
                        HashMap<String, String> objError = new HashMap<String, String>();
                        objError.put("msg", "No se pudo registrar el pago con la tarjeta de credito");
                        return new ResponseEntity<HashMap<String, String>>(objError, HttpStatus.NOT_ACCEPTABLE);
                    }
                    serviceReferenceNumber = respPayment.getServiceReferenceNumber() + " - "
                            + objEmailBodyData.getReferenceNumber();
                    objEmailBodyData
                            .setReferenceNumber(serviceReferenceNumber);
                    break;
                case "TOKEN":
                    if (objBusiness.getPaymentData() == null) {
                        throw new EntidadNoExisteException("El business con businessId " + objBusiness.getBusinessId()
                                + " no tiene creado un token de pago");
                    }
                    objEmailBodyData.setNameoncard(objBusiness.getPaymentData().getNameOnCard());
                    objEmailBodyData
                            .setCreditcarnumber("XXXX-XXXX-XXXX-" + objBusiness.getPaymentData().getLast4Digits());
                    objEmailBodyData.setExpDateMonth(objBusiness.getPaymentData().getExpDate().substring(0, 2));
                    objEmailBodyData.setExpDateYear(objBusiness.getPaymentData().getExpDate().substring(2, 4));
                    respPayment = blackStoneService.paymentWithToken(String.valueOf(formato.format(totalAmount)),
                            objBusiness.getAddress().getZipcode(),
                            objBusiness.getPaymentData().getToken(),
                            objBusiness.getPaymentData().getExpDate(),
                            objBusiness.getPaymentData().getNameOnCard(),
                            objBusiness.getPaymentData().getCvn(), null, userTransactionNumber);
                    if (respPayment.getResponseCode() != 200) {
                        objEmailBodyData.setErrorMessage(respPayment.getMsg().toString());
                        objEmailBodyData.setReferenceNumber(respPayment.getServiceReferenceNumber());
                        emailService.notifyErrorPayment(objEmailBodyData);
                        HashMap<String, String> objError = new HashMap<String, String>();
                        objError.put("msg", "No se pudo registrar el pago con EL TOKEN de la tarjeta de credito");
                        return new ResponseEntity<HashMap<String, String>>(objError, HttpStatus.NOT_ACCEPTABLE);
                    }
                    serviceReferenceNumber = respPayment.getServiceReferenceNumber() + " - "
                            + objEmailBodyData.getReferenceNumber();
                    objEmailBodyData
                            .setReferenceNumber(serviceReferenceNumber);
                    break;
            }
            Invoice objInvoice = new Invoice();
            objInvoice.setTotalAmount(totalAmount);
            objInvoice.setSubTotal(objEmailBodyData.getSubTotal());
            objInvoice.setStateTax(stateTax);
            objInvoice.setPaymentDescription(gson.toJson(paymentDescription));

            objInvoice.setPaymentMethod(prmPaymentInfo.getPaymethod());
            ATHMPaymentResponse payResponse = null;
            List<String> listTerminalIds = new ArrayList<String>();
            switch (prmPaymentInfo.getPaymethod()) {
                case "CREDIT-CARD":
                    objBusiness.setLastPayment(Instant.now());

                    for (TerminalsDoPaymentDTO objTerminal : prmPaymentInfo.getTerminalsDoPayment()) {
                        Service service = listService.get(objTerminal.getIdService());
                        Terminal objTer = this.serviceDBTerminal.findById(objTerminal.getTerminalId()).orElse(null);
                        objTer.setEnable(true);
                        // se incrementa la fecha de expiración del terminal de acuerdo a la duración
                        // del servicio
                        if (objTer.getExpirationDate() != null && objTer.isEnable() && objTer.isPayment()) {
                            if (objTer.getExpirationDate().isBefore(Instant.now())) {
                                objTer.setExpirationDate(Instant.now().plus(Duration.ofDays(service.getDuration())));
                            } else {
                                objTer.setExpirationDate(objTer.getExpirationDate().plus(Duration.ofDays(service.getDuration())));
                            }

                        } else {
                            objTer.setExpirationDate(Instant.now().plus(Duration.ofDays(service.getDuration())));
                        }
                        if (objTer.isPrincipal()) {
                            objBusiness.setPriorNotification(null);
                            objBusiness.setAfterNotification(null);
                            objBusiness.setLastDayNotification(null);

                        }
                        objTer.setLastPaymentValue(objTerminal.getAmount());
                            objTer.setPayment(true);
                        objTer.setService(service);
                        objTer.setAutomaticPayments(prmPaymentInfo.isAutomaticPayments());
                        objTer.setLastPayment(objBusiness.getLastPayment());
                        objTer = this.serviceDBTerminal.save(objTer);
                        listTerminalIds.add(objTerminal.getTerminalId());
                    }

                    objInvoice.setDate(LocalDate.now());
                    objInvoice.setTime(LocalTime.now());

                    objInvoice.setTerminals(prmPaymentInfo.getTerminalsNumber());
                    objInvoice.setBusinessId(objBusiness.getBusinessId());
                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                    objInvoice.setServiceId(objBusiness.getServiceId());
                    objInvoice.setInProcess(false);
                    objInvoice.setTerminalIds(
                            listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));

                    objInvoice = serviceDBInvoice.save(objInvoice);
                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                    objEmailBodyData.setTerminalsDoPayment(prmPaymentInfo.getTerminalsDoPayment());
                    emailService.notifyPaymentCreditCard(objEmailBodyData);
                    break;
                case "TOKEN":
                    objBusiness.setLastPayment(Instant.now());

                    for (TerminalsDoPaymentDTO objTerminal : prmPaymentInfo.getTerminalsDoPayment()) {
                        Service service = listService.get(objTerminal.getIdService());
                        Terminal objTer = this.serviceDBTerminal.findById(objTerminal.getTerminalId()).orElse(null);
                        objTer.setEnable(true);
                        // se incrementa la fecha de expiración del terminal de acuerdo a la duración
                        // del servicio
                        if (objTer.getExpirationDate() != null && objTer.isEnable() && objTer.isPayment()) {
                            if (objTer.getExpirationDate().isBefore(Instant.now())) {
                                objTer.setExpirationDate(Instant.now().plus(Duration.ofDays(service.getDuration())));
                            } else {
                                objTer.setExpirationDate(objTer.getExpirationDate().plus(Duration.ofDays(service.getDuration())));
                            }

                        } else {
                            objTer.setExpirationDate(Instant.now().plus(Duration.ofDays(service.getDuration())));
                        }
                        if (objTer.isPrincipal()) {
                            objBusiness.setPriorNotification(null);
                            objBusiness.setAfterNotification(null);
                            objBusiness.setLastDayNotification(null);

                        }
                        objTer.setLastPaymentValue(objTerminal.getAmount());
                        objTer.setPayment(true);
                        objTer.setService(service);
                        objTer.setLastPayment(objBusiness.getLastPayment());
                        objTer.setAutomaticPayments(prmPaymentInfo.isAutomaticPayments());
                        objTer = this.serviceDBTerminal.save(objTer);
                        listTerminalIds.add(objTerminal.getTerminalId());
                    }

                    objInvoice.setDate(LocalDate.now());
                    objInvoice.setTime(LocalTime.now());

                    objInvoice.setTerminals(prmPaymentInfo.getTerminalsNumber());
                    objInvoice.setBusinessId(objBusiness.getBusinessId());
                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                    objInvoice.setServiceId(objBusiness.getServiceId());
                    objInvoice.setInProcess(false);
                    objInvoice.setTerminalIds(
                            listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));

                    objInvoice = serviceDBInvoice.save(objInvoice);
                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                    objEmailBodyData.setTerminalsDoPayment(prmPaymentInfo.getTerminalsDoPayment());
                    emailService.notifyPaymentToken(objEmailBodyData);
                    break;
                case "ATHMOVIL":
                    if (prmPaymentInfo.getAthPhone() == null) {
                        HashMap<String, String> rta = new HashMap<>();
                        rta.put("msg", "El Negocio no tiene un número de telefono para enviar el pago de ATHMovil");
                        return new ResponseEntity<>(rta, HttpStatus.BAD_REQUEST);
                    }
                    if (prmPaymentInfo.getAthPhone().compareTo("") == 0) {
                        HashMap<String, String> rta = new HashMap<>();
                        rta.put("msg", "El Negocio no tiene un número de telefono para enviar el pago de ATHMovil");
                        return new ResponseEntity<>(rta, HttpStatus.BAD_REQUEST);
                    }
                    // Se crea la data para enviar el pago a ATH Movil
                    ATHMPaymentReqData req = new ATHMPaymentReqData();
                    req.setEnv("production");
                    req.setMetadata1("RETAIL MANAGER PR - RMPAY DASHBOARD");
                    String cleanPhone = prmPaymentInfo.getAthPhone().replaceAll("\\D", "");

                    req.setPhoneNumber(cleanPhone);
                    req.setSubtotal(String.valueOf(objInvoice.getSubTotal()));
                    req.setTax(String.valueOf(objInvoice.getStateTax()));
                    req.setTimeout("5000");
                    double resultado = BigDecimal.valueOf(objInvoice.getTotalAmount())
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    req.setTotal(String.valueOf(resultado));
                    req.setItems(items);
                    System.out.println();
                    try {
                        payResponse = athMovilService.doPayment(req);
                        if (payResponse != null && payResponse.getStatus().compareTo("success") == 0 && payResponse.getData() == null) {

                            JsonObject json = new JsonObject();
                            json.addProperty("msg", "Error al pagar con ATHMovil para el merchantId: " + objBusiness.getMerchantId());

                            return new ResponseEntity<>(json, HttpStatus.NOT_ACCEPTABLE);
                        }
                    } catch (ConsumeAPIException e) {
                        System.err.println("Error en el consumo de ATHMovil: CodigoHttp " + e.getHttpStatusCode() + " \n Mensje: " + e.getMessage());
                        JsonObject json = new JsonObject();
                        json.addProperty("msg", "Por favor comuniquese con el administrador de la página. Error: " + e.getMessage());

                        return new ResponseEntity<>(json, HttpStatus.BAD_GATEWAY);
                    }

                    for (TerminalsDoPaymentDTO objTerminal : prmPaymentInfo.getTerminalsDoPayment()) {
                        Service service = listService.get(objTerminal.getIdService());
                        Terminal objTer = this.serviceDBTerminal.findById(objTerminal.getTerminalId()).orElse(null);
                        objTer.setEnable(true);

                        // se incrementa la fecha de expiración del terminal de acuerdo a la duración
                        // del servicio
                        /*
                         if (objTer.getExpirationDate() != null && objTer.isEnable() && objTer.isPayment())
                         {
                            objTer.setExpirationDate(objTer.getExpirationDate().plus(Duration.ofDays(service.getDuration())));
                         } else {
                            objTer.setExpirationDate(Instant.now().plus(Duration.ofDays(service.getDuration())));
                         }*/
                        //objTer.setPayment(true);
                        objTer.setLastPaymentValue(objTerminal.getAmount());
                        objTer.setService(service);
                        objTer.setAutomaticPayments(prmPaymentInfo.isAutomaticPayments());
                        objTer = this.serviceDBTerminal.save(objTer);
                        listTerminalIds.add(objTerminal.getTerminalId());
                    }
                    serviceReferenceNumber = gson.toJson(payResponse);
                    objInvoice.setDate(LocalDate.now());
                    objInvoice.setTime(LocalTime.now());
                    objInvoice.setPaymentMethod(prmPaymentInfo.getPaymethod());
                    objInvoice.setTerminals(prmPaymentInfo.getTerminalsNumber());
                    objInvoice.setBusinessId(objBusiness.getBusinessId());
                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                    objInvoice.setServiceId(objBusiness.getServiceId());
                    objInvoice.setInProcess(true);
                    objInvoice.setTerminalIds(
                            listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));
                    objEmailBodyData.setTerminalsDoPayment(prmPaymentInfo.getTerminalsDoPayment());
                    objInvoice.setATHMPaymentDetails(gson.toJson(objEmailBodyData));
                    objInvoice = serviceDBInvoice.save(objInvoice);
                    //objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());

                    //emailService.notifyPaymentATHMovil(objEmailBodyData);
                    break;
                case "BANK-ACCOUNT":
                    for (TerminalsDoPaymentDTO objTerminal : prmPaymentInfo.getTerminalsDoPayment()) {
                        Service service = listService.get(objTerminal.getIdService());
                        Terminal objTer = this.serviceDBTerminal.findById(objTerminal.getTerminalId()).orElse(null);
                        objTer.setEnable(true);
                        // se incrementa la fecha de expiración del terminal de acuerdo a la duración
                        // del servicio
                        /*
                         * if (objTer.getExpirationDate() != null && objTer.isEnable() &&
                         * objTer.isPayment()) {
                         * objTer.setExpirationDate(objTer.getExpirationDate().plusDays(service.
                         * getDuration()));
                         * } else {
                         * objTer.setExpirationDate(LocalDate.now().plusDays(service.getDuration()));
                         * }
                         */

                        // objTer.setPayment(false);
                        objTer.setService(service);
                        objTer.setLastPaymentValue(objTerminal.getAmount());
                        objTer.setAutomaticPayments(prmPaymentInfo.isAutomaticPayments());
                        objTer = this.serviceDBTerminal.save(objTer);
                        listTerminalIds.add(objTerminal.getTerminalId());
                    }

                    objInvoice.setDate(LocalDate.now());
                    objInvoice.setTime(LocalTime.now());
                    objInvoice.setPaymentMethod(prmPaymentInfo.getPaymethod());
                    objInvoice.setTerminals(prmPaymentInfo.getTerminalsNumber());
                    objInvoice.setBusinessId(objBusiness.getBusinessId());
                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                    objInvoice.setServiceId(objBusiness.getServiceId());
                    objInvoice.setInProcess(true);
                    objInvoice.setTerminalIds(
                            listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));
                    objInvoice = serviceDBInvoice.save(objInvoice);
                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                    objEmailBodyData.setTerminalsDoPayment(prmPaymentInfo.getTerminalsDoPayment());
                    emailService.notifyPaymentBankAccount(objEmailBodyData);
                    break;
                case "PAID-WITH-DISCOUNT":
                    objBusiness.setLastPayment(Instant.now());
                    for (TerminalsDoPaymentDTO objTerminal : prmPaymentInfo.getTerminalsDoPayment()) {
                        Service service = listService.get(objTerminal.getIdService());
                        Terminal objTer = this.serviceDBTerminal.findById(objTerminal.getTerminalId()).orElse(null);
                        objTer.setEnable(true);
                        // se incrementa la fecha de expiración del terminal de acuerdo a la duración
                        // del servicio
                        if (objTer.getExpirationDate() != null && objTer.isEnable() && objTer.isPayment()) {
                            if (!objTer.getExpirationDate().isBefore(Instant.now())) {
                                objTer.setExpirationDate(objTer.getExpirationDate().plus(Duration.ofDays(service.getDuration())));
                            } else {
                                objTer.setExpirationDate(Instant.now().plus(Duration.ofDays(service.getDuration())));
                            }
                        } else {
                            objTer.setExpirationDate(Instant.now().plus(Duration.ofDays(service.getDuration())));
                        }

                        objTer.setPayment(true);
                        objTer.setService(service);
                        objTer.setLastPaymentValue(objTerminal.getAmount());
                        objTer.setAutomaticPayments(prmPaymentInfo.isAutomaticPayments());
                        objTer = this.serviceDBTerminal.save(objTer);
                        listTerminalIds.add(objTerminal.getTerminalId());
                    }

                    objInvoice.setDate(LocalDate.now());
                    objInvoice.setTime(LocalTime.now());
                    objInvoice.setPaymentMethod(prmPaymentInfo.getPaymethod());
                    objInvoice.setTerminals(prmPaymentInfo.getTerminalsNumber());
                    objInvoice.setBusinessId(objBusiness.getBusinessId());
                    objInvoice.setReferenceNumber(" DESCUENTO APLICADO: $" + objEmailBodyData.getDiscount());
                    objInvoice.setServiceId(objBusiness.getServiceId());
                    objInvoice.setInProcess(false);
                    objInvoice.setTerminalIds(
                            listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));
                    objInvoice = serviceDBInvoice.save(objInvoice);
                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                    objEmailBodyData.setTerminalsDoPayment(prmPaymentInfo.getTerminalsDoPayment());
                    emailService.notifyPaymentDiscount(objEmailBodyData);
                    break;
            }
            this.serviceDBBusiness.save(objBusiness);
            InvoiceDTO objInvoiceDTO = this.mapper.map(objInvoice, InvoiceDTO.class);
            return new ResponseEntity<InvoiceDTO>(objInvoiceDTO, HttpStatus.OK);
        } catch (ConsumeAPIException ex) {
            System.err.println("Error en el consumo de BlackStone: CodigoHttp " + ex.getHttpStatusCode()
                    + " \n Mensje: " + ex.getMessage());

            HashMap<String, String> map = new HashMap<>();
            map.put("msg", "Error en el consumo de BlackStone: CodigoHttp " + ex.getHttpStatusCode() + " \n Mensje: "
                    + ex.getMessage() + "Por favor comuniquese con el administrador de la página.");
            return new ResponseEntity<HashMap<String, String>>(map, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates a unique string using UUID.
     *
     * @return the unique string generated
     */
    public String uniqueString() {
        String random = UUID.randomUUID().toString();
        random = random.replaceAll("-", "");
        random = random.substring(0, 16);

        return random;
    }

    /**
     * Validates the given RegistryDTO object based on the pay method.
     *
     * @param prmRegistry The RegistryDTO object to be validated
     * @return The validated RegistryDTO object, or null if there is an error
     */
    private doPaymentDTO validateData(doPaymentDTO prmRegistry) {
        if (prmRegistry.getPaymethod() != null && prmRegistry.getPaymethod().compareTo("CREDIT-CARD") == 0) {
            if (prmRegistry.getCreditcarnumber() != null) {
                if (!prmRegistry.getCreditcarnumber().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")) {
                    msgError = "Letters are not allowed in the credit card number";
                    return null;
                }
            } else {
                msgError = "The credit card number is required";
                return null;
            }
            if (prmRegistry.getNameoncard() != null) {
                prmRegistry.setNameoncard(prmRegistry.getNameoncard().toUpperCase().trim());
            } else {
                msgError = "The name on card is required";
                return null;
            }
            if (prmRegistry.getSecuritycode() != null) {
                if (!prmRegistry.getSecuritycode().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")) {
                    msgError = "Letters are not allowed in the security code";
                    return null;
                }
            } else {
                msgError = "The security code is required";
                return null;
            }
            if (prmRegistry.getExpDateMonth() != null) {
                if (!prmRegistry.getExpDateMonth().matches("[+-]?\\d*(\\.\\d+)?")) {
                    msgError = "Letters are not allowed in the expiration date";
                    return null;
                } else {
                    if (Integer.parseInt(prmRegistry.getExpDateMonth()) > 12) {
                        msgError = "The expiration date month must be less than or equal to 12";
                        return null;
                    }
                }
            } else {
                msgError = "The expiration date month is required";
                return null;
            }
            if (prmRegistry.getExpDateYear() != null) {
                if (!prmRegistry.getExpDateYear().matches("[+-]?\\d*(\\.\\d+)?")) {
                    msgError = "Letters are not allowed in the expiration date";
                    return null;
                } else {
                    prmRegistry.setExpDateYear(prmRegistry.getExpDateYear().trim());
                    if (prmRegistry.getExpDateYear().length() != 2) {
                        msgError = "The expiration date year must be 2 digits";
                        return null;
                    }
                }
            } else {
                msgError = "The expiration date year is required";
                return null;
            }

        } else if (prmRegistry.getPaymethod() != null && prmRegistry.getPaymethod().compareTo("BANK-ACCOUNT") == 0) {
            if (prmRegistry.getChequeVoidId() != null) {
                Optional<FileModel> fileModel = this.fileRepository.findById(prmRegistry.getChequeVoidId());
                if (!fileModel.isPresent()) {
                    msgError = "The cheque void file is required";
                    return null;
                }
            }
            if (prmRegistry.getAccountNameBank() != null) {
                prmRegistry.setAccountNameBank(prmRegistry.getAccountNameBank().toUpperCase().trim());
            } else {
                msgError = "The account name is required";
                return null;
            }
            if (prmRegistry.getAccountNumberBank() != null) {
                if (!prmRegistry.getAccountNumberBank().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")) {
                    msgError = "Letters are not allowed in the account number";
                    return null;
                } else {
                    prmRegistry.setAccountNumberBank(prmRegistry.getAccountNumberBank().trim());
                }
            } else {
                msgError = "The account number is required";
                return null;
            }
            if (prmRegistry.getRouteNumberBank() != null) {
                if (!prmRegistry.getRouteNumberBank().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")) {
                    msgError = "Letters are not allowed in the route number";
                    return null;
                } else {
                    prmRegistry.setRouteNumberBank(prmRegistry.getRouteNumberBank().trim());
                }
            } else {
                msgError = "The route number is required";
                return null;
            }
            if (prmRegistry.getChequeVoidId() == null) {
                msgError = "The chequeVoidId is required";
                return null;
            }

        }
        return prmRegistry;
    }

    @Override
    public ResponseEntity<?> testPayment(doPaymentDTO prmPaymentInfo) {
        ResponsePayment respPayment;
        String userTransactionNumber = uniqueString();
        switch (prmPaymentInfo.getPaymethod()) {
            case "CREDIT-CARD":
                try {
                    ResponseJSON token = blackStoneService.getToken("190001",
                            prmPaymentInfo.getCreditcarnumber().replaceAll("-", ""),
                            prmPaymentInfo.getExpDateMonth() + prmPaymentInfo.getExpDateYear(),
                            prmPaymentInfo.getNameoncard(),
                            prmPaymentInfo.getSecuritycode(), null, userTransactionNumber);
                    /*
                     * respPayment =
                     * blackStoneService.paymentWithCreditCard(String.valueOf(formato.format(100)),
                     * "190001",
                     * prmPaymentInfo.getCreditcarnumber().replaceAll("-", ""),
                     * prmPaymentInfo.getExpDateMonth() + prmPaymentInfo.getExpDateYear(),
                     * prmPaymentInfo.getNameoncard(),
                     * prmPaymentInfo.getSecuritycode(), null, userTransactionNumber);
                     */
                    respPayment = blackStoneService.paymentWithToken(String.valueOf(formato.format(100)),
                            "190001",
                            token.getToken(),
                            prmPaymentInfo.getExpDateMonth() + prmPaymentInfo.getExpDateYear(),
                            prmPaymentInfo.getNameoncard(),
                            prmPaymentInfo.getSecuritycode(), null, userTransactionNumber);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
                }
                if (respPayment.getResponseCode() != 200) {
                    // emailService.notifyErrorRegister(objEmailBodyData);
                    HashMap<String, String> objError = new HashMap<String, String>();
                    objError.put("msg", "No se pudo registrar el pago con la tarjeta de credito");
                    return new ResponseEntity<HashMap<String, String>>(objError, HttpStatus.NOT_ACCEPTABLE);
                }
                System.out.println("PAGO EXITOSO ");
                String serviceReferenceNumber = respPayment.getServiceReferenceNumber();
                return new ResponseEntity<String>(serviceReferenceNumber, HttpStatus.OK);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPaymentHistor(LocalDate startDate, LocalDate endDate, String filter) {
        if (filter != null && filter.compareTo("") != 0) {
            filter.toUpperCase();
            if (filter.compareTo("CURRENT_MONTH") == 0) {
                startDate = LocalDate.now();
                // Obtener el primer dia del mes
                LocalDate firstDayOfMonth = startDate.with(TemporalAdjusters.firstDayOfMonth());
                startDate = firstDayOfMonth;
                // Obtener el último día del mes
                LocalDate lastDayOfMonth = startDate.with(TemporalAdjusters.lastDayOfMonth());
                endDate = lastDayOfMonth;
            }
            if (filter.compareTo("PREVIOUS_MONTH") == 0) {
                startDate = LocalDate.now().minusMonths(1);
                // Obtener el primer dia del mes
                LocalDate temporalDate = LocalDate.of(startDate.getYear(), startDate.getMonth(),
                        startDate.getDayOfMonth());
                LocalDate firstDayOfMonth = temporalDate.with(TemporalAdjusters.firstDayOfMonth());
                startDate = firstDayOfMonth;
                // Obtener el último día del mes
                LocalDate lastDayOfMonth = startDate.with(TemporalAdjusters.lastDayOfMonth());
                endDate = lastDayOfMonth;
            }
        } else {
            if (endDate == null) {
                endDate = startDate;
            }
        }
        List<Invoice> facturas = this.invoiceRepository.getPaymentReports(startDate, endDate);
        List<PaymentHistoryReport> report = new ArrayList<>();
        for (Invoice i : facturas) {
            PaymentHistoryReport phr = new PaymentHistoryReport();
            Business objBusiness = this.businessRepository.findById(i.getBusinessId()).orElse(null);
            if (objBusiness != null) {
                phr.setName(objBusiness.getUser().getName());
                phr.setBusinessName(objBusiness.getName());
                phr.setMerchantId(objBusiness.getMerchantId());

            }
            phr.setInvoiceId(i.getInvoiceNumber());
            phr.setDate(i.getDate());
            phr.setTime(i.getTime().withNano(0));
            phr.setPayMethod(i.getPaymentMethod());
            phr.setNumberTerminals(i.getTerminals());
            phr.setTotal(i.getTotalAmount());
            phr.setReference(i.getReferenceNumber());
            report.add(phr);
        }
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    /**
     * Confirms or rejects a payment based on the given invoice number and payment
     * information.
     *
     * @param invoiceNumber  the invoice number to confirm or reject
     * @param prmPaymentInfo the payment information containing the confirmation
     *                       status and observation
     * @return a ResponseEntity containing the updated invoice information or an
     * error message
     * @throws EntidadNoExisteException if the invoice with the given invoice number
     *                                  does not exist in the database
     */
    @Override
    @Transactional
    public ResponseEntity<?> confirmOrRejectPaymnt(Long invoiceNumber, ConfirmPaymentDTO prmPaymentInfo) {
        Invoice objInvoice = this.invoiceRepository.findById(invoiceNumber).orElse(null);
        if (objInvoice == null) {
            throw new EntidadNoExisteException("La Factura #" + invoiceNumber + " no existen en la base de datos");
        }
        objInvoice.setInProcess(false);
        if (prmPaymentInfo.getObservation() != null) {
            if (objInvoice.getReferenceNumber() != null) {
                objInvoice.setReferenceNumber(objInvoice.getReferenceNumber() + " " + prmPaymentInfo.getObservation());
            } else {
                objInvoice.setReferenceNumber(prmPaymentInfo.getObservation());
            }
        }

        String terminalsIds = objInvoice.getTerminalIds();
        if (prmPaymentInfo.getConfirm() || !isProd()) {

            Business objBusiness = this.businessRepository.findById(objInvoice.getBusinessId()).orElse(null);
            if (objBusiness != null) {
                objBusiness.setLastPayment(Instant.now());
                this.businessRepository.save(objBusiness);
            }
            if (terminalsIds != null) {
                String[] terminalIds = terminalsIds.split(",");
                for (String terminalId : terminalIds) {
                    Terminal objTerminal = this.serviceDBTerminal.findById(terminalId).orElse(null);
                    if (objTerminal != null) {
                        objTerminal.setLastPayment(Instant.now());
                        objTerminal.setEnable(true);
                        objTerminal.setPayment(true);
                        // se incrementa la fecha de expiración del terminal de acuerdo a la duración
                        // del servicio
                        if (objTerminal.getExpirationDate() != null && objTerminal.isEnable()
                                && objTerminal.isPayment()) {
                            if (!objTerminal.getExpirationDate().isBefore(Instant.now())) {
                                objTerminal.setExpirationDate(objTerminal.getExpirationDate()
                                        .plus(Duration.ofDays(objTerminal.getService().getDuration())));
                            } else {
                                objTerminal.setExpirationDate(
                                        Instant.now().plus(Duration.ofDays(objTerminal.getService().getDuration())));
                            }
                        } else {
                            objTerminal.setExpirationDate(
                                    Instant.now().plus(Duration.ofDays(objTerminal.getService().getDuration())));
                        }
                        if (objTerminal.isPrincipal()) {
                            objTerminal.getBusiness().setPriorNotification(null);
                            objTerminal.getBusiness().setAfterNotification(null);
                            objTerminal.getBusiness().setLastDayNotification(null);
                        }
                        this.serviceDBTerminal.save(objTerminal);
                    }
                }
            }
        } else {
            objInvoice.setRejected(true);
            if (objInvoice.getReferenceNumber() != null) {
                objInvoice.setReferenceNumber(objInvoice.getReferenceNumber() + "-" + "PAGO RECHAZADO");
            } else {
                objInvoice.setReferenceNumber("PAGO RECHAZADO");
            }
        }
        if (objInvoice.getPaymentDescription() != null && objInvoice.getPaymentDescription().contains("PAGO CANCELADO POR EL USUARIO")) {
            objInvoice.setReferenceNumber("PAGO CANCELADO POR EL USUARIO");
        }
        objInvoice = this.invoiceRepository.save(objInvoice);
        InvoiceDTO objInvoiceDTO = this.mapper.map(objInvoice, InvoiceDTO.class);
        return new ResponseEntity<InvoiceDTO>(objInvoiceDTO, HttpStatus.OK);
    }

    /**
     * Saves a payment method by creating a token for a credit card payment using
     * the BlackStone API.
     *
     * @param prmRegistry the payment data to be saved
     * @return the response entity with the result of the save operation
     */
    @Override
    @Transactional
    public ResponseEntity<?> createToken(PaymentDataDTO prmRegistry) {
        prmRegistry = validateDataForCreateToken(prmRegistry);
        if (prmRegistry == null) {
            HashMap<String, String> map = new HashMap<>();
            map.put("message", msgError);
            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
        }
        Business objBusiness = this.businessRepository.findById(prmRegistry.getBusinessId()).orElse(null);
        if (objBusiness == null) {
            throw new EntidadNoExisteException(
                    "El Business con businessId " + prmRegistry.getBusinessId() + " no existen en la base de datos");
        }
        String userTransactionNumber = uniqueString();
        try {
            ResponseJSON objToken = blackStoneService.getToken(prmRegistry.getZipCode(),
                    prmRegistry.getCreditcarnumber().replaceAll("-", ""),
                    prmRegistry.getExpDateMonth() + prmRegistry.getExpDateYear(),
                    prmRegistry.getNameOnCard(),
                    prmRegistry.getSecuritycode(), null, userTransactionNumber);
            if (objToken.getResponseCode() == 200) {
                PaymentData objPaymentData = new PaymentData();
                objPaymentData.setToken(objToken.getToken());
                objPaymentData.setExpDate(prmRegistry.getExpDateMonth() + prmRegistry.getExpDateYear());
                objPaymentData.setNameOnCard(prmRegistry.getNameOnCard().toUpperCase().trim());
                objPaymentData.setCvn(prmRegistry.getSecuritycode());
                objPaymentData.setLast4Digits(prmRegistry.getCreditcarnumber().replaceAll("-", "")
                        .substring(prmRegistry.getCreditcarnumber().length() - 4));
                objBusiness.setPaymentData(objPaymentData);
                objPaymentData.setUsingAutomaticPayment(prmRegistry.isUsingAutomaticPayment());
                this.businessRepository.save(objBusiness);
                HashMap<String, String> map = new HashMap<>();
                map.put("message", "Token creado exitosamente");
                return new ResponseEntity<>(map, HttpStatus.CREATED);
            } else {
                HashMap<String, String> map = new HashMap<>();
                map.put("message", "No se pudo crear el token: " + objToken.getMsg());
                return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            System.out.println("com.retailmanager.rmpaydashboard.services.services.InvoiceServices.InvoiceServices.createToken(): " + e.getMessage());
            HashMap<String, String> map = new HashMap<>();
            map.put("message", e.getMessage());
            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
        }
    }

    private PaymentDataDTO validateDataForCreateToken(PaymentDataDTO prmRegistry) {
        if (prmRegistry.getCreditcarnumber() != null) {
            if (!prmRegistry.getCreditcarnumber().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")) {
                msgError = "Letters are not allowed in the credit card number";
                return null;
            }
        } else {
            msgError = "The credit card number is required";
            return null;
        }
        if (prmRegistry.getNameOnCard() != null) {
            prmRegistry.setNameOnCard(prmRegistry.getNameOnCard().toUpperCase().trim());
        } else {
            msgError = "The name on card is required";
            return null;
        }
        if (prmRegistry.getSecuritycode() != null) {
            if (!prmRegistry.getSecuritycode().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")) {
                msgError = "Letters are not allowed in the security code";
                return null;
            }
        } else {
            msgError = "The security code is required";
            return null;
        }
        if (prmRegistry.getExpDateMonth() != null) {
            if (!prmRegistry.getExpDateMonth().matches("[+-]?\\d*(\\.\\d+)?")) {
                msgError = "Letters are not allowed in the expiration date";
                return null;
            } else {
                if (Integer.parseInt(prmRegistry.getExpDateMonth()) > 12) {
                    msgError = "The expiration date month must be less than or equal to 12";
                    return null;
                }
            }
        } else {
            msgError = "The expiration date month is required";
            return null;
        }
        if (prmRegistry.getExpDateYear() != null) {
            if (!prmRegistry.getExpDateYear().matches("[+-]?\\d*(\\.\\d+)?")) {
                msgError = "Letters are not allowed in the expiration date";
                return null;
            } else {
                prmRegistry.setExpDateYear(prmRegistry.getExpDateYear().trim());
                if (prmRegistry.getExpDateYear().length() != 2) {
                    msgError = "The expiration date year must be 2 digits";
                    return null;
                }
            }
        } else {
            msgError = "The expiration date year is required";
            return null;
        }

        return prmRegistry;

    }

    /**
     * Verifies if a payment method exists for a given business ID.
     *
     * @param prmBusinessId the ID of the business
     * @return a ResponseEntity containing a boolean value indicating if the payment
     * method exists
     * or not, wrapped in a ResponseEntity with HTTP status code 200
     * @throws EntidadNoExisteException if the business with the given ID does not
     *                                  exist in the database
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> existToken(Long prmBusinessId) {
        Business objBusiness = this.businessRepository.findById(prmBusinessId).orElse(null);
        if (objBusiness == null) {
            throw new EntidadNoExisteException(
                    "El Business con businessId " + prmBusinessId + " no existen en la base de datos");
        }
        if (objBusiness.getPaymentData() != null) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }

    /**
     * Retrieves the payment method for a given business ID.
     *
     * @param prmBusinessId the ID of the business
     * @return a ResponseEntity containing the payment method data, or an exception
     * if the business does not exist
     * @throws EntidadNoExisteException if the business with the given ID does not
     *                                  exist in the database
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getToken(Long prmBusinessId) {
        Business objBusiness = this.businessRepository.findById(prmBusinessId).orElse(null);
        if (objBusiness == null) {
            throw new EntidadNoExisteException(
                    "El Business con businessId " + prmBusinessId + " no existen en la base de datos");
        }
        if (objBusiness.getPaymentData() == null) {
            throw new EntidadNoExisteException(
                    "El Business con businessId " + prmBusinessId + " no tiene un metodo de pago configurado");
        }

        PaymentDataDTO objPaymentData = this.mapper.map(objBusiness.getPaymentData(), PaymentDataDTO.class);
        return new ResponseEntity<>(objPaymentData, HttpStatus.OK);
    }

    /**
     * Deletes the payment data for a given business ID.
     *
     * @param prmBusinessId the ID of the business
     * @return a ResponseEntity containing the result of the deletion operation, or
     * an exception if the business does not exist
     * @throws EntidadNoExisteException if the business with the given ID does not
     *                                  exist in the database
     */
    @Override
    @Transactional
    public ResponseEntity<?> deleteToken(Long prmBusinessId) {
        Business objBusiness = this.businessRepository.findById(prmBusinessId).orElse(null);
        if (objBusiness == null) {
            throw new EntidadNoExisteException(
                    "El Business con businessId " + prmBusinessId + " no existen en la base de datos");
        }
        try {
            if (objBusiness.getPaymentData() != null) {
                int paymentDataId = objBusiness.getPaymentData().getPaymentId();
                objBusiness.setPaymentData(null);

                this.businessRepository.save(objBusiness);
                paymentDataRepository.deleteById(paymentDataId);
            }

        } catch (Exception e) {
            HashMap<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    /**
     * Checks the status of an ATH Movil payment for a given invoice ID.
     *
     * @param invoiceId the ID of the invoice to check the payment status for
     * @return a ResponseEntity containing the FindPaymentResponse if successful,
     * or an error message if an exception occurs
     * @throws EntidadNoExisteException if the invoice with the given ID does not
     *                                  exist in the database
     */

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> checkStatusATHM(Long invoiceId) {
        Invoice invoice = this.invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice == null) {
            throw new EntidadNoExisteException("La Factura con invoiceId " + invoiceId + " no existen en la base de datos");
        }
        if (invoice.isInProcess() == false) {
            FindPaymentResponse athmCancelPaymentResponse = ConverterJson.convertStr2RespDetTransactionATHM(invoice.getATHMPaymentDetails());
            return new ResponseEntity<>(athmCancelPaymentResponse, HttpStatus.OK);
        }
        ATHMPaymentResponse pr = ConverterJson.convertStr2RespPaymentATHM(invoice.getReferenceNumber());
        FindPaymentReqData request = new FindPaymentReqData();
        request.setEcommerceId(pr.getData().getEcommerceId());


        try {
            FindPaymentResponse resposne = athMovilService.findPayment(request);

            if (!isProd()){
                resposne.getData().setEcommerceStatus("CONFIRM");
                cancelTransactionATHM(invoiceId);
            }

            return new ResponseEntity<>(resposne, HttpStatus.OK);
        } catch (ConsumeAPIException ex) {
            System.err.println("Error en el consumo de ATHMovil: CodigoHttp " + ex.getHttpStatusCode() + " \n Mensje: " + ex.getMessage());
            HashMap<String, String> map = new HashMap<>();
            map.put("msg", "Error en el consumo de ATHMovil: CodigoHttp " + ex.getHttpStatusCode() + " \n Mensje: " + ex.getMessage());

            return new ResponseEntity<>(map, HttpStatus.BAD_GATEWAY);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> confirmTransactionATHM(Long invoiceId) {

        if (!isProd()){
            ConfirmPaymentDTO confirmPaymentDTO = new ConfirmPaymentDTO();
            confirmPaymentDTO.setConfirm(true);
            confirmPaymentDTO.setObservation("");
            this.confirmOrRejectPaymnt(invoiceId, confirmPaymentDTO);
            return new ResponseEntity<>(confirmPaymentDTO, HttpStatus.OK);
        }
        Invoice invoice = this.serviceDBInvoice.findById(invoiceId).orElse(null);
        if (invoice == null) {
            throw new EntidadNoExisteException("La Factura con invoiceId " + invoiceId + " no existen en la base de datos");
        }
        if (invoice.isInProcess() == false) {
            HashMap<String, String> json = new HashMap<>();
            json.put("msg", "El pago ya ha sido confirmado o cancelado");
            return new ResponseEntity<>(json, HttpStatus.BAD_REQUEST);
        }
        ATHMPaymentResponse pr = ConverterJson.convertStr2RespPaymentATHM(invoice.getReferenceNumber());
        try {
            FindPaymentResponse resposne = athMovilService.confirmAuthorization(pr.getData().getAuth_token());
            if (resposne != null) {
                if ("success".equals(resposne.getStatus()) && "COMPLETED".equals(resposne.getData().getEcommerceStatus())) {
                    EmailBodyData paymentInfoForEmail = ConverterJson.convertStr2EmailBodyData(invoice.getATHMPaymentDetails());
                    System.out.println("ATHMPaymentResponse.token: " + pr.getData().getAuth_token());
                    invoice.setATHMPaymentDetails(gson.toJson(resposne));
                    invoice.setInProcess(false);
                    invoice.setReferenceNumber(resposne.getData().getReferenceNumber());
                    invoice = this.serviceDBInvoice.save(invoice);

                    paymentInfoForEmail.setReferenceNumber(resposne.getData().getReferenceNumber());
                    //Ejecuta la confirmación del pago,se reutiliza la logica ya implementada
                    ConfirmPaymentDTO confirmPaymentDTO = new ConfirmPaymentDTO();
                    confirmPaymentDTO.setConfirm(true);
                    confirmPaymentDTO.setObservation("");
                    this.confirmOrRejectPaymnt(invoiceId, confirmPaymentDTO);
                    paymentInfoForEmail.setInvoiceNumber(invoice.getInvoiceNumber());
                    this.emailService.notifyPaymentATHMovil(paymentInfoForEmail);
                    return new ResponseEntity<>(invoice, HttpStatus.OK);
                }
            }
            invoice.setInProcess(false);
            if (resposne == null)
                invoice.setReferenceNumber("PAGO NO PROCESADO - ATHM NO RESPONDIO");
            else
                invoice.setReferenceNumber("PAGO NO PROCESADO: " + resposne.getStatus() + " " + resposne.getMessage() + "-" + resposne.getErrorcode());
            invoice = this.serviceDBInvoice.save(invoice);
            return new ResponseEntity<>(invoice, HttpStatus.REQUEST_TIMEOUT);
        } catch (ConsumeAPIException ex) {

            System.err.println("Error en el consumo de ATHMovil EN com.retailmanager.rmpaydashboard.services.services.InvoiceServices.confirmTransactionATHM: CodigoHttp " + ex.getHttpStatusCode() + " \n Mensje: " + ex.getMessage());
            HashMap<String, String> json = new HashMap<>();
            json.put("msg", "Error en el consumo de ATHMovil: CodigoHttp " + ex.getHttpStatusCode() + " \n Mensje: " + ex.getMessage());

            return new ResponseEntity<>(json, HttpStatus.BAD_GATEWAY);
        }
    }


    /**
     * Cancela un pago en la API de ATH Móvil.
     *
     * @param invoiceId El identificador de la factura a cancelar.
     * @return La respuesta de la API con el resultado de la cancelación.
     * @throws EntidadNoExisteException Si la factura con el identificador
     *                                  especificado no existe en la base de datos.
     * @throws ConsumeAPIException      Si ocurre un error al consumir la API.
     */
    @Override
    @Transactional
    public ResponseEntity<?> cancelTransactionATHM(Long invoiceId) {

        Invoice invoice = this.serviceDBInvoice.findById(invoiceId).orElse(null);
        if (invoice == null) {
            throw new EntidadNoExisteException("La Factura con invoiceId " + invoiceId + " no existen en la base de datos");
        }
        if (invoice.getReferenceNumber() != null && invoice.getReferenceNumber().contains("PAGO CANCELADO POR EL USUARIO") && invoice.isInProcess() == false) {
            HashMap<String, String> json = new HashMap<>();
            json.put("msg", "El pago ya ha sido cancelado");
            return new ResponseEntity<>(json, HttpStatus.BAD_REQUEST);
        }
        try {
            ATHMPaymentResponse pr = ConverterJson.convertStr2RespPaymentATHM(invoice.getReferenceNumber());
            FindPaymentReqData request = new FindPaymentReqData();
            request.setEcommerceId(pr.getData().getEcommerceId());
            FindPaymentResponse rsp2 = athMovilService.findPayment(request);
            if (rsp2 != null && rsp2.getData().getEcommerceStatus().equals("CANCEL")) {
                EmailBodyData paymentInfoForEmail = ConverterJson.convertStr2EmailBodyData(invoice.getATHMPaymentDetails());

                invoice.setATHMPaymentDetails(gson.toJson(rsp2));
                invoice.setInProcess(false);
                invoice.setReferenceNumber("PAGO CANCELADO POR EL USUARIO");
                invoice = this.serviceDBInvoice.save(invoice);
                paymentInfoForEmail.setReferenceNumber(invoice.getReferenceNumber());
                emailService.notifyPaymentATHMovil(paymentInfoForEmail);
                //Ejecuta la confirmación del pago,se reutiliza la logica ya implementada
                ConfirmPaymentDTO confirmPaymentDTO = new ConfirmPaymentDTO();
                confirmPaymentDTO.setConfirm(false);
                confirmPaymentDTO.setObservation("");
                this.confirmOrRejectPaymnt(invoiceId, confirmPaymentDTO);
                return new ResponseEntity<>(invoice, HttpStatus.OK);
            } else {
                ATHMCancelPaymentResponse resposne = athMovilService.cancel(request);
                rsp2 = athMovilService.findPayment(request);
                if (resposne != null) {
                    if ("success".equals(resposne.getStatus())) {
                        EmailBodyData paymentInfoForEmail = ConverterJson.convertStr2EmailBodyData(invoice.getATHMPaymentDetails());

                        invoice.setATHMPaymentDetails(gson.toJson(rsp2));
                        invoice.setInProcess(false);
                        invoice.setReferenceNumber("PAGO CANCELADO POR EL USUARIO");
                        invoice = this.serviceDBInvoice.save(invoice);

                        paymentInfoForEmail.setReferenceNumber(invoice.getReferenceNumber());
                        emailService.notifyPaymentATHMovil(paymentInfoForEmail);
                        //Ejecuta la confirmación del pago,se reutiliza la logica ya implementada
                        ConfirmPaymentDTO confirmPaymentDTO = new ConfirmPaymentDTO();
                        confirmPaymentDTO.setConfirm(false);
                        confirmPaymentDTO.setObservation("");
                        this.confirmOrRejectPaymnt(invoiceId, confirmPaymentDTO);
                        return new ResponseEntity<>(invoice, HttpStatus.OK);
                    }
                }
            }
            HashMap<String, String> json = new HashMap<>();
            json.put("msg", "El pago no pudo ser cancelado: " + (rsp2 != null ? rsp2.getMessage() : "No se pudo obtener el estado del pago"));
            System.err.println("Error en la cancelación del Pago: " + json.get("msg"));
            return new ResponseEntity<>(json, HttpStatus.BAD_GATEWAY);
        } catch (ConsumeAPIException ex) {
            System.err.println("Error en el consumo de ATHMovil: CodigoHttp " + ex.getHttpStatusCode() + " \n Mensje: " + ex.getMessage());
            JsonObject json = new JsonObject();
            json.addProperty("msg", "Error en el consumo de ATHMovil: CodigoHttp " + ex.getHttpStatusCode() + " \n Mensje: " + ex.getMessage());

            return new ResponseEntity<>(json, HttpStatus.BAD_GATEWAY);
        } catch (Exception ex) {
            System.err.println("Error en la cancelación del Pago  Mensaje: " + ex.getMessage());
            JsonObject json = new JsonObject();
            json.addProperty("msg", "Error en la cancelación del Pago  Mensaje: " + ex.getMessage());

            return new ResponseEntity<>(json, HttpStatus.BAD_GATEWAY);
        }
    }

}
