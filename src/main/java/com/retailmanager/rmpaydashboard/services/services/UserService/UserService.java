package com.retailmanager.rmpaydashboard.services.services.UserService;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.retailmanager.rmpaydashboard.enums.Rol;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConsumeAPIException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.FileModel;
import com.retailmanager.rmpaydashboard.models.Invoice;
import com.retailmanager.rmpaydashboard.models.PaymentData;
import com.retailmanager.rmpaydashboard.models.Service;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.models.User;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.FileRepository;
import com.retailmanager.rmpaydashboard.repositories.InvoiceRepository;
import com.retailmanager.rmpaydashboard.repositories.ServiceRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;
import com.retailmanager.rmpaydashboard.repositories.UsersAppRepository;
import com.retailmanager.rmpaydashboard.services.DTO.BusinessDTO;
import com.retailmanager.rmpaydashboard.services.DTO.InvoiceDTO;
import com.retailmanager.rmpaydashboard.services.DTO.RegistryDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalsDoPaymentDTO;
import com.retailmanager.rmpaydashboard.services.DTO.UserDTO;
import com.retailmanager.rmpaydashboard.services.services.BusinessService.IBusinessService;
import com.retailmanager.rmpaydashboard.services.services.EmailService.EmailBodyData;
import com.retailmanager.rmpaydashboard.services.services.EmailService.IEmailService;
import com.retailmanager.rmpaydashboard.services.services.Payment.IATHMovilService;
import com.retailmanager.rmpaydashboard.services.services.Payment.IBlackStoneService;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMPaymentReqData;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMPaymentResponse;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ItemATHM;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponseJSON;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponsePayment;
import com.retailmanager.rmpaydashboard.services.services.ResellerServices.IResellerService;
import com.retailmanager.rmpaydashboard.services.services.TerminalService.ITerminalService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@org.springframework.stereotype.Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository serviceDBUser;
    @Autowired
    private UsersAppRepository serviceDBEmployee;
    @Autowired
    private ServiceRepository serviceDBService;
    @Autowired
    private IEmailService emailService;
    @Autowired
    private InvoiceRepository serviceDBInvoice;
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private IResellerService resellerService;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    DecimalFormat formato = new DecimalFormat("#.##", symbols);
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private IBusinessService businessService;
    @Autowired
    private IBlackStoneService blackStoneService;
    String msgError = "";
    @Autowired
    private TerminalRepository serviceDBTerminal;
    @Autowired
    private ITerminalService terminalService;
    Gson gson = new Gson();

    @Autowired
    private IATHMovilService athMovilService;

    /**
     * Save user data into the database and return the response entity
     *
     * @param prmUser the user data to be saved
     * @return the response entity containing the saved user data or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(UserDTO prmUser) {
        prmUser.setRegisterDate(Instant.now());
        if (prmUser.getUserID() != null) {
            final boolean exist = this.serviceDBUser.existsById(prmUser.getUserID());
            if (exist) {
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El Usuario con userId " + prmUser.getUserID() + " ya existe en la Base de datos");
                throw objExeption;
            } else {
                prmUser.setUserID(0L);
            }
        }
        if (prmUser.getEmail() != null) {
            Optional<User> exist2 = this.serviceDBUser.findOneByEmail(prmUser.getEmail());
            if (exist2.isPresent()) {
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El Usuario con email " + prmUser.getEmail() + " ya existe en la Base de datos");
                throw objExeption;
            }
        }
        Optional<User> exist = this.serviceDBUser.findOneByUsername(prmUser.getUsername());
        if (exist.isPresent()) {
            EntidadYaExisteException objExeption = new EntidadYaExisteException("El Usuario con username " + prmUser.getUsername() + " ya existe en la Base de datos");
            throw objExeption;
        }
        prmUser.setLastLogin(Instant.now());
        System.out.println("password: " + prmUser.getPassword());
        prmUser.setPassword(new BCryptPasswordEncoder().encode(prmUser.getPassword()));
        System.out.println("password encode: " + prmUser.getPassword());

        User objUser = this.mapper.map(prmUser, User.class);
        if (objUser.getRol() == null) {
            objUser.setRol(Rol.ROLE_USER);
        }
        User objUserRTA = null;
        if (objUser != null) {
            objUserRTA = this.serviceDBUser.save(objUser);
            objUserRTA.setBusiness(null);
        }
        UserDTO userDTO = this.mapper.map(objUserRTA, UserDTO.class);
        ResponseEntity<?> rta;
        if (userDTO != null) {
            rta = new ResponseEntity<UserDTO>(userDTO, HttpStatus.CREATED);
        } else {
            rta = new ResponseEntity<String>("Error al crear el User", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }

    /**
     * Update user information in the database.
     *
     * @param userId  the user ID
     * @param prmUser the user DTO with updated information
     * @return a response entity with the updated user DTO or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> update(Long userId, UserDTO prmUser) {
        ResponseEntity<?> rta = null;

        if (userId != null) {
            Optional<User> optional = this.serviceDBUser.findById(userId);
            if (optional.isPresent() == false) {
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El Usuario con userId " + userId + " no existe en la Base de datos");
                throw objExeption;
            } else {
                User objUser = optional.get();
                objUser.setName(prmUser.getName());
                if (prmUser.getPassword() != null && prmUser.getPassword().compareTo("unchanged") != 0) {
                    objUser.setPassword(new BCryptPasswordEncoder().encode(prmUser.getPassword()));
                }
                if (objUser.getUsername().compareTo(prmUser.getUsername()) != 0) {
                    Optional<User> exist = this.serviceDBUser.findOneByUsername(prmUser.getUsername());
                    if (exist.isPresent()) {
                        EntidadYaExisteException objExeption = new EntidadYaExisteException("El Usuario con username " + prmUser.getUsername() + " ya existe en la Base de datos");
                        throw objExeption;
                    } else {
                        objUser.setUsername(prmUser.getUsername());
                    }
                }
                objUser.setEmail(prmUser.getEmail());
                objUser.setPhone(prmUser.getPhone());
                objUser.setRol(prmUser.getRol());
                User objUserRTA = this.serviceDBUser.save(objUser);
                objUserRTA.setBusiness(null);
                UserDTO userDTO = this.mapper.map(objUserRTA, UserDTO.class);

                if (userDTO != null) {
                    userDTO.setPassword(null);
                    rta = new ResponseEntity<UserDTO>(userDTO, HttpStatus.OK);
                } else {
                    rta = new ResponseEntity<String>("Error al actualizar el User", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            rta = new ResponseEntity<String>("El id del usuario es null", HttpStatus.BAD_REQUEST);
        }
        return rta;
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId the ID of the user to be deleted
     * @return true if the user is successfully deleted, false otherwise
     */
    @Override
    @Transactional
    public boolean delete(Long userId) {
        boolean bandera = false;

        if (userId != null) {
            Optional<User> optional = this.serviceDBUser.findById(userId);
            if (optional.isPresent()) {
                User objUser = optional.get();
                if (objUser != null) {
                    this.serviceDBUser.delete(objUser);
                    bandera = true;
                }

            }
        }
        return bandera;
    }

    /**
     * Find a user by ID and return ResponseEntity with UserDTO if found, or throw an exception.
     *
     * @param userId the ID of the user to find
     * @return ResponseEntity with UserDTO if user is found, or an exception if not found
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long userId) {
        if (userId != null) {
            Optional<User> optional = this.serviceDBUser.findById(userId);
            if (optional.isPresent()) {
                optional.get().getBusiness().forEach(business -> business.setUser(null));
                UserDTO objUserDTO = this.mapper.map(optional.get(), UserDTO.class);

                return new ResponseEntity<UserDTO>(objUserDTO, HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Usuario con userId " + userId + " no existe en la Base de datos");
        throw objExeption;
    }

    /**
     * Finds a user by username and returns a ResponseEntity with the user information.
     *
     * @param username the username of the user to find
     * @return a ResponseEntity with the user information if found, otherwise throws an exception
     */
    @Override
    public ResponseEntity<?> findByUsername(String username) {
        if (username != null) {
            User optional = this.serviceDBUser.findOneByUsername(username).orElse(null);
            if (optional != null) {
                optional.getBusiness().forEach(business -> business.setUser(null));
                UserDTO objUserDTO = this.mapper.map(optional, UserDTO.class);

                objUserDTO.setPassword(null);
                return new ResponseEntity<UserDTO>(objUserDTO, HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Usuario con username " + username + " no existe en la Base de datos");
        throw objExeption;
    }

    /**
     * Retrieve user´s business by userId.
     *
     * @param userId the identifier of the user
     * @return ResponseEntity with a list of BusinessDTO or an exception if the user does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUserBusiness(Long userId) {
        if (userId != null) {
            Optional<User> optional = this.serviceDBUser.findById(userId);
            if (optional.isPresent()) {
                optional.get().getBusiness().forEach(business -> business.setUser(null));
                List<BusinessDTO> listBusinessDTO = new ArrayList<>();
                for (Business business : optional.get().getBusiness()) {
                    // Mapea el objeto Business a BusinessDTO usando ModelMapper
                    BusinessDTO businessDTO = this.mapper.map(business, BusinessDTO.class);
                    // Mapea la lista de Terminales a una lista de TerminalDTO manualmente

                    List<TerminalDTO> listTerminalDTO = business.getTerminals().stream()
                            .map(terminal -> {
                                terminal.setBusiness(null);
                                TerminalDTO terminalDTO = this.mapper.map(terminal, TerminalDTO.class);
                                return terminalDTO;
                            })
                            .collect(Collectors.toList());

                    businessDTO.setTerminals(listTerminalDTO);
                    listBusinessDTO.add(businessDTO);
                }
                List<BusinessDTO> listBusiness = listBusinessDTO;
                return new ResponseEntity<List<BusinessDTO>>(listBusiness, HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Usuario con userId " + userId + " no existe en la Base de datos");
        throw objExeption;
    }

    /**
     * Updates the enable status of a user.
     *
     * @param userId the unique identifier of the user
     * @param enable the new enable status
     * @return a ResponseEntity with a boolean indicating the success of the update
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateEnable(Long userId, boolean enable) {
        if (userId != null) {
            Optional<User> optional = this.serviceDBUser.findById(userId);
            if (optional.isPresent()) {
                this.serviceDBUser.updateEnable(userId, enable);
                return new ResponseEntity<Boolean>(true, HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Usuario con userId " + userId + " no existe en la Base de datos");
        throw objExeption;
    }

    /**
     * Registry a user with the business and process payment.
     *
     * @param prmRegistry the user's registry data
     * @return the response entity with the result of the registration process
     */
    @Override
    @Transactional
    public ResponseEntity<?> registryWithBusiness(RegistryDTO prmRegistry) {
        String msg = "No se pudo registrar el usuario";
        Double amount = 0.0;
        Double comisiones = 0.0;
        Double stateTax = 0.0;
        Double stateTaxRate = 0.04;
        ResponsePayment respPayment;
        Double aditionalTerminalsValue = 0.0;
        String serviceReferenceNumber = null;
        PaymentData objPaymentData = null;
        String descripcion = "";
        EmailBodyData objEmailBodyData = mapper.map(prmRegistry, EmailBodyData.class);
        objEmailBodyData.setDiscount(0.0);
        objEmailBodyData.setTerminalsDoPayment(new ArrayList<>());
        List<ItemATHM> items = new ArrayList<>();
        Optional<User> exist = this.serviceDBUser.findOneByUsername(prmRegistry.getUsername());
        if (exist.isPresent()) {
            EntidadYaExisteException objExeption = new EntidadYaExisteException("El Usuario con username " + prmRegistry.getUsername() + " ya existe en la Base de datos");
            throw objExeption;
        }
        Optional<User> exist2 = this.serviceDBUser.findOneByEmail(prmRegistry.getEmail());
        if (exist2.isPresent()) {
            EntidadYaExisteException objExeption = new EntidadYaExisteException("El Usuario con email " + prmRegistry.getEmail() + " ya existe en la Base de datos");
            throw objExeption;
        }
        Optional<Business> existB = this.serviceDBBusiness.findOneByMerchantId(prmRegistry.getMerchantId());
        if (existB.isPresent()) {
            EntidadYaExisteException objExeption = new EntidadYaExisteException("El business con merchantId " + prmRegistry.getMerchantId() + " ya existe en la Base de datos");
            throw objExeption;
        }
        try {
            prmRegistry = validateData(prmRegistry);
            if (prmRegistry == null) {
                return new ResponseEntity<String>(msgError, HttpStatus.BAD_REQUEST);
            }
            if (prmRegistry.getAdditionalTerminals() != null && prmRegistry.getAdditionalTerminals() != 0) {
                Optional<Service> optional = this.serviceDBService.findById(prmRegistry.getServiceId());
                if (!optional.isPresent()) {
                    EntidadNoExisteException objExeption = new EntidadNoExisteException("El Servicio con serviceId " + prmRegistry.getServiceId() + " no existe en la Base de datos");
                    throw objExeption;
                }
                String userTransactionNumber = uniqueString();
                Service objService = optional.get();
                descripcion = objService.getServiceDescription();
                descripcion += ": $" + String.valueOf(formato.format(objService.getServiceValue())) + "\n";
                amount = objService.getServiceValue();
                comisiones = objService.getReferralPayment();
                //INFORMACIÓN PARA ATH MOVIL
                ItemATHM item = new ItemATHM();
                item.setDescription(descripcion);
                item.setName(objService.getServiceName());
                item.setPrice(String.valueOf(formato.format(objService.getServiceValue())));
                item.setTax(String.valueOf(formato.format(objService.getServiceValue() * stateTaxRate)));
                item.setQuantity("1");
                items.add(item);
                //Calculo del valor de los terminales
                if (prmRegistry.getAdditionalTerminals() <= 5) {
                    aditionalTerminalsValue = objService.getTerminals2to5();
                    descripcion += "Terminales Adicionales: $" + prmRegistry.getAdditionalTerminals() + " X $" + String.valueOf(formato.format(objService.getTerminals2to5())) + "\n";
                    amount += (prmRegistry.getAdditionalTerminals() - 1) * objService.getTerminals2to5();
                    comisiones += (prmRegistry.getAdditionalTerminals() - 1) * objService.getReferralPayment2to5();
                    objEmailBodyData.setAdditionalTerminalsValue(objService.getTerminals2to5());
                    //INFORMACIÓN PARA ATH MOVIL
                    ItemATHM item1 = new ItemATHM();
                    item1.setDescription(descripcion);
                    item1.setName(objService.getServiceName());
                    item1.setPrice(String.valueOf(formato.format(objService.getServiceValue())));
                    item1.setTax(String.valueOf(formato.format(objService.getServiceValue() * stateTaxRate)));
                    item1.setQuantity("1");
                    items.add(item1);
                } else if (prmRegistry.getAdditionalTerminals() > 5 && prmRegistry.getAdditionalTerminals() < 10) {
                    descripcion += "Terminales Adicionales: $" + prmRegistry.getAdditionalTerminals() + " X $" + String.valueOf(formato.format(objService.getTerminals2to5())) + "\n";
                    aditionalTerminalsValue = objService.getTerminals6to9();
                    amount += (prmRegistry.getAdditionalTerminals() - 1) * objService.getTerminals6to9();
                    comisiones += (prmRegistry.getAdditionalTerminals() - 1) * objService.getReferralPayment6to9();
                    objEmailBodyData.setAdditionalTerminalsValue(objService.getTerminals6to9());
                    //INFORMACIÓN PARA ATH MOVIL
                    ItemATHM item1 = new ItemATHM();
                    item1.setDescription(descripcion);
                    item1.setName(objService.getServiceName());
                    item1.setPrice(String.valueOf(formato.format(objService.getServiceValue())));
                    item1.setTax(String.valueOf(formato.format(objService.getServiceValue() * stateTaxRate)));
                    item1.setQuantity("1");
                    items.add(item1);
                } else {
                    descripcion += "Terminales Adicionales: $" + prmRegistry.getAdditionalTerminals() + " X $" + String.valueOf(formato.format(objService.getTerminals10())) + "\n";
                    aditionalTerminalsValue = objService.getTerminals10();
                    amount += (prmRegistry.getAdditionalTerminals() - 1) * objService.getTerminals10();
                    comisiones += (prmRegistry.getAdditionalTerminals() - 1) * objService.getReferralPayment10();
                    objEmailBodyData.setAdditionalTerminalsValue(objService.getTerminals10());
                    //INFORMACIÓN PARA ATH MOVIL
                    ItemATHM item1 = new ItemATHM();
                    item1.setDescription(descripcion);
                    item1.setName(objService.getServiceName());
                    item1.setPrice(String.valueOf(formato.format(objService.getServiceValue())));
                    item1.setTax(String.valueOf(formato.format(objService.getServiceValue() * stateTaxRate)));
                    item1.setQuantity("1");
                    items.add(item1);
                }
                stateTax = amount * stateTaxRate;
                objEmailBodyData.setSubTotal(amount);
                objEmailBodyData.setStateTax(stateTax);
                amount = amount + stateTax;
                objEmailBodyData.setAmount(amount);

                objEmailBodyData.setServiceDescription(objService.getServiceDescription());
                objEmailBodyData.setServiceValue(formato.format(objService.getServiceValue()));


                switch (prmRegistry.getPaymethod()) {
                    case "CREDIT-CARD":
                        respPayment = blackStoneService.paymentWithCreditCard(String.valueOf(formato.format(amount)),
                                prmRegistry.getAddress().getZipcode(),
                                prmRegistry.getCreditcarnumber().replaceAll("-", ""),
                                prmRegistry.getExpDateMonth() + prmRegistry.getExpDateYear(),
                                prmRegistry.getNameoncard(),
                                prmRegistry.getSecuritycode(), null, userTransactionNumber);
                        if (respPayment.getResponseCode() != 200) {
                            objEmailBodyData.setReferenceNumber(respPayment.getServiceReferenceNumber());
                            objEmailBodyData.setErrorMessage(respPayment.getMsg().toString());
                            emailService.notifyErrorRegister(objEmailBodyData);
                            HashMap<String, String> objError = new HashMap<String, String>();
                            objError.put("msg", "No se pudo registrar el pago con la tarjeta de credito");
                            return new ResponseEntity<HashMap<String, String>>(objError, HttpStatus.NOT_ACCEPTABLE);
                        }
                        if (prmRegistry.isAutomaticPayments()) {
                            try {
                                ResponseJSON objToken = blackStoneService.getToken(prmRegistry.getAddress().getZipcode(),
                                        prmRegistry.getCreditcarnumber().replaceAll("-", ""),
                                        prmRegistry.getExpDateMonth() + prmRegistry.getExpDateYear(),
                                        prmRegistry.getNameoncard(),
                                        prmRegistry.getSecuritycode(), null, userTransactionNumber);
                                if (objToken.getResponseCode() == 200) {
                                    objPaymentData = new PaymentData();
                                    objPaymentData.setToken(objToken.getToken());
                                    objPaymentData.setExpDate(prmRegistry.getExpDateMonth() + prmRegistry.getExpDateYear());
                                    objPaymentData.setNameOnCard(prmRegistry.getNameoncard());
                                    objPaymentData.setCvn(prmRegistry.getSecuritycode());
                                    objPaymentData.setLast4Digits(prmRegistry.getCreditcarnumber().replaceAll("-", "").substring(prmRegistry.getCreditcarnumber().length() - 4));
                                    objPaymentData.setUsingAutomaticPayment(true);
                                }

                            } catch (Exception e) {
                                System.out.println("Error: No se pudo obtener el token para guardar el token de pago automatico: " + e.getMessage());
                            }

                        }
                        serviceReferenceNumber = respPayment.getServiceReferenceNumber();
                        objEmailBodyData.setReferenceNumber(serviceReferenceNumber);
                        break;
                }
            } else {
                prmRegistry.setAdditionalTerminals(0);
                objEmailBodyData.setAdditionalTerminals(0);
                prmRegistry.setPaymethod("SIN METODO DE PAGO");
                objEmailBodyData.setPaymethod("SIN METODO DE PAGO");
                prmRegistry.setServiceId(null);
                objEmailBodyData.setServiceDescription("");
            }

            UserDTO objUserDTO = new UserDTO();
            objUserDTO.setName(prmRegistry.getName());
            objUserDTO.setPassword(prmRegistry.getPassword());
            objUserDTO.setEmail(prmRegistry.getEmail());
            objUserDTO.setUsername(prmRegistry.getUsername());
            objUserDTO.setEnable(true);
            objUserDTO.setRol(Rol.ROLE_USER);
            objUserDTO.setPhone(prmRegistry.getPhone());
            objUserDTO.setRegisterDate(Instant.now());

            ResponseEntity<?> objResponseU = this.save(objUserDTO);
            if (objResponseU.getStatusCode() == HttpStatus.CREATED) {
                objUserDTO = (UserDTO) objResponseU.getBody();
                if (objUserDTO != null) {
                    BusinessDTO objBusinessDTO = new BusinessDTO();
                    objBusinessDTO.setUserId(objUserDTO.getUserID());
                    objBusinessDTO.setName(prmRegistry.getBusinessName());
                    objBusinessDTO.setAddress(prmRegistry.getAddress());
                    objBusinessDTO.setBusinessPhoneNumber(prmRegistry.getBusinessPhoneNumber());
                    objBusinessDTO.setAdditionalTerminals(prmRegistry.getAdditionalTerminals());
                    objBusinessDTO.setMerchantId(prmRegistry.getMerchantId());
                    objBusinessDTO.setServiceId(prmRegistry.getServiceId());
                    objBusinessDTO.setEnable(true);
                    objBusinessDTO.setDiscount(0.0);
                    if (prmRegistry.getPaymethod() != null && prmRegistry.getPaymethod().equals("CREDIT-CARD")) {
                        objBusinessDTO.setLastPayment(Instant.now());
                    }

                    ResponseEntity<?> objResponseB = this.businessService.save(objBusinessDTO);

                    if (objResponseB.getStatusCode() == HttpStatus.CREATED) {
                        objBusinessDTO = (BusinessDTO) objResponseB.getBody();
                        if (prmRegistry.getAdditionalTerminals() != null && prmRegistry.getAdditionalTerminals() != 0 && objBusinessDTO != null) {
                            Business objBusiness = serviceDBBusiness.findById(objBusinessDTO.getBusinessId()).get();
                            if (objPaymentData != null) {
                                objBusiness.setPaymentData(objPaymentData);
                                objBusiness = serviceDBBusiness.save(objBusiness);
                            }
                            Service objService = serviceDBService.findById(prmRegistry.getServiceId()).get();
                            Invoice objInvoice = new Invoice();
                            objInvoice.setSubTotal(objEmailBodyData.getSubTotal());
                            objInvoice.setStateTax(stateTax);
                            objInvoice.setTotalAmount(amount);
                            List<String> listTerminalIds = new ArrayList<String>();
                            List<String> paymentDescription = new ArrayList<>();
                            paymentDescription.add(descripcion);
                            ATHMPaymentResponse payResponse = null;
                            switch (prmRegistry.getPaymethod()) {
                                case "CREDIT-CARD":
                                    for (int i = 0; i < prmRegistry.getAdditionalTerminals(); i++) {
                                        TerminalsDoPaymentDTO objTerminalsDoPaymentDTO = new TerminalsDoPaymentDTO();
                                        Terminal objTerminal = new Terminal();
                                        objTerminal.setTerminalId(this.terminalService.getTerminalId());
                                        objTerminal.setRegisterDate(Instant.now());
                                        objTerminal.setEnable(true);
                                        objTerminal.setBusiness(objBusiness);
                                        objTerminal.setExpirationDate(Instant.now().plus(Duration.ofDays(objService.getDuration())));
                                        objTerminal.setSerial(null);
                                        objTerminal.setName(objTerminal.getTerminalId());
                                        objTerminal.setService(objService);
                                        if (i == 0) {
                                            objTerminal.setPrincipal(true);
                                        } else {
                                            objTerminal.setPrincipal(false);
                                        }
                                        objTerminal.setPayment(true);
                                        objTerminal.setAutomaticPayments(prmRegistry.isAutomaticPayments());
                                        objTerminal = this.serviceDBTerminal.save(objTerminal);
                                        //guardamos algunos datos en este objeto para discriminar el pago dentro del correo que se envia al usuario
                                        if (i == 0) {
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Principal ID [" + objTerminal.getTerminalId() + "] - " + objService.getServiceName() + " $" + String.valueOf(formato.format(objService.getServiceValue())));
                                            objTerminal.setLastPaymentValue(objService.getServiceValue());
                                        } else {
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Adicional ID [" + objTerminal.getTerminalId() + "] - " + objService.getServiceName() + " $" + String.valueOf(formato.format(aditionalTerminalsValue)));
                                            objTerminal.setLastPaymentValue(aditionalTerminalsValue);
                                        }
                                        objTerminalsDoPaymentDTO.setTerminalId(objTerminal.getTerminalId());
                                        objTerminalsDoPaymentDTO.setPrincipal(objTerminal.isPrincipal());
                                        objTerminalsDoPaymentDTO.setAmount(aditionalTerminalsValue);
                                        objTerminalsDoPaymentDTO.setIdService(objService.getServiceId());
                                        objEmailBodyData.getTerminalsDoPayment().add(objTerminalsDoPaymentDTO);
                                        listTerminalIds.add(objTerminal.getTerminalId());
                                    }
                                    objInvoice.setPaymentDescription(gson.toJson(paymentDescription));
                                    objInvoice.setDate(LocalDate.now());
                                    objInvoice.setTime(LocalTime.now());
                                    objInvoice.setPaymentMethod(prmRegistry.getPaymethod());
                                    objInvoice.setTerminals(prmRegistry.getAdditionalTerminals());
                                    objInvoice.setBusinessId(objBusinessDTO.getBusinessId());
                                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                                    objInvoice.setServiceId(prmRegistry.getServiceId());
                                    objInvoice.setInProcess(false);
                                    objInvoice.setTerminalIds(listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));
                                    objInvoice = serviceDBInvoice.save(objInvoice);
                                    this.resellerService.addResellerSales(prmRegistry.getIdReseller(), prmRegistry.getMerchantId(), amount, comisiones, objInvoice, descripcion);
                                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                                    emailService.notifyPaymentCreditCard(objEmailBodyData);
                                    break;
                                case "ATHMOVIL":
                                    if (prmRegistry.getAthPhone() == null) {
                                        HashMap<String, String> rta = new HashMap<>();
                                        rta.put("msg", "El Negocio no tiene un número de telefono para enviar el pago de ATHMovil");
                                        return new ResponseEntity<>(rta, HttpStatus.BAD_REQUEST);
                                    }
                                    if (prmRegistry.getAthPhone().compareTo("") == 0) {
                                        HashMap<String, String> rta = new HashMap<>();
                                        rta.put("msg", "El Negocio no tiene un número de telefono para enviar el pago de ATHMovil");
                                        return new ResponseEntity<>(rta, HttpStatus.BAD_REQUEST);
                                    }
                                    // Se crea la data para enviar el pago a ATH Movil
                                    ATHMPaymentReqData req = new ATHMPaymentReqData();
                                    req.setEnv("production");
                                    req.setMetadata1("RETAIL MANAGER PR - RMPAY DASHBOARD");
                                    String cleanPhone = prmRegistry.getAthPhone().replaceAll("\\D", "");

                                    req.setPhoneNumber(cleanPhone);
                                    req.setSubtotal(String.valueOf(formato.format(objInvoice.getSubTotal())));
                                    req.setTax(String.valueOf(formato.format(objInvoice.getStateTax())));
                                    req.setTimeout("5000");
                                    req.setTotal(String.valueOf(formato.format(objInvoice.getTotalAmount())));
                                    req.setItems(items);
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
                                    for (int i = 0; i < prmRegistry.getAdditionalTerminals(); i++) {
                                        TerminalsDoPaymentDTO objTerminalsDoPaymentDTO = new TerminalsDoPaymentDTO();
                                        Terminal objTerminal = new Terminal();
                                        objTerminal.setTerminalId(this.terminalService.getTerminalId());
                                        objTerminal.setRegisterDate(Instant.now());
                                        objTerminal.setEnable(true);
                                        objTerminal.setBusiness(objBusiness);
                                        objTerminal.setExpirationDate(Instant.now().plus(Duration.ofDays(1)));
                                        objTerminal.setSerial(null);
                                        objTerminal.setName(objTerminal.getTerminalId());
                                        objTerminal.setService(objService);
                                        if (i == 0) {
                                            objTerminal.setPrincipal(true);
                                        } else {
                                            objTerminal.setPrincipal(false);
                                        }
                                        objTerminal.setPayment(false);
                                        objTerminal.setAutomaticPayments(prmRegistry.isAutomaticPayments());
                                        objTerminal = this.serviceDBTerminal.save(objTerminal);
                                        listTerminalIds.add(objTerminal.getTerminalId());
                                        //guardamos algunos datos en este objeto para discriminar el pago dentro del correo que se envia al usuario
                                        if (i == 0) {
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Principal ID [" + objTerminal.getTerminalId() + "] - " + objService.getServiceName() + " $" + String.valueOf(formato.format(objService.getServiceValue())));
                                            objTerminal.setLastPaymentValue(objService.getServiceValue());
                                        } else {
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Adicional ID [" + objTerminal.getTerminalId() + "] - " + objService.getServiceName() + " $" + String.valueOf(formato.format(aditionalTerminalsValue)));
                                            objTerminal.setLastPaymentValue(aditionalTerminalsValue);
                                        }
                                        objTerminalsDoPaymentDTO.setTerminalId(objTerminal.getTerminalId());
                                        objTerminalsDoPaymentDTO.setPrincipal(objTerminal.isPrincipal());
                                        objTerminalsDoPaymentDTO.setAmount(aditionalTerminalsValue);
                                        objTerminalsDoPaymentDTO.setIdService(objService.getServiceId());
                                        objEmailBodyData.getTerminalsDoPayment().add(objTerminalsDoPaymentDTO);

                                    }
                                    objInvoice.setPaymentDescription(gson.toJson(paymentDescription));
                                    objInvoice.setDate(LocalDate.now());
                                    objInvoice.setTime(LocalTime.now());
                                    objInvoice.setPaymentMethod(prmRegistry.getPaymethod());
                                    objInvoice.setTerminals(prmRegistry.getAdditionalTerminals());
                                    objInvoice.setBusinessId(objBusinessDTO.getBusinessId());
                                    serviceReferenceNumber = gson.toJson(payResponse);
                                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                                    objInvoice.setServiceId(prmRegistry.getServiceId());
                                    objInvoice.setInProcess(true);
                                    objInvoice.setTerminalIds(listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));
                                    objInvoice.setATHMPaymentDetails(gson.toJson(objEmailBodyData));
                                    objInvoice = serviceDBInvoice.save(objInvoice);
                                    this.resellerService.addResellerSales(prmRegistry.getIdReseller(), prmRegistry.getMerchantId(), amount, comisiones, objInvoice, descripcion);
                                    prmRegistry.setPaymentResume(mapper.map(objInvoice, InvoiceDTO.class));
                                    //objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());

                                    //emailService.notifyPaymentATHMovil(objEmailBodyData);
                                    break;
                                case "BANK-ACCOUNT":
                                    for (int i = 0; i < prmRegistry.getAdditionalTerminals(); i++) {
                                        TerminalsDoPaymentDTO objTerminalsDoPaymentDTO = new TerminalsDoPaymentDTO();
                                        Terminal objTerminal = new Terminal();
                                        objTerminal.setTerminalId(this.terminalService.getTerminalId());
                                        objTerminal.setRegisterDate(Instant.now());
                                        objTerminal.setEnable(false);
                                        objTerminal.setBusiness(objBusiness);
                                        objTerminal.setExpirationDate(Instant.now().plus(Duration.ofDays(1)));
                                        objTerminal.setSerial(null);
                                        objTerminal.setName(objTerminal.getTerminalId());
                                        objTerminal.setService(objService);
                                        if (i == 0) {
                                            objTerminal.setPrincipal(true);
                                        } else {
                                            objTerminal.setPrincipal(false);
                                        }
                                        objTerminal.setPayment(false);
                                        objTerminal.setAutomaticPayments(prmRegistry.isAutomaticPayments());
                                        objTerminal = this.serviceDBTerminal.save(objTerminal);
                                        listTerminalIds.add(objTerminal.getTerminalId());
                                        //guardamos algunos datos en este objeto para discriminar el pago dentro del correo que se envia al usuario
                                        if (i == 0) {
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Principal ID [" + objTerminal.getTerminalId() + "] - " + objService.getServiceName() + " $" + String.valueOf(formato.format(objService.getServiceValue())));
                                            objTerminal.setLastPaymentValue(objService.getServiceValue());
                                        } else {
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Adicional ID [" + objTerminal.getTerminalId() + "] - " + objService.getServiceName() + " $" + String.valueOf(formato.format(aditionalTerminalsValue)));
                                            objTerminal.setLastPaymentValue(aditionalTerminalsValue);
                                        }
                                        objTerminalsDoPaymentDTO.setTerminalId(objTerminal.getTerminalId());
                                        objTerminalsDoPaymentDTO.setPrincipal(objTerminal.isPrincipal());
                                        objTerminalsDoPaymentDTO.setAmount(aditionalTerminalsValue);
                                        objTerminalsDoPaymentDTO.setIdService(objService.getServiceId());
                                        objEmailBodyData.getTerminalsDoPayment().add(objTerminalsDoPaymentDTO);
                                    }
                                    objInvoice.setPaymentDescription(gson.toJson(paymentDescription));
                                    objInvoice.setDate(LocalDate.now());
                                    objInvoice.setTime(LocalTime.now());
                                    objInvoice.setPaymentMethod(prmRegistry.getPaymethod());
                                    objInvoice.setTerminals(prmRegistry.getAdditionalTerminals());
                                    objInvoice.setBusinessId(objBusinessDTO.getBusinessId());
                                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                                    objInvoice.setServiceId(prmRegistry.getServiceId());
                                    objInvoice.setInProcess(true);
                                    objInvoice.setTerminalIds(listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));

                                    objInvoice = serviceDBInvoice.save(objInvoice);
                                    this.resellerService.addResellerSales(prmRegistry.getIdReseller(), prmRegistry.getMerchantId(), amount, comisiones, objInvoice, descripcion);
                                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                                    emailService.notifyPaymentBankAccount(objEmailBodyData);
                                    break;
                            }
                        }

                        emailService.notifyNewRegister(objEmailBodyData);
                        return new ResponseEntity<RegistryDTO>(prmRegistry, HttpStatus.CREATED);
                    } else {
                        msg = "No se pudo registrar el Negocio";
                    }
                } else {
                    msg = "No se pudo registrar el usuario";
                }
            } else {
                msg = "No se pudo registrar el usuario";
            }
        } catch (ConsumeAPIException ex) {
            System.err.println("Error en el consumo de BlackStone: CodigoHttp " + ex.getHttpStatusCode() + " \n Mensje: " + ex.getMessage());

            HashMap<String, String> map = new HashMap<>();
            map.put("msg", "Por favor comuniquese con el administrador de la página.");
            return new ResponseEntity<HashMap<String, String>>(map, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.out.println("Error en com.retailmanager.rmpaydashboard.services.services.UserService.UserService:RegistryWithBusiness: " + e.getMessage());
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>(msg, HttpStatus.BAD_REQUEST);
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
    private RegistryDTO validateData(RegistryDTO prmRegistry) {
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

    /**
     * Obtains all users from the database and maps them to DTOs.
     *
     * @return ResponseEntity containing a list of users in DTO form with HttpStatus.OK
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll() {
        List<UserDTO> listUserDTO = new ArrayList<>();
        Iterable<User> listUser = this.serviceDBUser.findAll();
        listUser.forEach(objUser -> {
            if (objUser.getRol().compareTo(Rol.ROLE_USER) == 0) {
                objUser.getBusiness().forEach(businessObj -> {
                    businessObj.setUser(null);
                });
                listUserDTO.add(this.mapper.map(objUser, UserDTO.class));
            }
        });
        return new ResponseEntity<List<UserDTO>>(listUserDTO, HttpStatus.OK);
    }

    /**
     * Retrieves active clients from the database and maps them to DTOs.
     *
     * @return ResponseEntity containing a list of active clients in DTO form with HttpStatus.OK
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getActivesClients() {
        List<User> listUser = this.serviceDBUser.findActives();
        listUser.forEach(user -> {
            user.getBusiness().forEach(businessObj -> {
                businessObj.setUser(null);
            });
        });
        List<UserDTO> listUserDTO = this.mapper.map(listUser, new TypeToken<List<UserDTO>>() {
        }.getType());
        return new ResponseEntity<List<UserDTO>>(listUserDTO, HttpStatus.OK);
    }

    /**
     * Retrieves unregistered clients from the database.
     * unregistered clients are clients that have not logged in for more than 10 days.
     *
     * @return Returns a ResponseEntity with a list of unregistered clients and HTTP status OK
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUnregisteredClients() {
        LocalDate date = LocalDate.now();
        date = date.minusDays(10);
        List<User> listUser = this.serviceDBUser.findByLastLoginIsNullOrLastLoginLessThan(date);
        listUser.forEach(user -> {
            user.setBusiness(null);
        });
        List<UserDTO> listUserDTO = this.mapper.map(listUser, new TypeToken<List<UserDTO>>() {
        }.getType());
        return new ResponseEntity<List<UserDTO>>(listUserDTO, HttpStatus.OK);
    }

    /**
     * Find all users with pagination and filter.
     *
     * @param pageable pagination information
     * @param filter   filter string
     * @return ResponseEntity with list of UserDTO and HTTP status
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll(Pageable pageable, String filter) {
        filter = "%" + filter + "%";
        Page<Business> listBusiness = this.serviceDBBusiness.findyAllClientsByFilter(pageable, filter);

        listBusiness.forEach(objBusinessDTO -> {
            objBusinessDTO.setUsersBusiness(null);

            objBusinessDTO.setCategories(null);
            objBusinessDTO.getTerminals().forEach(objTerminalDTO -> {
                objTerminalDTO.setBusiness(null);
            });
            objBusinessDTO.getUser().setBusiness(null);
        });

        return new ResponseEntity<Page<Business>>(listBusiness, HttpStatus.OK);
    }

    /**
     * Find all users with pagination.
     *
     * @param pageable the pagination information
     * @return a ResponseEntity containing the paginated list of users
     */
    @Override
    public ResponseEntity<?> findAll(Pageable pageable) {

        Page<Business> listBusiness = this.serviceDBBusiness.findyAllClientsPageable(pageable);

        listBusiness.forEach(objBusinessDTO -> {
            objBusinessDTO.setUsersBusiness(null);

            objBusinessDTO.setCategories(null);
            objBusinessDTO.getTerminals().forEach(objTerminalDTO -> {
                objTerminalDTO.setBusiness(null);
            });
            objBusinessDTO.getUser().setBusiness(null);
        });
        return new ResponseEntity<Page<Business>>(listBusiness, HttpStatus.OK);
    }

    /**
     * Retrieves a list of users with manager roles.
     *
     * @return a ResponseEntity containing the list of UserDTO objects representing the users with manager roles
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllUsersManagers() {
        List<Rol> listRol = new ArrayList<>();
        listRol.add(Rol.ROLE_MANAGER_VIEW);
        listRol.add(Rol.ROLE_MANAGER);

        List<User> listBusiness = this.serviceDBUser.findAllUsersManagers(listRol);
        List<UserDTO> listUserDTO = this.mapper.map(listBusiness, new TypeToken<List<UserDTO>>() {
        }.getType());
        return new ResponseEntity<List<UserDTO>>(listUserDTO, HttpStatus.OK);

    }

    /**
     * Updates the password for a user with a manager role.
     *
     * @param userId   the ID of the user to update
     * @param password the new password
     * @return a ResponseEntity containing the updated user or a BAD_REQUEST status if the
     * user with the given ID does not exist
     */
    @Override
    @Transactional
    public ResponseEntity<?> updatePasswordForAdmin(Long userId, String password) {
        User user = this.serviceDBUser.findById(userId).orElse(null);

        if (user != null) {
            if (user.getRol().compareTo(Rol.ROLE_MANAGER) != 0 && user.getRol().compareTo(Rol.ROLE_MANAGER_VIEW) != 0) {
                HashMap<String, String> map = new HashMap<>();
                map.put("message", "No tiene permisos para realizar esta operacion");
                return new ResponseEntity<>(map, HttpStatus.FORBIDDEN);
            }
            user.setPassword(new BCryptPasswordEncoder().encode(password));
            this.serviceDBUser.save(user);
            HashMap<String, String> map = new HashMap<>();
            map.put("message", "Contraseña actualizada correctamente");
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
        return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
    }


}
