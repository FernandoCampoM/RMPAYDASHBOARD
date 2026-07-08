package com.retailmanager.rmpaydashboard.services.services.TerminalService;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConsumeAPIException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.DataInconsistencyException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.TerminalDisabled;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Invoice;
import com.retailmanager.rmpaydashboard.models.PaymentData;
import com.retailmanager.rmpaydashboard.models.Service;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.InvoiceRepository;
import com.retailmanager.rmpaydashboard.repositories.ServiceRepository;
import com.retailmanager.rmpaydashboard.repositories.ShiftReporsitory;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.services.DTO.BuyTerminalDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalsDoPaymentDTO;
import com.retailmanager.rmpaydashboard.services.services.EmailService.EmailBodyData;
import com.retailmanager.rmpaydashboard.services.services.EmailService.IEmailService;
import com.retailmanager.rmpaydashboard.services.services.Payment.IBlackStoneService;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponseJSON;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponsePayment;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@org.springframework.stereotype.Service
public class TerminalService implements ITerminalService {
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private TerminalRepository serviceDBTerminal;
    @Autowired
    private ServiceRepository serviceDBService;
    @Autowired
    private IBlackStoneService blackStoneService;
    @Autowired
    private IEmailService emailService;
    @Autowired
    private InvoiceRepository serviceDBInvoice;
    @Autowired
    private ShiftReporsitory shiftReporsitory;

    DecimalFormat formato = new DecimalFormat("#.##");
    String msgError = "";

    /**
     * GUARDA EL NOMBRE Y SERIAL DEL TERMINAL EN LA BASE DE DATOS SÍ EL TERMINAL YA ESTÁ CREADO
     * Saves a TerminalDTO object in the database and returns a response entity.
     *
     * @param prmTerminal the TerminalDTO object to be saved
     * @return a response entity containing the saved TerminalDTO or an error
     * message
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(TerminalDTO prmTerminal) {

        String terminalId = prmTerminal.getTerminalId();
        Terminal terminal = null;
        if (terminalId != null) {
            terminal = this.serviceDBTerminal.findById(terminalId).orElse(null);
            if (terminal == null) {
                EntidadNoExisteException objExeption = new EntidadNoExisteException(
                        "El terminal con terminalId " + prmTerminal.getTerminalId() + " no existe en la Base de datos");
                throw objExeption;
            }
        } else {
            throw new EntidadNoExisteException("El terminalId no puede ser nulo");
        }

        if (!terminal.isEnable()) {
            throw new TerminalDisabled("El terminal con terminalId " + prmTerminal.getTerminalId() + " se encuentra deshabilitado");
        }
        if (terminal.getExpirationDate() != null && terminal.getExpirationDate().isBefore(Instant.now())) {
            throw new TerminalDisabled("El terminal con terminalId " + prmTerminal.getTerminalId() + " ha expirado");
        }
        if (terminal.getBusiness().getBusinessId() != prmTerminal.getBusinesId()) {
            throw new TerminalDisabled("El terminal con terminalId " + prmTerminal.getTerminalId() + " no pertenece aal negocio con businessId " + prmTerminal.getBusinesId());
        }


        ResponseEntity<?> rta;
        Long businessId = prmTerminal.getBusinesId();
        if (businessId != null) {
            Optional<Business> existBusiness = this.serviceDBBusiness.findById(businessId);
            if (!existBusiness.isPresent()) {
                EntidadNoExisteException objExeption = new EntidadNoExisteException(
                        "El business con businessId " + businessId + " no existe en la Base de datos");
                throw objExeption;
            } else {
                terminal.setName(prmTerminal.getName());
                terminal.setSerial(prmTerminal.getSerial());
                terminal.setEnable(prmTerminal.getEnable());
            }
        }
        terminal = this.serviceDBTerminal.save(terminal);

        TerminalDTO terminalDTO = this.mapper.map(terminal, TerminalDTO.class);
        if (terminalDTO != null) {

            rta = new ResponseEntity<TerminalDTO>(terminalDTO, HttpStatus.OK);
        } else {
            rta = new ResponseEntity<String>("Error al crear el Terminal", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }

    /**
     * Updates a terminal in the database.
     *
     * @param terminalId  the ID of the terminal to be updated
     * @param prmTerminal the updated terminal details
     * @return response entity with the updated terminal details
     */
    @Override
    @Transactional
    public ResponseEntity<?> update(String terminalId, TerminalDTO prmTerminal) {
        Terminal objTerminal = null;
        ResponseEntity<?> rta = null;
        if (terminalId != null) {
            Optional<Terminal> exist = this.serviceDBTerminal.findById(terminalId);
            if (!exist.isPresent()) {
                EntidadNoExisteException objExeption = new EntidadNoExisteException(
                        "El terminal con terminalId " + prmTerminal.getTerminalId() + " No existe en la Base de datos");
                throw objExeption;
            }
            objTerminal = exist.get();
            if (prmTerminal.getSerial() != null && objTerminal.getSerial() != null
                    && objTerminal.getSerial().compareTo(prmTerminal.getSerial()) != 0) {
                if (prmTerminal.getSerial() != null) {
                    Optional<Terminal> existBySerial = this.serviceDBTerminal.findOneBySerial(prmTerminal.getSerial());
                    if (existBySerial.isPresent()) {
                        EntidadYaExisteException objExeption = new EntidadYaExisteException(
                                "El terminal con serial " + prmTerminal.getSerial() + " ya existe en la Base de datos");
                        throw objExeption;
                    }
                }
            }
            objTerminal.setSerial(prmTerminal.getSerial());
            objTerminal.setEnable(prmTerminal.getEnable());
            objTerminal.setName(prmTerminal.getName());
            if (objTerminal != null) {
                objTerminal = this.serviceDBTerminal.save(objTerminal);
            }
            TerminalDTO terminalDTO = this.mapper.map(objTerminal, TerminalDTO.class);
            if (terminalDTO != null) {

                rta = new ResponseEntity<TerminalDTO>(terminalDTO, HttpStatus.OK);
            } else {
                rta = new ResponseEntity<String>("Error al actualizar el Terminal", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return rta;
    }

    /**
     * Deletes a terminal by ID.
     *
     * @param terminalId the ID of the terminal to delete
     * @return true if the terminal was deleted, false otherwise
     */
    @Override
    @Transactional
    public boolean delete(String terminalId) {
        boolean bandera = false;

        if (terminalId != null) {
            Optional<Terminal> optional = this.serviceDBTerminal.findById(terminalId);
            if (optional.isPresent()) {
                Terminal objTerminal = optional.get();

                int contShift = shiftReporsitory.countShiftByBusinessIdAndSerialAndOpenShift(
                        objTerminal.getBusiness().getBusinessId(),
                        objTerminal.getSerial(),
                        true);

                if (contShift > 0) {
                    throw new DataInconsistencyException("No se puede liberal el terminal, tiene turnos abiertos");
                }

                objTerminal.setSerial(null);
                this.serviceDBTerminal.save(objTerminal);
                bandera = true;
            }
        }
        return bandera;
    }

    /**
     * Find a terminal by its ID.
     *
     * @param terminalId the ID of the terminal to find
     * @return a response entity with the terminal DTO if found, or an exception if
     * not found
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(String terminalId) {
        if (terminalId != null) {
            Optional<Terminal> optional = this.serviceDBTerminal.findById(terminalId);
            if (optional.isPresent()) {
                TerminalDTO objTerminalDTO = this.mapper.map(optional.get(), TerminalDTO.class);

                return new ResponseEntity<TerminalDTO>(objTerminalDTO, HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException(
                "El Terminal con terminalId " + terminalId + " no existe en la Base de datos");
        throw objExeption;
    }

    /**
     * Finds a record by its serial number and returns a ResponseEntity with the
     * corresponding TerminalDTO if found. Throws EntidadNoExisteException if the
     * record does not exist.
     *
     * @param serial the serial number of the record to be found
     * @return a ResponseEntity containing the TerminalDTO if found
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findBySerial(String serial) {
        if (serial != null) {
            Optional<Terminal> optional = this.serviceDBTerminal.findOneBySerial(serial);
            if (optional.isPresent()) {
                TerminalDTO objTerminalDTO = this.mapper.map(optional.get(), TerminalDTO.class);

                return new ResponseEntity<TerminalDTO>(objTerminalDTO, HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException(
                "El Terminal con serial " + serial + " no existe en la Base de datos");
        throw objExeption;
    }

    /**
     * Update the enable status of a terminal.
     *
     * @param terminalId the ID of the terminal to update
     * @param enable     the new enable status
     * @return the response entity indicating the success of the update
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateEnable(String terminalId, boolean enable) {

        if (terminalId != null) {
            Optional<Terminal> optional = this.serviceDBTerminal.findById(terminalId);
            if (optional.isPresent()) {
                if (optional.get().getExpirationDate() == null
                        || (enable && optional.get().getExpirationDate().isBefore(Instant.now()) == true)) {
                    return new ResponseEntity<Boolean>(false, HttpStatus.PAYMENT_REQUIRED);
                }
                this.serviceDBTerminal.updateEnable(terminalId, enable);
                return new ResponseEntity<Boolean>(true, HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException(
                "El Terminal con terminalId " + terminalId + " no existe en la Base de datos");
        throw objExeption;
    }

    @Override
    @Transactional
    public ResponseEntity<?> buyTerminal(BuyTerminalDTO prmTerminal) {

        Double amount = 0.0;
        Double stateTax = 0.0;
        Double serviceValue = 0.0;
        ResponsePayment respPayment;
        String serviceReferenceNumber = null;
        prmTerminal = validateData(prmTerminal);
        if (prmTerminal == null) {
            return new ResponseEntity<String>(msgError, HttpStatus.BAD_REQUEST);
        }
        Service objService = null;
        ResponseEntity<?> rta;
        Business objBusiness = null;
        Terminal objTerminal = this.mapper.map(prmTerminal, Terminal.class);
        objTerminal.setTerminalId(getTerminalId());
        objTerminal.setRegisterDate(Instant.now());
        Long businessId = prmTerminal.getBusinesId();
        EmailBodyData objEmailBodyData = mapper.map(prmTerminal, EmailBodyData.class);
        objEmailBodyData.setAdditionalTerminals(1);
        objEmailBodyData.setTerminalsDoPayment(new ArrayList<>());
        if (businessId != null) {
            Optional<Business> existBusiness = this.serviceDBBusiness.findById(businessId);
            if (!existBusiness.isPresent()) {
                EntidadNoExisteException objExeption = new EntidadNoExisteException(
                        "El business con businessId " + businessId + " no existe en la Base de datos");
                throw objExeption;
            } else {
                String userTransactionNumber = uniqueString();
                objService = this.serviceDBService.findById(prmTerminal.getIdService()).orElse(null);

                amount = objService.getServiceValue();
                objBusiness = existBusiness.get();
                int activesTerminals = this.serviceDBTerminal.countActiveTerminals(businessId);
                objBusiness.setAdditionalTerminals(activesTerminals);
                objEmailBodyData.setBusinessName(objBusiness.getName());
                objEmailBodyData.setEmail(objBusiness.getUser().getEmail());
                objEmailBodyData.setMerchantId(objBusiness.getMerchantId());
                objEmailBodyData.setName(objBusiness.getUser().getName());
                objEmailBodyData.setPhone(objBusiness.getUser().getPhone());

                // Calculo del valor de los terminales
                if (objBusiness.getAdditionalTerminals() > 1 && objBusiness.getAdditionalTerminals() <= 5) {
                    amount = objService.getTerminals2to5();
                    objEmailBodyData.setAdditionalTerminalsValue(objService.getTerminals2to5());
                } else if (objBusiness.getAdditionalTerminals() > 5 && objBusiness.getAdditionalTerminals() < 10) {
                    amount = objService.getTerminals6to9();
                    objEmailBodyData.setAdditionalTerminalsValue(objService.getTerminals6to9());
                } else {
                    amount = objService.getTerminals10();
                    objEmailBodyData.setAdditionalTerminalsValue(objService.getTerminals10());
                }
                serviceValue = amount;
                objEmailBodyData.setDiscount(0.0);
                if (objBusiness.getDiscount() != 0.0) {
                    objEmailBodyData.setReferenceNumber(
                            "DESCUENTO APLICADO:$" + String.valueOf(formato.format(objBusiness.getDiscount())));
                    objEmailBodyData.setDiscount(objBusiness.getDiscount());

                    if (objBusiness.getDiscount() < amount) {
                        amount = amount - objBusiness.getDiscount();
                        objBusiness.setDiscount(0.0);
                    } else {
                        prmTerminal.setPaymethod("PAID-WITH-DISCOUNT");
                        objBusiness.setDiscount(objBusiness.getDiscount() - amount);
                        amount = 0.0;
                    }
                }
                if (objBusiness.getAdditionalTerminals() == 0) {
                    objTerminal.setPrincipal(true);
                    objBusiness.setServiceId(objService.getServiceId());
                } else {
                    objTerminal.setPrincipal(false);
                }
                stateTax = amount * 0.04;
                objEmailBodyData.setStateTax(stateTax);
                objEmailBodyData.setSubTotal(amount);
                amount = amount + stateTax;
                objEmailBodyData.setAmount(amount);
                objEmailBodyData.setServiceDescription(objService.getServiceDescription());
                objEmailBodyData.setServiceValue(formato.format(objService.getServiceValue()));
                objEmailBodyData.setBuyTerminal(true);
                objTerminal.setService(objService);
                objTerminal.setBusiness(existBusiness.get());
                switch (prmTerminal.getPaymethod()) {
                    case "CREDIT-CARD":
                        try {
                            respPayment = blackStoneService.paymentWithCreditCard(
                                    String.valueOf(formato.format(amount)),
                                    objBusiness.getAddress().getZipcode(),
                                    prmTerminal.getCreditcarnumber().replaceAll("-", ""),
                                    prmTerminal.getExpDateMonth() + prmTerminal.getExpDateYear(),
                                    prmTerminal.getNameoncard(),
                                    prmTerminal.getSecuritycode(), null, userTransactionNumber);
                        } catch (ConsumeAPIException ex) {
                            System.err.println("Error en el consumo de BlackStone: CodigoHttp " + ex.getHttpStatusCode()
                                    + " \n Mensje: " + ex.getMessage());
                            HashMap<String, String> map = new HashMap<>();
                            map.put("msg", "Por favor comuniquese con el administrador de la página.");
                            return new ResponseEntity<HashMap<String, String>>(map, HttpStatus.BAD_REQUEST);
                        }
                        System.out.println("respPayment: " + respPayment.getResponseCode());
                        if (respPayment.getResponseCode() != 200) {
                            emailService.notifyErrorRegister(objEmailBodyData);
                            HashMap<String, String> objError = new HashMap<String, String>();
                            objError.put("msg", "No se pudo registrar el pago con la tarjeta de credito");
                            return new ResponseEntity<HashMap<String, String>>(objError, HttpStatus.NOT_ACCEPTABLE);
                        }
                        if (prmTerminal.isAutomaticPayments()) {
                            try {
                                ResponseJSON objToken = blackStoneService.getToken(objBusiness.getAddress().getZipcode(),
                                        prmTerminal.getCreditcarnumber().replaceAll("-", ""),
                                        prmTerminal.getExpDateMonth() + prmTerminal.getExpDateYear(),
                                        prmTerminal.getNameoncard(),
                                        prmTerminal.getSecuritycode(), null, userTransactionNumber);
                                if (objToken.getResponseCode() == 200) {
                                    PaymentData objPaymentData = new PaymentData();
                                    objPaymentData.setToken(objToken.getToken());
                                    objPaymentData.setExpDate(prmTerminal.getExpDateMonth() + prmTerminal.getExpDateYear());
                                    objPaymentData.setNameOnCard(prmTerminal.getNameoncard());
                                    objPaymentData.setCvn(prmTerminal.getSecuritycode());
                                    objPaymentData.setLast4Digits(prmTerminal.getCreditcarnumber().replaceAll("-", "").substring(prmTerminal.getCreditcarnumber().length() - 4));
                                    objBusiness.setPaymentData(objPaymentData);
                                }

                            } catch (Exception e) {
                                System.out.println("Error: No se pudo obtener el token para guardar el token de pago automatico: " + e.getMessage());
                            }

                        }
                        existBusiness.get().setLastPayment(Instant.now());
                        this.serviceDBBusiness.save(existBusiness.get());
                        serviceReferenceNumber = respPayment.getServiceReferenceNumber();
                        objEmailBodyData.setReferenceNumber(serviceReferenceNumber);
                        break;
                }

            }
        }
        objTerminal.setAutomaticPayments(prmTerminal.isAutomaticPayments());
        objTerminal.setEnable(true);
        objTerminal.setExpirationDate(Instant.now().plus(Duration.ofDays(objService.getDuration())));
        objTerminal.setSerial(null);
        objTerminal.setName(objTerminal.getTerminalId().toString());
        Invoice objInvoice = new Invoice();
        objInvoice.setTotalAmount(amount);
        objInvoice.setSubTotal(objEmailBodyData.getSubTotal());
        objInvoice.setStateTax(stateTax);
        TerminalsDoPaymentDTO objTerDoPay = new TerminalsDoPaymentDTO();
        switch (prmTerminal.getPaymethod()) {
            case "CREDIT-CARD":
                objTerminal.setPayment(true);
                objTerminal = this.serviceDBTerminal.save(objTerminal);
                objInvoice.setDate(LocalDate.now());
                objInvoice.setTime(LocalTime.now());
                objInvoice.setPaymentMethod(prmTerminal.getPaymethod());
                objInvoice.setTerminals(1);
                objInvoice.setBusinessId(objBusiness.getBusinessId());
                objInvoice.setReferenceNumber(serviceReferenceNumber);
                objInvoice.setServiceId(prmTerminal.getIdService());
                objInvoice.setInProcess(false);
                objInvoice.setTerminalIds(objTerminal.getTerminalId().toString());
                objInvoice = serviceDBInvoice.save(objInvoice);
                objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());

                if (objTerminal.isPrincipal()) {
                    objTerDoPay.setServiceDescription("Terminal Principal ID [" + objTerminal.getTerminalId() + "] - "
                            + objService.getServiceName() + " $"
                            + String.valueOf(formato.format(serviceValue)));
                    objTerminal.setLastPaymentValue(serviceValue);
                } else {
                    objTerDoPay.setServiceDescription("Terminal Adicional ID [" + objTerminal.getTerminalId() + "] - "
                            + objService.getServiceName() + " $"
                            + String.valueOf(formato.format(serviceValue)));
                    objTerminal.setLastPaymentValue(serviceValue);
                }
                objInvoice.setPaymentDescription("[ \"" + objTerDoPay.getServiceDescription() + "\"] ");
                objEmailBodyData.setServiceDescription(objTerDoPay.getServiceDescription());
                objTerDoPay.setTerminalId(objTerminal.getTerminalId());
                objTerDoPay.setPrincipal(objTerminal.isPrincipal());
                objTerDoPay.setAmount(amount);
                objTerDoPay.setIdService(objTerminal.getService().getServiceId());
                objEmailBodyData.getTerminalsDoPayment().add(objTerDoPay);
                emailService.notifyPaymentCreditCard(objEmailBodyData);
                emailService.notifyNewTerminal(objEmailBodyData);
                break;
            case "ATHMOVIL":
                objTerminal.setExpirationDate(Instant.now().minus(Duration.ofDays(1)));
                objTerminal.setPayment(false);
                objTerminal = this.serviceDBTerminal.save(objTerminal);
                objInvoice.setDate(LocalDate.now());
                objInvoice.setTime(LocalTime.now());
                objInvoice.setPaymentMethod(prmTerminal.getPaymethod());
                objInvoice.setTerminals(1);
                objInvoice.setBusinessId(objBusiness.getBusinessId());
                objInvoice.setReferenceNumber(serviceReferenceNumber);
                objInvoice.setServiceId(prmTerminal.getIdService());
                objInvoice.setInProcess(true);
                objInvoice.setTerminalIds(objTerminal.getTerminalId().toString());
                objInvoice = serviceDBInvoice.save(objInvoice);
                objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                if (objTerminal.isPrincipal()) {
                    objTerDoPay.setServiceDescription("Terminal Principal ID [" + objTerminal.getTerminalId() + "] - "
                            + objService.getServiceName() + " $"
                            + String.valueOf(formato.format(serviceValue)));
                    objTerminal.setLastPaymentValue(serviceValue);
                } else {
                    objTerDoPay.setServiceDescription("Terminal Adicional ID [" + objTerminal.getTerminalId() + "] - "
                            + objService.getServiceName() + " $"
                            + String.valueOf(formato.format(serviceValue)));
                    objTerminal.setLastPaymentValue(serviceValue);
                }
                objInvoice.setPaymentDescription("[ \"" + objTerDoPay.getServiceDescription() + "\"] ");
                objEmailBodyData.setServiceDescription(objTerDoPay.getServiceDescription());
                objTerDoPay.setTerminalId(objTerminal.getTerminalId());
                objTerDoPay.setPrincipal(objTerminal.isPrincipal());
                objTerDoPay.setAmount(amount);
                objTerDoPay.setIdService(objTerminal.getService().getServiceId());
                objEmailBodyData.getTerminalsDoPayment().add(objTerDoPay);
                emailService.notifyPaymentATHMovil(objEmailBodyData);
                emailService.notifyNewTerminal(objEmailBodyData);
                break;
            case "BANK-ACCOUNT":
                objTerminal.setExpirationDate(Instant.now().minus(Duration.ofDays(1)));
                objTerminal.setPayment(false);
                objTerminal = this.serviceDBTerminal.save(objTerminal);
                objInvoice.setDate(LocalDate.now());
                objInvoice.setTime(LocalTime.now());
                objInvoice.setPaymentMethod(prmTerminal.getPaymethod());
                objInvoice.setTerminals(1);
                objInvoice.setBusinessId(objBusiness.getBusinessId());
                objInvoice.setReferenceNumber(serviceReferenceNumber);
                objInvoice.setServiceId(prmTerminal.getIdService());
                objInvoice.setInProcess(true);
                objInvoice.setTerminalIds(objTerminal.getTerminalId().toString());

                objInvoice = serviceDBInvoice.save(objInvoice);
                objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());

                if (objTerminal.isPrincipal()) {
                    objTerDoPay.setServiceDescription("Terminal Principal ID [" + objTerminal.getTerminalId() + "] - "
                            + objService.getServiceName() + " $"
                            + String.valueOf(formato.format(serviceValue)));
                    objTerminal.setLastPaymentValue(serviceValue);
                } else {
                    objTerDoPay.setServiceDescription("Terminal Adicional ID [" + objTerminal.getTerminalId() + "] - "
                            + objService.getServiceName() + " $"
                            + String.valueOf(formato.format(serviceValue)));
                    objTerminal.setLastPaymentValue(serviceValue);
                }
                objInvoice.setPaymentDescription("[ \"" + objTerDoPay.getServiceDescription() + "\"] ");
                objEmailBodyData.setServiceDescription(objTerDoPay.getServiceDescription());
                objTerDoPay.setTerminalId(objTerminal.getTerminalId());
                objTerDoPay.setPrincipal(objTerminal.isPrincipal());
                objTerDoPay.setAmount(amount);
                objTerDoPay.setIdService(objTerminal.getService().getServiceId());
                objEmailBodyData.getTerminalsDoPayment().add(objTerDoPay);
                emailService.notifyPaymentBankAccount(objEmailBodyData);
                emailService.notifyNewTerminal(objEmailBodyData);
                break;
            case "PAID-WITH-DISCOUNT":
                objTerminal.setPayment(true);
                objTerminal = this.serviceDBTerminal.save(objTerminal);
                objInvoice.setDate(LocalDate.now());
                objInvoice.setTime(LocalTime.now());
                objInvoice.setPaymentMethod(prmTerminal.getPaymethod());
                objInvoice.setTerminals(1);
                objInvoice.setBusinessId(objBusiness.getBusinessId());
                objInvoice.setReferenceNumber(" DESCUENTO APLICADO: $" + objEmailBodyData.getDiscount());
                objInvoice.setServiceId(prmTerminal.getIdService());
                objInvoice.setInProcess(false);
                objInvoice.setTerminalIds(objTerminal.getTerminalId().toString());
                objInvoice = serviceDBInvoice.save(objInvoice);
                objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());

                if (objTerminal.isPrincipal()) {
                    objTerDoPay.setServiceDescription("Terminal Principal ID [" + objTerminal.getTerminalId() + "] - "
                            + objService.getServiceName() + " $"
                            + String.valueOf(formato.format(serviceValue)));
                    objTerminal.setLastPaymentValue(serviceValue);
                } else {
                    objTerDoPay.setServiceDescription("Terminal Adicional ID [" + objTerminal.getTerminalId() + "] - "
                            + objService.getServiceName() + " $"
                            + String.valueOf(formato.format(serviceValue)));
                    objTerminal.setLastPaymentValue(serviceValue);
                }
                objInvoice.setPaymentDescription("[ \"" + objTerDoPay.getServiceDescription() + "\"] ");
                objEmailBodyData.setServiceDescription(objTerDoPay.getServiceDescription());
                objTerDoPay.setTerminalId(objTerminal.getTerminalId());
                objTerDoPay.setPrincipal(objTerminal.isPrincipal());
                objTerDoPay.setAmount(amount);
                objTerDoPay.setIdService(objTerminal.getService().getServiceId());
                objEmailBodyData.getTerminalsDoPayment().add(objTerDoPay);
                emailService.notifyPaymentDiscount(objEmailBodyData);
                emailService.notifyNewTerminal(objEmailBodyData);
                break;
        }
        objTerminal.setName("Terminal " + objTerminal.getTerminalId());
        objTerminal = this.serviceDBTerminal.save(objTerminal);
        TerminalDTO terminalDTO = this.mapper.map(objTerminal, TerminalDTO.class);
        if (terminalDTO != null) {
            objBusiness.setAdditionalTerminals(objBusiness.getAdditionalTerminals() + 1);
            this.serviceDBBusiness.save(objBusiness);
            rta = new ResponseEntity<TerminalDTO>(terminalDTO, HttpStatus.CREATED);
        } else {
            rta = new ResponseEntity<String>("Error al crear el Terminal", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }

    public String uniqueString() {
        String random = UUID.randomUUID().toString();
        random = random.replaceAll("-", "");
        random = random.substring(0, 16);

        return random;
    }

    private BuyTerminalDTO validateData(BuyTerminalDTO prmRegistry) {
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

    /**
     * Retrieves a list of expired terminals for a given business ID.
     *
     * @param businessId the ID of the business
     * @return a ResponseEntity containing the list of expired terminals
     */
    @Override
    public ResponseEntity<?> getExpiredTerminals(Long businessId) {
        List<TerminalDTO> listTerminalDTO = new ArrayList<>();
        if (businessId != null) {
            Optional<Business> optional = this.serviceDBBusiness.findById(businessId);
            if (optional.isPresent()) {
                listTerminalDTO = this.mapper.map(
                        this.serviceDBTerminal.findByBusinessAndExpirationDateLessThan(optional.get(), Instant.now()),
                        new TypeToken<List<TerminalDTO>>() {
                        }.getType());
                return new ResponseEntity<List<TerminalDTO>>(listTerminalDTO, HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException(
                "El Business con businessId " + businessId + " no existe en la Base de datos");
        throw objExeption;
    }

    /**
     * Generates a unique Terminal ID.
     *
     * @return a unique 6 character string in the format "RMxxxx"
     */
    @Override
    @Transactional(readOnly = true)
    public String getTerminalId() {
        Random random = new Random();
        //long currentTimeMillis = System.currentTimeMillis();
        //long generatedId = currentTimeMillis + randomInt;
        Terminal terminal = null;
        int randomInt = 0;
        String formattedInt = "00000";
        do {
            randomInt = random.nextInt(99999); // Agrega una aleatoriedad para reducir colisiones
            formattedInt = String.format("%05d", randomInt);
            String terminalId = "RM" + formattedInt;
            terminal = this.serviceDBTerminal.findById(terminalId).orElse(null);
            if (terminal == null) {
                return terminalId;
            }
        } while (terminal != null);

        return "RM" + formattedInt;
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateAutomaticPayments(String idTerminal, Boolean status) {
        if (idTerminal != null) {
            Optional<Terminal> optional = this.serviceDBTerminal.findById(idTerminal);
            if (optional.isPresent()) {
                this.serviceDBTerminal.updateAutomaticPayments(idTerminal, status);
                return new ResponseEntity<Boolean>(true, HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException(
                "El Terminal con terminalId " + idTerminal + " no existe en la Base de datos");
        throw objExeption;
    }

}
