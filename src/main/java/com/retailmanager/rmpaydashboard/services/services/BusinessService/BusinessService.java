package com.retailmanager.rmpaydashboard.services.services.BusinessService;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.retailmanager.rmpaydashboard.enums.EmployeeRole;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConsumeAPIException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Invoice;
import com.retailmanager.rmpaydashboard.models.PaymentData;
import com.retailmanager.rmpaydashboard.models.Permission;
import com.retailmanager.rmpaydashboard.models.Service;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.models.User;
import com.retailmanager.rmpaydashboard.models.UserPermission;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.InvoiceRepository;
import com.retailmanager.rmpaydashboard.repositories.PermisionRepository;
import com.retailmanager.rmpaydashboard.repositories.ServiceRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;
import com.retailmanager.rmpaydashboard.repositories.UsersAppRepository;
import com.retailmanager.rmpaydashboard.services.DTO.BusinessDTO;
import com.retailmanager.rmpaydashboard.services.DTO.CategoryDTO;
import com.retailmanager.rmpaydashboard.services.DTO.RegsitryBusinessDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalsDoPaymentDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.ActivationDashboardResponseDto;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.ActivationDto;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.DailyTrendDto;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.RegistrationDto;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.StatusDistributionDto;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.StatusDistributionPercentageDto;
import com.retailmanager.rmpaydashboard.services.services.EmailService.EmailBodyData;
import com.retailmanager.rmpaydashboard.services.services.EmailService.IEmailService;
import com.retailmanager.rmpaydashboard.services.services.FileServices.IFileService;
import com.retailmanager.rmpaydashboard.services.services.Payment.IBlackStoneService;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponseJSON;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponsePayment;
import com.retailmanager.rmpaydashboard.services.services.TerminalService.ITerminalService;

@org.springframework.stereotype.Service
public class BusinessService implements IBusinessService {
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private UserRepository serviceDBUser;
    @Autowired
    private UsersAppRepository serviceDBEmployee;
    @Autowired
    private ServiceRepository serviceDBService;
    @Autowired
    private TerminalRepository serviceDBTerminal;
    @Autowired
    private ITerminalService terminalService;
     @Autowired
    private PermisionRepository serviceDBUPermission;
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private IBlackStoneService blackStoneService;
    String msgError = "";
    @Autowired 
    private IEmailService emailService;
    @Autowired 
    private InvoiceRepository serviceDBInvoice;
    @Autowired
    private IFileService fileService;
    Gson gson = new Gson();
    DecimalFormat formato = new DecimalFormat("#.##");
    /**
     * Save a business entity.
     *
     * @param  prmBusiness   the business DTO to be saved
     * @return               the response entity with the saved business DTO or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(BusinessDTO prmBusiness) {
        Long businessId = prmBusiness.getBusinessId();
        if(businessId!=null){
            final boolean exist = this.serviceDBBusiness.existsById(businessId);
            if(exist){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El business con businessId "+prmBusiness.getBusinessId()+" ya existe en la Base de datos");
                throw objExeption;
            }else{
                prmBusiness.setBusinessId(null);
            }
        }
        Optional<Business> exist = this.serviceDBBusiness.findOneByMerchantId(prmBusiness.getMerchantId());
            if(exist.isPresent()){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El business con merchantId "+prmBusiness.getMerchantId()+" ya existe en la Base de datos");
                throw objExeption;
            }
        Business objBusiness= this.mapper.map(prmBusiness, Business.class);
        Long serviceId = prmBusiness.getServiceId();
        Optional<Service> existService=null;
        if(serviceId!=null){
            if(serviceId!=0){
                existService= this.serviceDBService.findById(serviceId);
                if(!existService.isPresent()){
                    EntidadNoExisteException objExeption = new EntidadNoExisteException("El service con serviceId "+prmBusiness.getServiceId()+" no existe en la Base de datos");
                    throw objExeption;
                }else{
                    objBusiness.setServiceId(serviceId);
                }
            }
        }
        ResponseEntity<?> rta;
        Optional<User> existUser=null; 
         if(objBusiness!=null){
            Long userId=prmBusiness.getUserId();
            if(userId!=null){
                existUser = this.serviceDBUser.findById(userId);
                if(!existUser.isPresent()){
                    EntidadNoExisteException objExeption = new EntidadNoExisteException("El User con userId "+prmBusiness.getUserId()+" no existe en la Base de datos");
                    throw objExeption;
                }else{
                    objBusiness.setUser(existUser.get());
                }
            }
            objBusiness.setRegisterDate(Instant.now());
            objBusiness=this.serviceDBBusiness.save(objBusiness);
            // Si el negocio se ha creado correctamente, 
            //creamos el usuario administrador por defecto
            // y le asignamos el negocio. 
            UsersBusiness objUserBusiness=new UsersBusiness();
            objUserBusiness.setBusiness(objBusiness);
            objUserBusiness.setEnable(true);
            objUserBusiness.setUsername(EmployeeRole.ADMIN.getName());
            objUserBusiness.setPassword("1234");
            objUserBusiness.setCostHour(0.0);
            objUserBusiness.setCreatedAt(LocalDate.now().atStartOfDay());
            objUserBusiness.setUpdatedAt(LocalDate.now().atStartOfDay());
            Iterable<Permission> listPermissions = this.serviceDBUPermission.findAll();
           List<UserPermission> userPermissionsList = new ArrayList<>();

            for (Permission permission : listPermissions) {
                UserPermission userPermission = new UserPermission();
                userPermission.setPermission(permission); // Establece el objeto Permission
                userPermission.setUserBusiness(objUserBusiness); // Establece el UserBusiness asociado
                userPermission.setEnable(true); // Establece 'true' por defecto

                userPermissionsList.add(userPermission);
            }

            // Finalmente, establece la lista de UserPermission en tu objUserBusiness
            objUserBusiness.setUserPermissions(userPermissionsList);
            objUserBusiness.setDownload(false);
            objUserBusiness.setRoleId(EmployeeRole.ADMIN.getId());
            objUserBusiness=this.serviceDBEmployee.save(objUserBusiness);

         }
        BusinessDTO businessDTO=this.mapper.map(objBusiness, BusinessDTO.class);
        if(businessDTO!=null){
            
            rta=new ResponseEntity<BusinessDTO>(businessDTO, HttpStatus.CREATED);
        }else{
            rta= new ResponseEntity<String>("Error al crear el Business",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }
    /**
     * Save a business entity.
     *
     * @param  prmBusiness   the business DTO to be saved
     * @return               the response entity with the saved business DTO or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(RegsitryBusinessDTO prmBusiness) {
        Long businessId = prmBusiness.getBusinessId();
        prmBusiness=validateData(prmBusiness);
            if(prmBusiness==null){
                return new ResponseEntity<String>(msgError,HttpStatus.BAD_REQUEST);
            }
        if(businessId!=null){
            final boolean exist = this.serviceDBBusiness.existsById(businessId);
            if(exist){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El business con businessId "+prmBusiness.getBusinessId()+" ya existe en la Base de datos");
                throw objExeption;
            }else{
                prmBusiness.setBusinessId(null);
            }
        }
        Optional<Business> exist = this.serviceDBBusiness.findOneByMerchantId(prmBusiness.getMerchantId());
            if(exist.isPresent()){
                EntidadYaExisteException objExeption = new EntidadYaExisteException("El business con merchantId "+prmBusiness.getMerchantId()+" ya existe en la Base de datos");
                throw objExeption;
            }
        Business objBusiness= this.mapper.map(prmBusiness, Business.class);
        Long serviceId = prmBusiness.getServiceId();
        Optional<Service> existService=null;
        Service objService=null;
        
        if(serviceId!=null){
            if(serviceId!=0){
                existService= this.serviceDBService.findById(serviceId);
                if(!existService.isPresent()){
                    EntidadNoExisteException objExeption = new EntidadNoExisteException("El service con serviceId "+prmBusiness.getServiceId()+" no existe en la Base de datos");
                    throw objExeption;
                }else{
                    objService=existService.get();
                    objBusiness.setServiceId(serviceId);
                }
            }
        }
        Optional<User> existUser=null; 
        Long userId=prmBusiness.getUserId();
        User objUserDTO=null;
        if(userId!=null){
                existUser = this.serviceDBUser.findById(userId);
                if(!existUser.isPresent()){

                    EntidadNoExisteException objExeption = new EntidadNoExisteException("El User con userId "+prmBusiness.getUserId()+" no existe en la Base de datos");
                    throw objExeption;
                }else{
                    objUserDTO=existUser.get();
                    objBusiness.setUser(existUser.get());
                }
        }
        if(objBusiness==null || objService==null || objUserDTO==null){
            return new ResponseEntity<String>("Error al crear el Business",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Double amount=0.0;
        ResponsePayment respPayment;
        String serviceReferenceNumber=null;
        PaymentData objPaymentData=null;
        EmailBodyData objEmailBodyData=mapper.map(prmBusiness, EmailBodyData.class);
        objEmailBodyData.setDiscount(0.0);
        objEmailBodyData.setTerminalsDoPayment(new ArrayList<>());
        Double aditionalTerminalsValue=0.0;
        Double stateTax=0.0;
        try {
            objEmailBodyData.setEmail(objUserDTO.getEmail());
            if(prmBusiness.getAdditionalTerminals()!=null && prmBusiness.getAdditionalTerminals()!=0){
                
                String userTransactionNumber = uniqueString();
                
                String descripcion=objService.getServiceDescription();
                descripcion+=": $"+String.valueOf(formato.format(objService.getServiceValue()))+"\n";
                amount=objService.getServiceValue();
                //Calculo del valor de los terminales
                if(prmBusiness.getAdditionalTerminals()<=5){
                    descripcion+="Terminales Adicionales: $"+prmBusiness.getAdditionalTerminals()+" X $"+String.valueOf(formato.format(objService.getTerminals2to5()))+"\n";
                    amount+=(prmBusiness.getAdditionalTerminals()-1)*objService.getTerminals2to5();
                    objEmailBodyData.setAdditionalTerminalsValue(objService.getTerminals2to5());
                }else if(prmBusiness.getAdditionalTerminals()>5 && prmBusiness.getAdditionalTerminals()<10){
                    amount+=(prmBusiness.getAdditionalTerminals()-1)*objService.getTerminals6to9();
                    objEmailBodyData.setAdditionalTerminalsValue(objService.getTerminals6to9());
                }else{
                    amount+=(prmBusiness.getAdditionalTerminals()-1)*objService.getTerminals10();
                    objEmailBodyData.setAdditionalTerminalsValue(objService.getTerminals10());
                }
                stateTax=amount*0.04;
                objEmailBodyData.setStateTax(stateTax);
                objEmailBodyData.setSubTotal(amount);
                objEmailBodyData.setAmount(amount+stateTax);
                objEmailBodyData.setServiceDescription(objService.getServiceDescription());
                objEmailBodyData.setServiceValue(formato.format(objService.getServiceValue()));
                amount=amount+stateTax;
                switch (prmBusiness.getPaymethod()){
                    case "CREDIT-CARD":
                        respPayment=blackStoneService.paymentWithCreditCard(String.valueOf(formato.format(amount)), 
                        prmBusiness.getAddress().getZipcode(), 
                        prmBusiness.getCreditcarnumber().replaceAll("-", ""),
                        prmBusiness.getExpDateMonth() + prmBusiness.getExpDateYear(), 
                        prmBusiness.getNameoncard(), 
                        prmBusiness.getSecuritycode(), null, userTransactionNumber);
                        System.out.println("RESPONSE CODE :("+String.valueOf(formato.format(amount))+")"+respPayment.getResponseCode());
                        if(respPayment.getResponseCode()!=200){
                            emailService.notifyErrorRegister(objEmailBodyData);
                            HashMap <String, String> objError=new HashMap<String, String>();
                            objError.put("msg", "No se pudo registrar el pago con la tarjeta de credito");
                            return new ResponseEntity<HashMap<String, String>>(objError,HttpStatus.NOT_ACCEPTABLE);
                        }
                        if(prmBusiness.isAutomaticPayments()){
                            try{
                                ResponseJSON objToken=blackStoneService.getToken(prmBusiness.getAddress().getZipcode(),
                                prmBusiness.getCreditcarnumber().replaceAll("-", ""),
                                prmBusiness.getExpDateMonth() + prmBusiness.getExpDateYear(),
                                prmBusiness.getNameoncard(),
                                prmBusiness.getSecuritycode(), null, userTransactionNumber);
                                if(objToken.getResponseCode()==200){
                                    objPaymentData=new PaymentData();
                                    objPaymentData.setToken(objToken.getToken());
                                    objPaymentData.setExpDate(prmBusiness.getExpDateMonth() + prmBusiness.getExpDateYear());
                                    objPaymentData.setNameOnCard(prmBusiness.getNameoncard());
                                    objPaymentData.setCvn(prmBusiness.getSecuritycode());
                                    objPaymentData.setLast4Digits(prmBusiness.getCreditcarnumber().replaceAll("-", "").substring(prmBusiness.getCreditcarnumber().length()-4));
                                    objEmailBodyData.setAutomaticPayments(true);
                                    objBusiness.setPaymentData(objPaymentData);

                                }
                            
                            } catch (Exception e) {
                                System.out.println("Error: No se pudo obtener el token para guardar el token de pago automatico: "+e.getMessage());
                            }
                            
                        }
                        serviceReferenceNumber=respPayment.getServiceReferenceNumber();
                        objEmailBodyData.setReferenceNumber(serviceReferenceNumber);
                    break;
                }
            }else{
                prmBusiness.setAdditionalTerminals(0);
                objEmailBodyData.setAdditionalTerminals(0);
                prmBusiness.setPaymethod("SIN METODO DE PAGO");
                objEmailBodyData.setPaymethod("SIN METODO DE PAGO");
                prmBusiness.setServiceId(null);
                objEmailBodyData.setServiceDescription("");
            }
            if(objUserDTO!=null){
                    objBusiness.setAdditionalTerminals(prmBusiness.getAdditionalTerminals());
                    objBusiness.setEnable(true);
                    objBusiness.setDiscount(0.0);
                    if(prmBusiness.getPaymethod()!=null && prmBusiness.getPaymethod().equals("CREDIT-CARD")){
                        objBusiness.setLastPayment(Instant.now());
                    }
                    objBusiness.setRegisterDate(Instant.now());
                    objBusiness=this.serviceDBBusiness.save(objBusiness);
                    // Si el negocio se ha creado correctamente, 
            //creamos el usuario administrador por defecto
            // y le asignamos el negocio. 
            UsersBusiness objUserBusiness=new UsersBusiness();
            objUserBusiness.setBusiness(objBusiness);
            objUserBusiness.setEnable(true);
            objUserBusiness.setUsername(EmployeeRole.ADMIN.getName());
            objUserBusiness.setPassword("1234");
            objUserBusiness.setCostHour(0.0);
            objUserBusiness.setCreatedAt(LocalDate.now().atStartOfDay());
            objUserBusiness.setUpdatedAt(LocalDate.now().atStartOfDay());
            Iterable<Permission> listPermissions = this.serviceDBUPermission.findAll();
            List<UserPermission> userPermissionsList = new ArrayList<>();

            for (Permission permission : listPermissions) {
                UserPermission userPermission = new UserPermission();
                userPermission.setPermission(permission); // Establece el objeto Permission
                userPermission.setUserBusiness(objUserBusiness); // Establece el UserBusiness asociado
                userPermission.setEnable(true); // Establece 'true' por defecto

                userPermissionsList.add(userPermission);
            }

            // Finalmente, establece la lista de UserPermission en tu objUserBusiness
            objUserBusiness.setUserPermissions(userPermissionsList);
            objUserBusiness.setDownload(false);
            objUserBusiness.setRoleId(EmployeeRole.ADMIN.getId());
            objUserBusiness=this.serviceDBEmployee.save(objUserBusiness);
            // Creamos los terminales del negocio
                    objBusiness.setTerminals(new ArrayList<Terminal>());
                    BusinessDTO objBusinessDTO=new BusinessDTO();
                    if(objBusiness!=null){
                        
                        objBusinessDTO=this.mapper.map(objBusiness, BusinessDTO.class);
                        if(prmBusiness.getAdditionalTerminals()!=null && prmBusiness.getAdditionalTerminals()!=0 && objBusinessDTO!=null && prmBusiness.getPaymethod()!=null){
                            List<String> listTerminalIds=new ArrayList<String>();
                            Invoice objInvoice=new Invoice();
                            List<String> paymentDescription=new ArrayList<>();
                            objInvoice.setSubTotal(objEmailBodyData.getSubTotal());
                            objInvoice.setTotalAmount(amount);
                            objInvoice.setStateTax(stateTax);
                            switch (prmBusiness.getPaymethod()){
                                case "CREDIT-CARD":
                                    for (int i = 0; i < prmBusiness.getAdditionalTerminals(); i++) {
                                        TerminalsDoPaymentDTO objTerminalsDoPaymentDTO=new TerminalsDoPaymentDTO();
                                        Terminal objTerminal=new Terminal();
                                        objTerminal.setTerminalId(this.terminalService.getTerminalId());
                                        objTerminal.setRegisterDate(Instant.now());
                                        objTerminal.setEnable(true);
                                        objTerminal.setBusiness(objBusiness);
                                        objTerminal.setExpirationDate(Instant.now().plus(Duration.ofDays(objService.getDuration())));
                                        objTerminal.setSerial(null);
                                        objTerminal.setName(objTerminal.getTerminalId());
                                        objTerminal.setService(objService);
                                        objTerminal.setPayment(true);
                                        if(i==0){
                                            objTerminal.setPrincipal(true);
                                        }else{
                                            objTerminal.setPrincipal(false);
                                        }
                                        objTerminal.setAutomaticPayments(prmBusiness.isAutomaticPayments());
                                        objTerminal=serviceDBTerminal.save(objTerminal);
                                         //guardamos algunos datos en este objeto para discriminar el pago dentro del correo que se envia al usuario
                                         if(i==0){
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Principal ID ["+objTerminal.getTerminalId()+"] - "+objService.getServiceName()+" $"+String.valueOf(formato.format(objService.getServiceValue())));
                                            objTerminal.setLastPaymentValue(objService.getServiceValue());
                                        }else{
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Adicional ID ["+objTerminal.getTerminalId()+"] - "+objService.getServiceName()+" $"+String.valueOf(formato.format(objEmailBodyData.getAdditionalTerminalsValue())));
                                            objTerminal.setLastPaymentValue(objEmailBodyData.getAdditionalTerminalsValue());
                                        }
                                        paymentDescription.add(objTerminalsDoPaymentDTO.getServiceDescription());
                                        objTerminalsDoPaymentDTO.setTerminalId(objTerminal.getTerminalId());
                                        objTerminalsDoPaymentDTO.setPrincipal(objTerminal.isPrincipal());
                                        objTerminalsDoPaymentDTO.setAmount(aditionalTerminalsValue);
                                        objTerminalsDoPaymentDTO.setIdService(objService.getServiceId());
                                        objEmailBodyData.getTerminalsDoPayment().add(objTerminalsDoPaymentDTO);
                                        objBusinessDTO.getTerminals().add(this.mapper.map(objTerminal, TerminalDTO.class));
                                        listTerminalIds.add(objTerminal.getTerminalId());
                                    }
                                    objInvoice.setPaymentDescription(gson.toJson(paymentDescription));
                                    objInvoice.setDate(LocalDate.now());
                                    objInvoice.setTime(LocalTime.now());
                                    objInvoice.setPaymentMethod(prmBusiness.getPaymethod());
                                    objInvoice.setTerminals(prmBusiness.getAdditionalTerminals());
                                    objInvoice.setBusinessId(objBusinessDTO.getBusinessId());
                                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                                    objInvoice.setServiceId(prmBusiness.getServiceId());
                                    objInvoice.setInProcess(false);
                                    objInvoice.setTerminalIds(listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));
                                    objInvoice=serviceDBInvoice.save(objInvoice);
                                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                                    emailService.notifyPaymentCreditCard(objEmailBodyData);
                                break;
                                case "ATHMOVIL":
                                    for (int i = 0; i < prmBusiness.getAdditionalTerminals(); i++) {
                                        TerminalsDoPaymentDTO objTerminalsDoPaymentDTO=new TerminalsDoPaymentDTO();
                                        Terminal objTerminal=new Terminal();
                                        objTerminal.setTerminalId(this.terminalService.getTerminalId());
                                        objTerminal.setRegisterDate(Instant.now());
                                        objTerminal.setEnable(true);
                                        objTerminal.setBusiness(objBusiness);
                                        objTerminal.setExpirationDate(Instant.now().plus(Duration.ofDays(objService.getDuration())));
                                        objTerminal.setSerial(null);
                                        objTerminal.setName(objTerminal.getTerminalId());
                                        objTerminal.setService(objService);
                                        if(i==0){
                                            objTerminal.setPrincipal(true);
                                        }else{
                                            objTerminal.setPrincipal(false);
                                        }
                                        objTerminal.setPayment(false);
                                        objTerminal.setAutomaticPayments(prmBusiness.isAutomaticPayments());
                                        objTerminal=this.serviceDBTerminal.save(objTerminal);
                                         //guardamos algunos datos en este objeto para discriminar el pago dentro del correo que se envia al usuario
                                         if(i==0){
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Principal ID ["+objTerminal.getTerminalId()+"] - "+objService.getServiceName()+" $"+String.valueOf(formato.format(objService.getServiceValue())));
                                            objTerminal.setLastPaymentValue(objService.getServiceValue());
                                        }else{
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Adicional ID ["+objTerminal.getTerminalId()+"] - "+objService.getServiceName()+" $"+String.valueOf(formato.format(aditionalTerminalsValue)));
                                            objTerminal.setLastPaymentValue(objEmailBodyData.getAdditionalTerminalsValue());
                                        }
                                         objTerminalsDoPaymentDTO.setTerminalId(objTerminal.getTerminalId());
                                         objTerminalsDoPaymentDTO.setPrincipal(objTerminal.isPrincipal());
                                         objTerminalsDoPaymentDTO.setAmount(aditionalTerminalsValue);
                                         objTerminalsDoPaymentDTO.setIdService(objService.getServiceId());
                                         objEmailBodyData.getTerminalsDoPayment().add(objTerminalsDoPaymentDTO);
                                        objBusinessDTO.getTerminals().add(this.mapper.map(objTerminal, TerminalDTO.class));
                                        listTerminalIds.add(objTerminal.getTerminalId());
                                    }
                                    objInvoice.setPaymentDescription(gson.toJson(paymentDescription));
                                    objInvoice.setDate(LocalDate.now());
                                    objInvoice.setTime(LocalTime.now());
                                    objInvoice.setPaymentMethod(prmBusiness.getPaymethod());
                                    objInvoice.setTerminals(prmBusiness.getAdditionalTerminals());
                                    objInvoice.setBusinessId(objBusinessDTO.getBusinessId());
                                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                                    objInvoice.setServiceId(prmBusiness.getServiceId());
                                    objInvoice.setInProcess(true);
                                    objInvoice.setTerminalIds(listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));
                                    objInvoice=serviceDBInvoice.save(objInvoice);
                                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                                    emailService.notifyPaymentATHMovil(objEmailBodyData);
                                    break;
                                case "BANK-ACCOUNT":
                                    for (int i = 0; i < prmBusiness.getAdditionalTerminals(); i++) {
                                        TerminalsDoPaymentDTO objTerminalsDoPaymentDTO=new TerminalsDoPaymentDTO();
                                        Terminal objTerminal=new Terminal();
                                        objTerminal.setTerminalId(this.terminalService.getTerminalId());
                                        objTerminal.setRegisterDate(Instant.now());
                                        objTerminal.setEnable(false);
                                        objTerminal.setBusiness(objBusiness);
                                        objTerminal.setExpirationDate(Instant.now().plus(Duration.ofDays(objService.getDuration())));
                                        objTerminal.setSerial(null);
                                        objTerminal.setName(objTerminal.getTerminalId());
                                        objTerminal.setService(objService);
                                        if(i==0){
                                            objTerminal.setPrincipal(true);
                                        }else{
                                            objTerminal.setPrincipal(false);
                                        }
                                        objTerminal.setPayment(false);
                                        objTerminal.setAutomaticPayments(prmBusiness.isAutomaticPayments());
                                        objTerminal=this.serviceDBTerminal.save(objTerminal);
                                        objBusinessDTO.getTerminals().add(this.mapper.map(objTerminal, TerminalDTO.class));
                                        listTerminalIds.add(objTerminal.getTerminalId());
                                         //guardamos algunos datos en este objeto para discriminar el pago dentro del correo que se envia al usuario
                                         if(i==0){
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Principal ID ["+objTerminal.getTerminalId()+"] - "+objService.getServiceName()+" $"+String.valueOf(formato.format(objService.getServiceValue())));
                                            objTerminal.setLastPaymentValue(objService.getServiceValue());                                       
                                        }else{
                                            objTerminalsDoPaymentDTO.setServiceDescription("Terminal Adicional ID ["+objTerminal.getTerminalId()+"] - "+objService.getServiceName()+" $"+String.valueOf(formato.format(aditionalTerminalsValue)));
                                        
                                            objTerminal.setLastPaymentValue(objEmailBodyData.getAdditionalTerminalsValue());
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
                                    objInvoice.setPaymentMethod(prmBusiness.getPaymethod());
                                    objInvoice.setTerminals(prmBusiness.getAdditionalTerminals());
                                    objInvoice.setBusinessId(objBusinessDTO.getBusinessId());
                                    objInvoice.setReferenceNumber(serviceReferenceNumber);
                                    objInvoice.setServiceId(prmBusiness.getServiceId());
                                    objInvoice.setInProcess(true);
                                    objInvoice.setTerminalIds(listTerminalIds.toString().replace("[", "").replace("]", "").replace(" ", ""));

                                    objInvoice=serviceDBInvoice.save(objInvoice);
                                    objEmailBodyData.setInvoiceNumber(objInvoice.getInvoiceNumber());
                                    emailService.notifyPaymentBankAccount(objEmailBodyData);
                                break;
                            }
                        }
                        emailService.notifyNewBusiness(objEmailBodyData);
                        objBusinessDTO.getUser().setBusiness(null);
                        
                        return new ResponseEntity<BusinessDTO>(objBusinessDTO,HttpStatus.CREATED);
                    }
                }
        }catch (ConsumeAPIException ex) {
                System.err.println("Error en el consumo de BlackStone: CodigoHttp " + ex.getHttpStatusCode() + " \n Mensje: "+ ex.getMessage() );
                
                HashMap<String, String> map = new HashMap<>();
                map.put("msg", "Por favor comuniquese con el administrador de la página.");
                return new ResponseEntity<HashMap<String,String>>(map,HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>("Error al registrar el negocio",HttpStatus.INTERNAL_SERVER_ERROR);
    }
 
    /**
     * Generates a unique string using UUID.
     *
     * @return  the unique string generated
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
     * @param  prmRegistry  The RegistryDTO object to be validated
     * @return              The validated RegistryDTO object, or null if there is an error
     */
    private RegsitryBusinessDTO validateData(RegsitryBusinessDTO prmRegistry) {
        if(prmRegistry.getPaymethod()!=null && prmRegistry.getPaymethod().compareTo("CREDIT-CARD")==0){
            if(prmRegistry.getCreditcarnumber()!=null){
                if(!prmRegistry.getCreditcarnumber().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the credit card number";
                return null;
                }
            }else{
                msgError = "The credit card number is required";
                return null;
            }
            if(prmRegistry.getNameoncard()!=null){
                prmRegistry.setNameoncard(prmRegistry.getNameoncard().toUpperCase().trim());
            }else{
                msgError = "The name on card is required";
                return null;
            }
            if(prmRegistry.getSecuritycode()!=null){
                if(!prmRegistry.getSecuritycode().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the security code";
                return null;
                }
            }else{
                msgError = "The security code is required";
                return null;
            }
            if(prmRegistry.getExpDateMonth()!=null){
                if(!prmRegistry.getExpDateMonth().matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the expiration date";
                return null;
                }else{
                    if(Integer.parseInt(prmRegistry.getExpDateMonth())>12){
                        msgError = "The expiration date month must be less than or equal to 12";
                        return null;
                    }
                }
            }else{
                msgError = "The expiration date month is required";
                return null;
            }
            if(prmRegistry.getExpDateYear()!=null){
                if(!prmRegistry.getExpDateYear().matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the expiration date";
                return null;
                }else{
                    prmRegistry.setExpDateYear(prmRegistry.getExpDateYear().trim());
                    if(prmRegistry.getExpDateYear().length()!=2){
                        msgError = "The expiration date year must be 2 digits";
                        return null;
                    }
                }
            }else{
                msgError = "The expiration date year is required";
                return null;
            }
            
        }else if(prmRegistry.getPaymethod()!=null && prmRegistry.getPaymethod().compareTo("BANK-ACCOUNT")==0){
            if(prmRegistry.getAccountNameBank()!=null){
                prmRegistry.setAccountNameBank(prmRegistry.getAccountNameBank().toUpperCase().trim());
            }else{
                msgError = "The account name is required";
                return null;
            }
            if(prmRegistry.getAccountNumberBank()!=null){
                if(!prmRegistry.getAccountNumberBank().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the account number";
                    return null;
                }else{
                    prmRegistry.setAccountNumberBank(prmRegistry.getAccountNumberBank().trim());
                }
            }else{
                msgError = "The account number is required";
                return null;
            }
            if(prmRegistry.getRouteNumberBank()!=null){
                if(!prmRegistry.getRouteNumberBank().replace("-", "").matches("[+-]?\\d*(\\.\\d+)?")){
                    msgError = "Letters are not allowed in the route number";
                    return null;
                }else{
                    prmRegistry.setRouteNumberBank(prmRegistry.getRouteNumberBank().trim());
                }
            }else{
                msgError = "The route number is required";
                return null;
            }
            if(prmRegistry.getChequeVoidId()==null || prmRegistry.getChequeVoidId()==0){
                msgError = "The chequeVoidId is required";
                return null;
            }
            
        }
        return prmRegistry;
    }

    /**
     * Updates the business entity with the given businessId and prmBusiness details.
     *
     * @param  businessId    the ID of the business entity to be updated
     * @param  prmBusiness   the details of the business entity for update
     * @return               the ResponseEntity with the updated business entity or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> update(Long businessId, BusinessDTO prmBusiness) {
        Business objBusiness=null;
        ResponseEntity<?> rta=null;
        Long serviceId = prmBusiness.getServiceId();
        if(serviceId!=null){
            if(serviceId!=0){
                Optional<Service> existService = this.serviceDBService.findById(serviceId);
                if(!existService.isPresent()){
                    EntidadNoExisteException objExeption = new EntidadNoExisteException("El service con serviceId "+prmBusiness.getServiceId()+" no existe en la Base de datos");
                    throw objExeption;
                }
            }
        }
        if(businessId!=null){
            Optional<Business> exist = this.serviceDBBusiness.findById(businessId);
            if(!exist.isPresent()){
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El business con businessId "+prmBusiness.getBusinessId()+" ya existe en la Base de datos");
                throw objExeption;
            }
            objBusiness=exist.get();
            if(objBusiness.getMerchantId().compareTo(prmBusiness.getMerchantId())!=0){
                Optional<Business> exist2 = this.serviceDBBusiness.findOneByMerchantId(prmBusiness.getMerchantId());
                if(exist2.isPresent()){
                    EntidadYaExisteException objExeption = new EntidadYaExisteException("El business con merchantId "+prmBusiness.getMerchantId()+" ya existe en la Base de datos");
                    throw objExeption;
                }
            }
             objBusiness.setMerchantId(prmBusiness.getMerchantId());
             objBusiness.setAdditionalTerminals(prmBusiness.getAdditionalTerminals());
             objBusiness.setBusinessPhoneNumber(prmBusiness.getBusinessPhoneNumber());
             objBusiness.getAddress().setAddress1(prmBusiness.getAddress().getAddress1());
             objBusiness.getAddress().setAddress2(prmBusiness.getAddress().getAddress2());
             objBusiness.getAddress().setCity(prmBusiness.getAddress().getCity());
             objBusiness.getAddress().setCountry(prmBusiness.getAddress().getCountry());
             objBusiness.getAddress().setZipcode(prmBusiness.getAddress().getZipcode());
             objBusiness.setComment(prmBusiness.getComment());
             objBusiness.setPercentageProfit(prmBusiness.getPercentageProfit());
             objBusiness.setDiscount(prmBusiness.getDiscount());
             objBusiness.setName(prmBusiness.getName());
             objBusiness.setServiceId(serviceId);
             if(objBusiness.getLogo()!=null && prmBusiness.getLogo()!=null && !objBusiness.getLogo().equals(prmBusiness.getLogo())){
                if(objBusiness.getLogo()!=null){
                    this.fileService.deleteImage(objBusiness.getLogo());
                }
                objBusiness.setLogo(prmBusiness.getLogo());
             }else{
                if(objBusiness.getLogo()!=null && prmBusiness.getLogo()==null){
                    this.fileService.deleteImage(objBusiness.getLogo());
                    objBusiness.setLogo(null);
                }else if(objBusiness.getLogo()==null && prmBusiness.getLogo()!=null){
                    objBusiness.setLogo(prmBusiness.getLogo());
                }
             }
             if(objBusiness.getLogoAth()!=null && prmBusiness.getLogoAth()!=null && !objBusiness.getLogoAth().equals(prmBusiness.getLogoAth())){
                if(objBusiness.getLogoAth()!=null){
                    this.fileService.deleteImage(objBusiness.getLogoAth());
                }
                objBusiness.setLogoAth(prmBusiness.getLogoAth());
             }else{
                if(objBusiness.getLogoAth()!=null && prmBusiness.getLogoAth()==null){
                    this.fileService.deleteImage(objBusiness.getLogoAth());
                    objBusiness.setLogoAth(null);
                }else if(objBusiness.getLogoAth()==null && prmBusiness.getLogoAth()!=null){
                    objBusiness.setLogoAth(prmBusiness.getLogoAth());
                }
             }
             if(objBusiness!=null){
                objBusiness=this.serviceDBBusiness.save(objBusiness);
             }
             objBusiness.getUser().setBusiness(null);
            BusinessDTO businessDTO=this.mapper.map(objBusiness, BusinessDTO.class);
            if(businessDTO!=null){
                rta=new ResponseEntity<BusinessDTO>(businessDTO, HttpStatus.CREATED);
            }else{
                rta= new ResponseEntity<String>("Error al crear el Business",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return rta;
    }

    /**
     * Deletes a business record by its ID.
     *
     * @param  businessId   the ID of the business to delete
     * @return              true if the business was successfully deleted, false otherwise
     */
    @Override
    @Transactional
    public boolean delete(Long businessId) {
        boolean bandera=false;
        
        if(businessId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findById(businessId);
            if(optional.isPresent()){
                Business objBusiness=optional.get();
                if(objBusiness!=null){
                    this.serviceDBBusiness.delete(objBusiness);
                    bandera=true;
                }
                
            }
        }
        return bandera;
    }

    /**
     * Find a Business by its ID and return a ResponseEntity with BusinessDTO if found, 
     * otherwise throw an EntidadNoExisteException.
     *
     * @param  businessId   the ID of the Business to find
     * @return              a ResponseEntity with BusinessDTO if the Business is found
     *                      or throw an EntidadNoExisteException if not found
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long businessId) {
        if(businessId!=null){
            Business optional= this.serviceDBBusiness.findById(businessId).orElse(null);
            if(optional!=null){
                optional.getUser().setBusiness(null);
                BusinessDTO objBusinessDTO=this.mapper.map(optional,BusinessDTO.class);
                
                return new ResponseEntity<BusinessDTO>(objBusinessDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
                throw objExeption;
    }

    /**
     * findByMerchantId function to retrieve Business by merchantId.
     *
     * @param  merchantId  the ID of the merchant to search for
     * @return             ResponseEntity with BusinessDTO if found, else throw exception
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findByMerchantId(String merchantId) {
        if(merchantId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findOneByMerchantId(merchantId);
            if(optional.isPresent()){
                BusinessDTO objBusinessDTO=this.mapper.map(optional.get(),BusinessDTO.class);
                
                return new ResponseEntity<BusinessDTO>(objBusinessDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con merchantId "+merchantId+" no existe en la Base de datos");
                throw objExeption;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTerminals(Long businessId) {
        List<TerminalDTO> listTerminalDTO=new ArrayList<>();
        if(businessId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findById(businessId);
            if(optional.isPresent()){
                if(optional.get().getTerminals()!=null)
                    listTerminalDTO=this.mapper.map(optional.get().getTerminals(), new TypeToken<List<TerminalDTO>>(){}.getType());
                
                return new ResponseEntity<List<TerminalDTO>>(listTerminalDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
                throw objExeption;
    }

    @Override
    public ResponseEntity<?> getCategories(Long businessId) {
        List<CategoryDTO> listCategoryDTO=new ArrayList<>();
        if(businessId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findById(businessId);
            if(optional.isPresent()){
                if(optional.get().getCategories()!=null)
                    listCategoryDTO=this.mapper.map(optional.get().getCategories(),new TypeToken<List<CategoryDTO>>(){}.getType());
                return new ResponseEntity<List<CategoryDTO>>(listCategoryDTO,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
                throw objExeption;
    }

    /**
     * Update the enable status of a business.
     *
     * @param  businessId  the ID of the business to update
     * @param  enable      the new enable status
     * @return             ResponseEntity with a boolean indicating success or failure
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateEnable(Long businessId, boolean enable) {
        
        if(businessId!=null){
            Optional<Business> optional= this.serviceDBBusiness.findById(businessId);
            if(optional.isPresent()){
                this.serviceDBBusiness.updateEnable(businessId, enable);
                return new ResponseEntity<Boolean>(true,HttpStatus.OK);
            }
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
                throw objExeption;
    }
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll() {
        List<BusinessDTO> listBusinessDTO=new ArrayList<>();
        Iterable<Business> listBusiness=this.serviceDBBusiness.findAll();
        listBusiness.forEach(objBusiness->{
            listBusinessDTO.add(this.mapper.map(objBusiness, BusinessDTO.class));
        });
        return new ResponseEntity<List<BusinessDTO>>(listBusinessDTO,HttpStatus.OK);
    }
    /**
     * Retrieves the activations for a given month.
     *
     * @param  month  the month for which activations are to be retrieved
     * @return        a ResponseEntity containing the activations for the given month
     */
    @Override
@Transactional(readOnly = true)
public ResponseEntity<?> getActivations(Instant starDate, Instant endDate) {
    // ── NEW: Activation dashboard DTO response ──────────────────
    // Purpose : Builds activation dashboard metrics without using HashMap responses
    // Depends on : BusinessRepository.findAllByRegistrations, TerminalRepository.findAllByActivations, Terminal fields
    // Does NOT modify : repository contracts, entity fields, synchronous service contract

    List<Business> listBusiness = this.serviceDBBusiness.findAllByRegistrations(starDate, endDate);
    List<Terminal> listTerminal = this.serviceDBTerminal.findAllByActivations(starDate, endDate);

    Instant previousStartMonth = starDate
            .atZone(ZoneId.systemDefault())
            .minusMonths(1)
            .toInstant();

    Instant previousEndMonth = endDate
            .atZone(ZoneId.systemDefault())
            .minusMonths(1)
            .toInstant();

    List<Business> lastMonthlistBusiness = this.serviceDBBusiness.findAllByRegistrations(previousStartMonth, previousEndMonth);
    List<Terminal> lastMonthlistTerminal = this.serviceDBTerminal.findAllByActivations(previousStartMonth, previousEndMonth);

    List<RegistrationDto> registrations = listBusiness.stream()
            .map(this::toRegistrationDto)
            .toList();

    double totalSales = listTerminal.stream()
            .map(Terminal::getLastPaymentValue)
            .filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .sum();

    List<ActivationDto> activations = listTerminal.stream()
            .map(this::toActivationDto)
            .toList();

    Instant inactiveLimit = Instant.now().minus(30, ChronoUnit.DAYS);

    long activeTerminals = this.serviceDBTerminal.countCurrentlyActiveTerminals(inactiveLimit);
    long inactiveTerminals = this.serviceDBTerminal.countInactiveTerminals(inactiveLimit);
    long deactivatedTerminals = this.serviceDBTerminal.countDeactivatedTerminals();
    long totalTerminals = this.serviceDBTerminal.countAllTerminals();

    long totalRegistrations = registrations.size();
    long totalActivations = activations.size();
    long lastMonthRegistrations = lastMonthlistBusiness.size();
    long lastMonthActivations = lastMonthlistTerminal.size();

    StatusDistributionDto statusDistribution = new StatusDistributionDto(
            activeTerminals,
            inactiveTerminals,
            deactivatedTerminals,
            totalRegistrations,
            totalTerminals
    );

    StatusDistributionPercentageDto statusDistributionPercentage = buildStatusDistributionPercentage(statusDistribution);

    ActivationDashboardResponseDto response = new ActivationDashboardResponseDto(
            totalRegistrations,
            totalActivations,
            lastMonthRegistrations,
            lastMonthActivations,
            totalSales,
            calculateVariationPercentage(totalActivations, lastMonthActivations),
            calculateVariationPercentage(totalRegistrations, lastMonthRegistrations),
            activeTerminals,
            inactiveTerminals,
            deactivatedTerminals,
            totalTerminals,
            registrations,
            activations,
            buildDailyTrend(listBusiness, listTerminal),
            statusDistribution,
            statusDistributionPercentage
    );

    return new ResponseEntity<>(response, HttpStatus.OK);
}
    
    @Override
    public ResponseEntity<?> getMonthActivations() {
        // TODO: FALTA IMPLEMENTAR
        throw new UnsupportedOperationException("Unimplemented method 'getMonthActivations'");
    }
    /**
     * Deletes the logo of a business and returns the updated business DTO.
     *
     * @param  businessId  the ID of the business whose logo is to be deleted
     * @return              a ResponseEntity containing the updated business DTO
     * @throws EntidadNoExisteException  if the business with the given ID does not exist in the database
     */
    @Override
    @Transactional
    public ResponseEntity<?> deleteLogo(Long businessId) {
        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if(business==null){
                throw new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
        }
        if(business.getLogo()!=null){
            try{
                fileService.deleteImage(business.getLogo());
            }catch(EntidadNoExisteException    e){
    
            }
            
            business.setLogo(null);
            business=serviceDBBusiness.save(business);
        }
        
        business.getUser().setBusiness(null);
            BusinessDTO businessDTO=this.mapper.map(business, BusinessDTO.class);
        return new ResponseEntity<BusinessDTO>(businessDTO,HttpStatus.OK);
    }   

    
    /**
     * Deletes the logo of a business with the given business ID.
     *
     * @param  businessId   the ID of the business
     * @return              a ResponseEntity containing the updated BusinessDTO and HTTP status OK
     * @throws EntidadNoExisteException if the business with the given ID does not exist in the database
     */
    @Override
    @Transactional
    public ResponseEntity<?> deleteLogoATH(Long businessId) {
        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if(business==null){
                throw new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
        }
        if(business.getLogoAth()!=null){
            fileService.deleteImage(business.getLogoAth());
            business.setLogoAth(null);
            business=serviceDBBusiness.save(business);
        }
        
        business.getUser().setBusiness(null);
            BusinessDTO businessDTO=this.mapper.map(business, BusinessDTO.class);
        return new ResponseEntity<BusinessDTO>(businessDTO,HttpStatus.OK);
    }
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findByTerminalId(String terminalId) {
        if(terminalId!=null){
            Optional<Terminal> optional= this.serviceDBTerminal.findById(terminalId);
            if(optional.isPresent()){
                BusinessDTO objBusinessDTO=this.mapper.map(optional.get().getBusiness(),BusinessDTO.class);
                return new ResponseEntity<BusinessDTO>(objBusinessDTO,HttpStatus.OK);
            }   
        }
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Terminal con terminalId "+terminalId+" no existe en la Base de datos");
                throw objExeption;
    }
    // ── NEW: Registration DTO mapper ──────────────────
// Purpose : Converts Business entity to dashboard registration DTO
// Depends on : Business fields businessId, registerDate, name, address, businessPhoneNumber, user
// Does NOT modify : Business entity or repository contracts
private RegistrationDto toRegistrationDto(Business business) {
    return new RegistrationDto(
            business.getBusinessId(),
            business.getRegisterDate(),
            business.getName(),
            business.getAddress() != null ? business.getAddress().getCity() : null,
            business.getBusinessPhoneNumber(),
            "NUEVO REGISTRO",
            business.isEnable() ? "ACTIVO" : "DESACTIVADO",
            business.getMerchantId()
    );
}

// ── NEW: Activation DTO mapper ──────────────────
// Purpose : Converts Terminal entity to dashboard activation DTO
// Depends on : Terminal fields terminalId, serial, name, business, service, registerDate, lastPayment, enable, isPrincipal
// Does NOT modify : Terminal entity or repository contracts
private ActivationDto toActivationDto(Terminal terminal) {
    Instant activationDate = resolveActivationDate(terminal);

    return new ActivationDto(
            terminal.getTerminalId(),
            terminal.getSerial(),
            activationDate,
            terminal.getBusiness() != null ? terminal.getBusiness().getName() : null,
            terminal.getName(),
            buildActivationType(terminal),
            buildTerminalStatus(terminal),
            terminal.getService() != null ? terminal.getService().getServiceValue() : null,
            terminal.getBusiness() != null && terminal.getBusiness().getUser() != null
                    ? terminal.getBusiness().getUser().getName()
                    : null
    );
}

// ── NEW: Activation date resolver ──────────────────
// Purpose : Selects payment date first and registration date as fallback
// Depends on : Terminal.lastPayment, Terminal.registerDate
// Does NOT modify : Terminal fields
private Instant resolveActivationDate(Terminal terminal) {
    if (terminal.getLastPayment() != null) {
        return terminal.getLastPayment();
    }

    return terminal.getRegisterDate();
}

// ── NEW: Activation type builder ──────────────────
// Purpose : Builds activation or renewal label for terminal
// Depends on : Terminal.registerDate, Terminal.lastPayment, Terminal.isPrincipal
// Does NOT modify : Terminal fields
private String buildActivationType(Terminal terminal) {
    boolean principal = terminal.isPrincipal();

    if (terminal.getRegisterDate() == null || terminal.getLastPayment() == null) {
        return principal ? "ACTIVACIÓN TERMINAL PRINCIPAL" : "ACTIVACIÓN TERMINAL ADICIONAL";
    }

    boolean sameMonth = YearMonth.from(terminal.getRegisterDate().atZone(ZoneOffset.UTC))
            .equals(YearMonth.from(terminal.getLastPayment().atZone(ZoneOffset.UTC)));

    if (sameMonth) {
        return principal ? "ACTIVACIÓN TERMINAL PRINCIPAL" : "ACTIVACIÓN TERMINAL ADICIONAL";
    }

    return principal ? "RENOVACIÓN TERMINAL PRINCIPAL" : "RENOVACIÓN TERMINAL ADICIONAL";
}

// ── NEW: Terminal status builder ──────────────────
// Purpose : Classifies terminal as active, inactive or deactivated
// Depends on : Terminal.enable, Terminal.lastTransmision
// Does NOT modify : Terminal fields
private String buildTerminalStatus(Terminal terminal) {
    if (!terminal.isEnable()) {
        return "DESACTIVADA";
    }

    Instant inactiveLimit = Instant.now().minus(30, ChronoUnit.DAYS);

    if (terminal.getLastTransmision() == null || !terminal.getLastTransmision().isAfter(inactiveLimit)) {
        return "INACTIVA";
    }

    return "ACTIVA";
}

// ── NEW: Daily trend builder ──────────────────
// Purpose : Groups registrations and activations by day
// Depends on : Business.registerDate, Terminal.lastPayment, Terminal.registerDate
// Does NOT modify : Business or Terminal entities
private List<DailyTrendDto> buildDailyTrend(List<Business> businesses, List<Terminal> terminals) {
    Map<LocalDate, Long> registrationsByDate = businesses.stream()
            .filter(business -> business.getRegisterDate() != null)
            .collect(Collectors.groupingBy(
                    business -> toLocalDate(business.getRegisterDate()),
                    TreeMap::new,
                    Collectors.counting()
            ));

    Map<LocalDate, Long> activationsByDate = terminals.stream()
            .map(this::resolveActivationDate)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                    this::toLocalDate,
                    TreeMap::new,
                    Collectors.counting()
            ));

    Set<LocalDate> dates = new TreeSet<>();
    dates.addAll(registrationsByDate.keySet());
    dates.addAll(activationsByDate.keySet());

    return dates.stream()
            .map(date -> new DailyTrendDto(
                    date,
                    activationsByDate.getOrDefault(date, 0L),
                    registrationsByDate.getOrDefault(date, 0L)
            ))
            .toList();
}

// ── NEW: Local date converter ──────────────────
// Purpose : Converts Instant to LocalDate using system timezone
// Depends on : system default timezone
// Does NOT modify : input date
private LocalDate toLocalDate(Instant date) {
    return date.atZone(ZoneId.systemDefault()).toLocalDate();
}

// ── NEW: Variation percentage calculator ──────────────────
// Purpose : Calculates percentage variation against previous period
// Depends on : current and previous totals
// Does NOT modify : any state
private double calculateVariationPercentage(long currentValue, long previousValue) {
    if (previousValue == 0) {
        return currentValue == 0 ? 0.0 : 100.0;
    }

    return ((double) (currentValue - previousValue) / previousValue) * 100;
}

// ── NEW: Status percentage builder ──────────────────
// Purpose : Calculates percentage distribution for dashboard status counters
// Depends on : StatusDistributionDto values
// Does NOT modify : any state
private StatusDistributionPercentageDto buildStatusDistributionPercentage(StatusDistributionDto distribution) {
    long total = distribution.total();

    if (total == 0) {
        return new StatusDistributionPercentageDto(0.0, 0.0, 0.0, 0.0);
    }

    return new StatusDistributionPercentageDto(
            calculatePercentage(distribution.active(), total),
            calculatePercentage(distribution.inactive30Days(), total),
            calculatePercentage(distribution.deactivated(), total),
            calculatePercentage(distribution.registrations(), total)
    );
}

// ── NEW: Percentage calculator ──────────────────
// Purpose : Calculates percentage from part and total values
// Depends on : numeric arguments only
// Does NOT modify : any state
private double calculatePercentage(long value, long total) {
    if (total == 0) {
        return 0.0;
    }

    return ((double) value / total) * 100;
}
}
