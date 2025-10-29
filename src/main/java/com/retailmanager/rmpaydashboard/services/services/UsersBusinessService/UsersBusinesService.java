package com.retailmanager.rmpaydashboard.services.services.UsersBusinessService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.enums.EmployeeRole;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.DataInconsistencyException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.InvalidDateOrTime;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.UserDisabled;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.EmployeeBusinessConfigDownload;
import com.retailmanager.rmpaydashboard.models.EntryExit;
import com.retailmanager.rmpaydashboard.models.Permission;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.models.UserBusiness_Category;
import com.retailmanager.rmpaydashboard.models.UserBusiness_Product;
import com.retailmanager.rmpaydashboard.models.UserPermission;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;
import com.retailmanager.rmpaydashboard.repositories.AvailableSchedulesRepository;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.EmployeeBusinessConfigDownloadRepository;
import com.retailmanager.rmpaydashboard.repositories.EntryExitRepository;
import com.retailmanager.rmpaydashboard.repositories.PermisionRepository;
import com.retailmanager.rmpaydashboard.repositories.ScheduleCalendarRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.repositories.UserBusiness_CategoryRepository;
import com.retailmanager.rmpaydashboard.repositories.UserBusiness_ProductRepository;
import com.retailmanager.rmpaydashboard.repositories.UserPermissionRepository;
import com.retailmanager.rmpaydashboard.repositories.UsersAppRepository;
import com.retailmanager.rmpaydashboard.security.TokenUtils;
import com.retailmanager.rmpaydashboard.services.DTO.BusinessConfigurationDTO;
import com.retailmanager.rmpaydashboard.services.DTO.CategoryDTO;
import com.retailmanager.rmpaydashboard.services.DTO.EmployeeAuthentication;
import com.retailmanager.rmpaydashboard.services.DTO.EntryExitDTO;
import com.retailmanager.rmpaydashboard.services.DTO.PermissionDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;
import com.retailmanager.rmpaydashboard.services.DTO.UsersBusinessDTO;

@Service
public class UsersBusinesService implements IUsersBusinessService{
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private TerminalRepository serviceDBTerminal;

    @Autowired
    private UsersAppRepository usersAppDBService;
    @Autowired
    private EntryExitRepository entryExitDBService;
    @Autowired
    private UserBusiness_ProductRepository ubpServices;
    @Autowired
    private UserBusiness_CategoryRepository ubcServices;
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private PermisionRepository serviceDBUPermission;
    @Autowired
    private UserPermissionRepository serviceDBUserPermission;
    @Autowired
    private EmployeeBusinessConfigDownloadRepository employeeBusinessConfig;
    @Autowired
    private ScheduleCalendarRepository serviceDBScheduleCalendar;
    @Autowired
    private AvailableSchedulesRepository serviceDBAvailableSchedules;
    
    /**
     * Save the UsersBusinessDTO to the database.
     *
     * @param  prmUsersBusiness	the UsersBusinessDTO to be saved
     * @return         			ResponseEntity containing the saved UsersBusinessDTO or a BAD_REQUEST status
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(UsersBusinessDTO prmUsersBusiness) {
        Long businessId=prmUsersBusiness.getIdBusines();
        Business business = null;
        if(businessId!=null) {
            business=this.serviceDBBusiness.findById(businessId).orElse(null);
            Optional<UsersBusiness> existingUser = this.usersAppDBService.findByPasswordAndBusiness(prmUsersBusiness.getPassword(), businessId);
            if(existingUser.isPresent()){
                throw new EntidadYaExisteException("El Empleado con password "+prmUsersBusiness.getPassword()+" ya existe en la Base de datos para el Negocio "+businessId);
            }
            UsersBusiness usersBusiness = this.mapper.map(prmUsersBusiness, UsersBusiness.class);
            usersBusiness.setUserPermissions(new ArrayList<>());
            
            if(EmployeeRole.fromId(prmUsersBusiness.getRoleId())==null){
                usersBusiness.setRoleId(EmployeeRole.USER.getId()); //Default role is 2
            }else{
                usersBusiness.setRoleId(prmUsersBusiness.getRoleId());
            }
            if(business!=null) {
                usersBusiness.setBusiness(business);
                for(Long idPermission:prmUsersBusiness.getActivesPermissions()){
                    
                    Permission permission = this.serviceDBUPermission.findById(idPermission).orElse(null);
                    if(permission==null){
                        throw new EntidadNoExisteException("El Permission con permissionId "+idPermission+" no existe en la Base de datos");
                    }
                    UserPermission userPermission = new UserPermission();
                    userPermission.setPermission(permission);
                    userPermission.setUserBusiness(usersBusiness);
                    userPermission.setEnable(true);
                    usersBusiness.getUserPermissions().add(userPermission);

                }
                usersBusiness.setCreatedAt(LocalDateTime.now());
                usersBusiness.setUpdatedAt(LocalDateTime.now());
                usersBusiness=this.usersAppDBService.save(usersBusiness);
                return new ResponseEntity<UsersBusinessDTO>(this.mapper.map(usersBusiness, UsersBusinessDTO.class), HttpStatus.CREATED);
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+businessId+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(Long userBusinessId, UsersBusinessDTO prmUsersBusiness) {
        if(userBusinessId!=null) {
            UsersBusiness usersBusiness = this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(usersBusiness!=null) {
                Optional<UsersBusiness> existingUser = this.usersAppDBService.findByPasswordAndBusiness(prmUsersBusiness.getPassword(), prmUsersBusiness.getIdBusines());
                //System.out.println("Existing User: " + existingUser.get().getUserBusinessId());
                if(existingUser != null && existingUser.isPresent() && existingUser.get().getUserBusinessId().equals(userBusinessId)==false){
                    throw new EntidadYaExisteException("El Empleado con password "+prmUsersBusiness.getPassword()+" ya existe en la Base de datos para el Negocio "+prmUsersBusiness.getIdBusines());
                }
                usersBusiness.setUsername(prmUsersBusiness.getUsername());
                usersBusiness.setPassword(prmUsersBusiness.getPassword());
                usersBusiness.setEnable(prmUsersBusiness.getEnable());
                usersBusiness.setCostHour(prmUsersBusiness.getCostHour());
                usersBusiness.setUpdatedAt(LocalDateTime.now());
                usersBusiness.setRoleId(prmUsersBusiness.getRoleId());
                usersBusiness.getUserPermissions().clear();
                if(EmployeeRole.fromId(usersBusiness.getRoleId())==null){
                    usersBusiness.setRoleId(EmployeeRole.USER.getId()); //Default role is 2
                }
                for(Long idPermission:prmUsersBusiness.getActivesPermissions()){
                    
                    Permission permission = this.serviceDBUPermission.findById(idPermission).orElse(null);
                    if(permission==null){
                        throw new EntidadNoExisteException("El Permission con permissionId "+idPermission+" no existe en la Base de datos");
                    }
                    UserPermission userPermission = new UserPermission();
                    userPermission.setPermission(permission);
                    userPermission.setUserBusiness(usersBusiness);
                    usersBusiness.getUserPermissions().add(userPermission);
                } 
                usersBusiness=this.usersAppDBService.save(usersBusiness);
                this.usersAppDBService.updateAllDownloadExceptMe(userBusinessId, false);
                prmUsersBusiness=this.mapper.map(usersBusiness, UsersBusinessDTO.class);
                prmUsersBusiness.setActivesPermissions(new ArrayList<>());
                for (UserPermission iterable_element : usersBusiness.getUserPermissions()) {
                    prmUsersBusiness.getActivesPermissions().add(iterable_element.getPermission().getPermissionId());
                }

                return new ResponseEntity<UsersBusinessDTO>(prmUsersBusiness, HttpStatus.OK);
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Delete a userBusiness by userBusinessId.
     *
     * @param  userBusinessId  the ID of the userBusiness to be deleted
     * @return                 true if the userBusiness is deleted, false otherwise
     */
    @Override
    @Transactional
    public boolean delete(Long userBusinessId) {
        if(userBusinessId!=null) {
            this.serviceDBScheduleCalendar.findByEmployeeId(userBusinessId).forEach(schedule -> {
                    this.serviceDBScheduleCalendar.delete(schedule);
                });
            this.serviceDBAvailableSchedules.findByEmployeeId(userBusinessId).forEach(schedule -> {
                    this.serviceDBAvailableSchedules.delete(schedule);
            });
            UsersBusiness usersBusiness = this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(usersBusiness!=null) {
                
                this.usersAppDBService.delete(usersBusiness);
                return true;
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return false;
    }

    /**
     * Finds a user by their business ID.
     *
     * @param  userBusinessId	the ID of the user's business
     * @return         		the ResponseEntity containing the user's business details or a BAD_REQUEST response
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long userBusinessId) {
        if(userBusinessId!=null) {
            UsersBusiness usersBusiness = this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(usersBusiness!=null) {
                UsersBusinessDTO user=this.mapper.map(usersBusiness, UsersBusinessDTO.class);
                user.setActivesPermissions(new ArrayList<>());
                for (UserPermission userPermission : usersBusiness.getUserPermissions()) {
                    user.getActivesPermissions().add(userPermission.getPermission().getPermissionId());
                }
                return new ResponseEntity<UsersBusinessDTO>(user, HttpStatus.OK);
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Updates the enable status for a user business.
     *
     * @param  userBusinessId   the ID of the user's business
     * @param  enable           the new enable status
     * @return                 a ResponseEntity with the updated UsersBusinessDTO and HttpStatus
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateEnable(Long userBusinessId, boolean enable) {
        if(userBusinessId!=null) {
            UsersBusiness usersBusiness = this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(usersBusiness!=null) {
                usersBusiness.setEnable(enable);
                usersBusiness.setUpdatedAt(LocalDateTime.now());
                usersBusiness=this.usersAppDBService.save(usersBusiness);
                return new ResponseEntity<Boolean>(true, HttpStatus.OK);
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Find and return users by business ID.
     *
     * @param  idBusiness   the ID of the business
     * @return              the ResponseEntity containing the list of users business DTOs
     */
    @Override
    public ResponseEntity<?> findByBusiness(Long idBusiness) {
        if(idBusiness!=null) {
            Business business = this.serviceDBBusiness.findById(idBusiness).orElse(null);
            if(business!=null) {
                List<UsersBusiness> usersBusiness = this.usersAppDBService.findByBusiness(business);
                List<UsersBusinessDTO> usersBusinessDTO = this.mapper.map(usersBusiness,  new TypeToken<List<UsersBusinessDTO>>(){}.getType());
                for(int i=0;i<usersBusiness.size();i++){
                    usersBusinessDTO.get(i).setActivesPermissions(new ArrayList<>()   );{
                    for(UserPermission up:usersBusiness.get(i).getUserPermissions()){
                        usersBusinessDTO.get(i).getActivesPermissions().add(up.getPermission().getPermissionId());
                    }
                    }
                }
                return new ResponseEntity<List<UsersBusinessDTO>>(usersBusinessDTO, HttpStatus.OK);
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El Business con businessId "+idBusiness+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Updates the permission of a user.
     *
     * @param  idUser           the ID of the user
     * @param  idPermission     the ID of the permission
     * @param  enable           the enable status of the permission
     * @return                  a ResponseEntity with a Boolean value and HttpStatus
     *                          HttpStatus.OK if the permission is updated successfully
     *                          HttpStatus.BAD_REQUEST if the user or permission does not exist
     * @throws EntidadNoExisteException if the permission does not exist in the database
     */
    @Override
    @Transactional
    public ResponseEntity<?> updatePermission(Long idUser, Long idPermission, boolean enable) {
        if(idUser!=null) {
            UsersBusiness usersBusiness = this.usersAppDBService.findById(idUser).orElse(null);
            if(usersBusiness!=null) {
                for (UserPermission userPermission : usersBusiness.getUserPermissions()) {
                    if (userPermission.getPermission().getPermissionId()==idPermission) {
                        if(enable){
                            userPermission.setEnable(true);
                        }else{
                            usersBusiness.getUserPermissions().remove(userPermission);
                            this.usersAppDBService.save(usersBusiness);
                            //this.serviceDBUserPermission.delete(userPermission);
                        }
                        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
                    }
                }
                if(enable){
                    Permission permission = this.serviceDBUPermission.findById(idPermission).orElse(null);
                    if(permission==null){
                        throw new EntidadNoExisteException("El Permission con permissionId "+idPermission+" no existe en la Base de datos");

                    }
                    UserPermission userPermission = new UserPermission();
                    userPermission.setPermission(permission);
                    userPermission.setUserBusiness(usersBusiness);
                    userPermission.setEnable(true);
                    this.serviceDBUserPermission.save(userPermission);
                    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
                }else{
                    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
                }
                
            }else{
                EntidadNoExisteException objExeption = new EntidadNoExisteException("El UsersBusiness con userBusinessId "+idUser+" no existe en la Base de datos");
                throw objExeption;
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * Retrieves all permissions from the database and returns them as a list of PermissionDTO objects.
     *
     * @return         	A ResponseEntity object containing a list of PermissionDTO objects and an HTTP status code.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllPermissions() {
        Iterable<Permission> permissions = this.serviceDBUPermission.findAll();
        List<PermissionDTO> permissionsDTO = this.mapper.map(permissions,  new TypeToken<List<PermissionDTO>>(){}.getType());
        return new ResponseEntity<List<PermissionDTO>>(permissionsDTO, HttpStatus.OK);
    }

    /**
     * Retrieves the list of products associated with the given user business ID.
     *
     * @param  userBusinessId  the ID of the user business
     * @return                 the ResponseEntity containing the list of ProductDTO objects
     *                         or an INTERNAL_SERVER_ERROR status if an exception occurs
     * @throws EntidadNoExisteException if the UsersBusiness with the given ID does not exist in the database
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProducts(Long userBusinessId) {
        List<ProductDTO> productDTOs=new ArrayList<>();
        
            UsersBusiness objUser=this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(objUser==null){
                throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
            }
            try {    
            List<UserBusiness_Product> ubpList=this.ubpServices.findByObjUserAndDownload(objUser,false);
            for(UserBusiness_Product ubp:ubpList){
                productDTOs.add(this.mapper.map(ubp.getObjProduct(), ProductDTO.class));
                //ubp.setDownload(true);
                //this.ubpServices.save(ubp);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        

        return new ResponseEntity<>(productDTOs,HttpStatus.OK);
    }

        /**
     * Actualiza el estado de descarga de productos para un usuario de negocio.
     * 
     * @param userBusinessId El ID del usuario de negocio.
     * @param product_ids    La lista de IDs de productos que se van a marcar como descargados.
     * @return               ResponseEntity con el estado de la operación.
     */
    @Override
    @Transactional
    public ResponseEntity<?> updateDownloadProducts(Long userBusinessId,List<Long> product_ids) {
        try{
            for(Long product_id:product_ids){
                this.ubpServices.updateDownload(product_id, userBusinessId, true);
            }
        }catch(Exception e){
            return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<>(true,HttpStatus.OK);
    }

    /**
     * Retrieves the list of categories associated with the given user business ID.
     *
     * @param  userBusinessId  the ID of the user business
     * @return                 the ResponseEntity containing the list of CategoryDTO objects
     *                         or an INTERNAL_SERVER_ERROR status if an exception occurs
     * @throws EntidadNoExisteException if the UsersBusiness with the given ID does not exist in the database
     */
        @Override
        @Transactional
        public ResponseEntity<?> getCategory(Long userBusinessId) {
            List<CategoryDTO> categoryDTO=new ArrayList<>();
        
            UsersBusiness objUser=this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(objUser==null){
                throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
            }
            try {    
            List<UserBusiness_Category> ubpList=this.ubcServices.findByObjUserAndDownload(objUser,false);
            for(UserBusiness_Category ubp:ubpList){
                categoryDTO.add(this.mapper.map(ubp.getObjCategory(), CategoryDTO.class));
                //ubp.setDownload(true);
                //this.ubpServices.save(ubp);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        

        return new ResponseEntity<>(categoryDTO,HttpStatus.OK);
        }

    /**
     * Retrieves the list of UsersBusinessDTO objects associated with the given userBusinessId.
     *
     * @param  userBusinessId  the ID of the user business
     * @return                 the ResponseEntity containing the list of UsersBusinessDTO objects
     */
        @Override
        @Transactional
        public ResponseEntity<?> getUsersBusiness(Long userBusinessId) {
            List<UsersBusinessDTO> ubpListDTO=new ArrayList<>();
            UsersBusiness objUser=this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(objUser==null){
                throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
            }
            try { 
                if(!objUser.getDownload()){
                    List<UsersBusiness> ubpList=objUser.getBusiness().getUsersBusiness();
                    ubpListDTO= this.mapper.map(ubpList, new TypeToken<List<UsersBusinessDTO>>(){}.getType());
                }   
            
            
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        

        return new ResponseEntity<>(ubpListDTO,HttpStatus.OK);
        }

    /**
     * Updates the download status of a category for a given user business.
     *
     * @param  userBusinessId  the ID of the user business
     * @param  category_ids    the list of category IDs to update
     * @return                 a ResponseEntity indicating the success or failure of the operation
     */
        @Override
        @Transactional
        public ResponseEntity<?> updateDownloadCategory(Long userBusinessId, List<Long> category_ids) {
            try{
                for(Long category_id:category_ids){
                    this.ubcServices.updateDownload(category_id, userBusinessId, true);
                }
            }catch(Exception e){
                return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            return new ResponseEntity<>(true,HttpStatus.OK);
        }

    /**
     * Updates the download status of a user business.
     *
     * @param  userBusinessId  the ID of the user business
     * @return                 a ResponseEntity indicating the success or failure of the operation
     * @throws EntidadNoExisteException  if the user business with the given ID does not exist in the database
     */
        @Override
        @Transactional
        public ResponseEntity<?> updateDownloadUserBusiness(Long userBusinessId) {
            UsersBusiness objUser=this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(objUser==null){
                throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
            }
            try {    
                objUser.setDownload(true);
                this.usersAppDBService.save(objUser);
            
            } catch (Exception e) {
                return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        

        return new ResponseEntity<>(true,HttpStatus.OK);
        }

/**
     * Registers an entry or exit for a userBusiness , i.e. registers a new entry or exit for a employee.
     *
     * @param  prmEntryExit  the EntryExitDTO object containing the entry details
     * @return               a ResponseEntity with a boolean value indicating success and the HTTP status code
     * @throws EntidadNoExisteException if the UsersBusiness with the given userId does not exist in the database
     */       
     @Override
    @Transactional
    public ResponseEntity<?> registerExit(String authToken,EmployeeAuthentication prmEmployeeAuthentication) {
        Terminal objTerminal=null;
        Business objBusiness=null;
        String terminalId=TokenUtils.getTerminalId(authToken);
        if(terminalId!=null){
            objTerminal=this.serviceDBTerminal.findById(terminalId).orElse(null);
            if(objTerminal!=null){
                objBusiness=objTerminal.getBusiness();
            }else{
                throw new EntidadNoExisteException("Terminal con ID "+terminalId+" no existe en la Base de datos");
            }
            
        }else{
            if(prmEmployeeAuthentication.getBusinessId()==null){
                HashMap<String,String> extra = new HashMap<>();
                extra.put("businessId", "businessId: No puede ser null o generen un toeken que incluya el terminalId");
                return new ResponseEntity<>(extra,HttpStatus.BAD_REQUEST);
            }
            objBusiness=this.serviceDBBusiness.findById(prmEmployeeAuthentication.getBusinessId()).orElse(null);
            if(objBusiness==null ){
                throw new EntidadNoExisteException("El Negocio con businessId "+prmEmployeeAuthentication.getBusinessId()+" no existe en la Base de datos");
            }
        }
        List<UsersBusiness> objUser=this.usersAppDBService.findByPasswordAndBusinessId(String.valueOf(prmEmployeeAuthentication.getPassword()), objBusiness.getBusinessId());
        if(objUser==null || objUser.isEmpty()){
            throw new EntidadNoExisteException("El Empleado con password "+prmEmployeeAuthentication.getPassword()+" no existe en la Base de datos");
        }
        if(objUser.get(0).getEnable()==false){
            throw new UserDisabled("El Empleado con password "+prmEmployeeAuthentication.getPassword()+" esta deshabilitado");
        }
        
        EntryExit objEntryExit=new EntryExit();
        objEntryExit.setEntry(false);
        objEntryExit.setDate(LocalDate.now());
        objEntryExit.setHour(LocalTime.now());
        Pageable pageable = PageRequest.of(0, 10);
        float hoursWorket=0;
        List<EntryExit> listActivity=this.entryExitDBService.getLastActivity(objUser.get(0).getUserBusinessId(),pageable);
        if(listActivity!=null && !listActivity.isEmpty()){
            if(listActivity.get(0).getEntry()){
                if(objEntryExit.getDate().isBefore(listActivity.get(0).getDate())){
                    throw new InvalidDateOrTime("La fecha de entrada no puede ser menor a la registrada en el último ponche");
                }
                if(objEntryExit.getDate().isEqual(listActivity.get(0).getDate()) && objEntryExit.getHour().isBefore(listActivity.get(0).getHour())){
                    throw new InvalidDateOrTime("La hora de entrada no puede ser menor a la registrada en el último ponche");
                }
                hoursWorket=calculateDuration(listActivity.get(0).getDate(), listActivity.get(0).getHour(), objEntryExit.getDate(), objEntryExit.getHour()).toHours();
                objEntryExit.setHoursWorked(hoursWorket);
                objEntryExit.setTotalWorkCost(hoursWorket*objUser.get(0).getCostHour());
            }
        }
        objEntryExit.setUserBusiness(objUser.get(0));
        objEntryExit=this.entryExitDBService.save(objEntryExit);
        EntryExitDTO prmEntryExit=this.mapper.map(objEntryExit, EntryExitDTO.class);
        prmEntryExit.setUserId(objUser.get(0).getUserBusinessId());
        prmEntryExit.setName(objUser.get(0).getUsername());
        prmEntryExit.setHour(prmEntryExit.getHour().withNano(0));
        prmEntryExit.setHoursWorked(hoursWorket);
        return new ResponseEntity<>(prmEntryExit,HttpStatus.CREATED);
    }
    @Override
    @Transactional
    public ResponseEntity<?> registerEntry(String authToken,EmployeeAuthentication prmEmployeeAuthentication) {
        Terminal objTerminal=null;
        Business objBusiness=null;
        String terminalId=TokenUtils.getTerminalId(authToken);
        if(terminalId!=null){
            objTerminal=this.serviceDBTerminal.findById(terminalId).orElse(null);
            if(objTerminal!=null){
                objBusiness=objTerminal.getBusiness();
            }else{
                throw new EntidadNoExisteException("Terminal con ID "+terminalId+" no existe en la Base de datos");
            }
            
        }else{
            if(prmEmployeeAuthentication.getBusinessId()==null){
                HashMap<String,String> extra = new HashMap<>();
                extra.put("businessId", "businessId: No puede ser null o generen un toeken que incluya el terminalId");
                return new ResponseEntity<>(extra,HttpStatus.BAD_REQUEST);
            }
            objBusiness=this.serviceDBBusiness.findById(prmEmployeeAuthentication.getBusinessId()).orElse(null);
            if(objBusiness==null ){
                throw new EntidadNoExisteException("El Negocio con businessId "+prmEmployeeAuthentication.getBusinessId()+" no existe en la Base de datos");
            }
        }
        List<UsersBusiness> objUser=this.usersAppDBService.findByPasswordAndBusinessId(String.valueOf(prmEmployeeAuthentication.getPassword()), objBusiness.getBusinessId());
        if(objUser==null || objUser.isEmpty()){
            throw new EntidadNoExisteException("El Empleado con password "+prmEmployeeAuthentication.getPassword()+" no existe en la Base de datos para el Negocio "+objBusiness.getBusinessId());
        }
        if(terminalId!=null && objUser.get(0).getBusiness().getBusinessId()!=objBusiness.getBusinessId()){
            throw new DataInconsistencyException("El Empleado con id "+objUser.get(0).getUserBusinessId()+" y el terminal con id: "+terminalId+" no pertenecen al mismo negocio");
        }
        if(objUser.get(0).getEnable()==false){
            throw new UserDisabled("El Empleado con password "+prmEmployeeAuthentication.getPassword()+" esta deshabilitado");
        }
        EntryExit objEntryExit=new EntryExit();
        objEntryExit.setDate(LocalDate.now());
        objEntryExit.setHour(LocalTime.now());
        objEntryExit.setUserBusiness(objUser.get(0));
        Pageable pageable = PageRequest.of(0, 10);
        float hoursWorket=0;
        List<EntryExit> listActivity=this.entryExitDBService.getLastActivity(objUser.get(0).getUserBusinessId(),pageable);
        if(listActivity==null || listActivity.isEmpty()){
            objEntryExit.setEntry(true);
        }else{
            if(objEntryExit.getDate().isBefore(listActivity.get(0).getDate())){
                throw new InvalidDateOrTime("La fecha de entrada no puede ser menor a la registrada en el último ponche");
            }
            if(objEntryExit.getDate().isEqual(listActivity.get(0).getDate()) && objEntryExit.getHour().isBefore(listActivity.get(0).getHour())){
                throw new InvalidDateOrTime("La hora de entrada no puede ser menor a la registrada en el último ponche");
            }
            if(listActivity.get(0).getEntry()){
                objEntryExit.setEntry(false);
                 hoursWorket=calculateDuration(listActivity.get(0).getDate(), listActivity.get(0).getHour(), objEntryExit.getDate(), objEntryExit.getHour()).toHours();
                objEntryExit.setHoursWorked(hoursWorket);
                objEntryExit.setTotalWorkCost(hoursWorket*objUser.get(0).getCostHour());
            }else{
                objEntryExit.setEntry(true);
            }
        }
        
        objEntryExit=this.entryExitDBService.save(objEntryExit);
        EntryExitDTO EntryExitDTO=this.mapper.map(objEntryExit, EntryExitDTO.class);
        EntryExitDTO.setUserId(objUser.get(0).getUserBusinessId());
        EntryExitDTO.setName(objUser.get(0).getUsername());
        EntryExitDTO.setHour(EntryExitDTO.getHour().withNano(0));
        EntryExitDTO.setHoursWorked(hoursWorket);
        return new ResponseEntity<>(EntryExitDTO,HttpStatus.CREATED);
    }
/**
     * Retrieves the last activity for a given user business ID.
     *
     * @param  prmUserBusinessId  the ID of the user business
     * @return                    a ResponseEntity containing the last activity details
     *                            or an EntidadNoExisteException if the user business or activity does not exist
     */
@Override
@Transactional
public ResponseEntity<?> getLastActivity(Long prmUserBusinessId) {
   
    UsersBusiness objUser=this.usersAppDBService.findById(prmUserBusinessId).orElse(null);
        if(objUser==null){
            throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+prmUserBusinessId+" no existe en la Base de datos");
        }
        Pageable pageable = PageRequest.of(0, 10);
        List<EntryExit> objEntryExit=this.entryExitDBService.getLastActivity(prmUserBusinessId,pageable);
        if(objEntryExit==null || objEntryExit.isEmpty()){
            throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+prmUserBusinessId+" no tiene actividad registrada");
        }
        EntryExitDTO objEntryExitDTO=this.mapper.map(objEntryExit.get(0), EntryExitDTO.class);
        objEntryExitDTO.setUserId(objUser.getUserBusinessId());
        objEntryExitDTO.setName(objUser.getUsername());
        objEntryExitDTO.setHour(objEntryExitDTO.getHour().withNano(0));
        return new ResponseEntity<>(objEntryExitDTO,HttpStatus.OK);
}
@Override
@Transactional
public ResponseEntity<?> deleteLastActivity(Long prmUserBusinessId) {
   
    UsersBusiness objUser=this.usersAppDBService.findById(prmUserBusinessId).orElse(null);
        if(objUser==null){
            throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+prmUserBusinessId+" no existe en la Base de datos");
        }
        Pageable pageable = PageRequest.of(0, 10);
        List<EntryExit> objEntryExit=this.entryExitDBService.getLastActivity(prmUserBusinessId,pageable);
        if(objEntryExit==null || objEntryExit.isEmpty()){
            throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+prmUserBusinessId+" no tiene actividad registrada");
        }
        this.entryExitDBService.delete(objEntryExit.get(0));
        
        return new ResponseEntity<>(true,HttpStatus.OK);
}

/**
 * Updates the entry and exit details for a specific activity.
 *
 * @param  activityId   the ID of the activity to update
 * @param  prmPonche    the EntryExitDTO object containing the updated details
 * @return              a ResponseEntity containing the updated entry and exit details
 */
@Override
@Transactional
public ResponseEntity<?> updatePonche(Long activityId, EntryExitDTO prmPonche) {
    EntryExit objPonche = this.entryExitDBService.findById(activityId).orElse(null);
    if(objPonche==null){
        throw new EntidadNoExisteException("La actividad con activityId "+activityId+" no existe en la Base de datos");
    }
    objPonche.setDate(prmPonche.getDate());
    objPonche.setHour(prmPonche.getHour());
    if(!prmPonche.getEntry()){
        Pageable pageable = PageRequest.of(0, 10);
        List<EntryExit> listActivity=this.entryExitDBService.getPreviousEntry(objPonche.getId(),objPonche.getUserBusiness().getUserBusinessId(),pageable);
        float hoursWorket=calculateDuration(listActivity.get(0).getDate(), listActivity.get(0).getHour(), objPonche.getDate(), objPonche.getHour()).toHours();
        objPonche.setHoursWorked(hoursWorket);
        objPonche.setTotalWorkCost(hoursWorket*objPonche.getUserBusiness().getCostHour());
        if(prmPonche.getHour().isBefore(listActivity.get(0).getHour())){
            throw new InvalidDateOrTime("La fecha o hora de entrada no puede ser menor a la registrada en el último ponche");
        }
    }
    objPonche=this.entryExitDBService.save(objPonche);
    EntryExitDTO objEntryExitDTO=this.mapper.map(objPonche, EntryExitDTO.class);
    objEntryExitDTO.setName(objPonche.getUserBusiness().getUsername());
    objEntryExitDTO.setHour(objEntryExitDTO.getHour().withNano(0));
    return new ResponseEntity<>(objEntryExitDTO,HttpStatus.OK);
}
public static Duration calculateDuration(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
        return Duration.between(startDateTime, endDateTime);
    }

/**
 * Retrieves the business configuration associated with the given user business ID.
 *
 * @param  userBusinessId  the ID of the user business
 * @return                 the ResponseEntity containing the list of BusinessConfigurationDTO objects
 *                         or an INTERNAL_SERVER_ERROR status if an exception occurs
 * @throws EntidadNoExisteException if the UsersBusiness with the given ID does not exist in the database
 */ 
@Override
@Transactional
public ResponseEntity<?> getBusinessConfiguration(Long userBusinessId) {
    List<BusinessConfigurationDTO> businessConfiguration=new ArrayList<>();
        
            UsersBusiness objUser=this.usersAppDBService.findById(userBusinessId).orElse(null);
            if(objUser==null){
                throw new EntidadNoExisteException("El UsersBusiness con userBusinessId "+userBusinessId+" no existe en la Base de datos");
            }
            try {    
            List<EmployeeBusinessConfigDownload> ebcList=this.employeeBusinessConfig.findByObjUserAndDownload(objUser,false);
            for(EmployeeBusinessConfigDownload ebc:ebcList){
                businessConfiguration.add(ebc.getObjConfiguration().toDTO());
                //ubp.setDownload(true);
                //this.ubpServices.save(ubp);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        

        return new ResponseEntity<>(businessConfiguration,HttpStatus.OK);
}

/**
 * Updates the download status for a list of business configurations associated with a user business.
 *
 * @param userBusinessId   the ID of the user business
 * @param configuration_ids the list of configuration IDs to be updated
 * @return                  a ResponseEntity indicating the success or failure of the operation
 */
@Override
@Transactional
public ResponseEntity<?> updateDownloadBusinessConfiguration(Long userBusinessId, List<Long> configuration_ids) {
    try{
        for(Long configuration_id:configuration_ids){
            this.employeeBusinessConfig.updateDownload(configuration_id, userBusinessId, true);
        }
    }catch(Exception e){
        return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}",HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    return new ResponseEntity<>(true,HttpStatus.OK);
}

/**
 * Finds all users business associated with a terminal.
 *
 * @param terminalId the ID of the terminal
 * @return a ResponseEntity containing the list of UsersBusinessDTO objects
 *         associated with the given terminal ID, or a BAD_REQUEST response
 *         if the terminal ID does not exist in the database
 */
@Override
@Transactional(readOnly = true)
public ResponseEntity<?> findByTerminalId(String terminalId) {
    Terminal terminal = this.serviceDBTerminal.findById(terminalId).orElse(null);
    
    if(terminal!=null) {
        List<UsersBusiness> usersBusiness = this.usersAppDBService.findByBusiness(terminal.getBusiness());
        List<UsersBusinessDTO> usersBusinessDTO = this.mapper.map(usersBusiness, new TypeToken<List<UsersBusinessDTO>>(){}.getType());
        for(int i=0;i<usersBusiness.size();i++){
                    usersBusinessDTO.get(i).setActivesPermissions(new ArrayList<>()   );
                    {
                    for(UserPermission up:usersBusiness.get(i).getUserPermissions()){

                        usersBusinessDTO.get(i).getActivesPermissions().add(up.getPermission().getPermissionId());
                    }
                    }
                }
        return new ResponseEntity<>(usersBusinessDTO,HttpStatus.OK);
    }else{
        EntidadNoExisteException objExeption = new EntidadNoExisteException("El Terminal con terminalId "+terminalId+" no existe en la Base de datos");
        throw objExeption;
    }
    
}
}
