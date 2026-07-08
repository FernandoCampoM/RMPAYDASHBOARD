package com.retailmanager.rmpaydashboard.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.services.DTO.EmployeeAuthentication;
import com.retailmanager.rmpaydashboard.services.DTO.EntryExitDTO;
import com.retailmanager.rmpaydashboard.services.DTO.UsersBusinessDTO;
import com.retailmanager.rmpaydashboard.services.services.EmailService.IEmailService;
import com.retailmanager.rmpaydashboard.services.services.UsersBusinessService.IUsersBusinessService;


@RestController
@RequestMapping("/api")
@Validated
public class UserBusinessController {
    
    @Autowired
    private IUsersBusinessService usersBusinessService;


    @Autowired IEmailService emailService;

    /**
     * Save a user business.
     *
     * @param  prmUsersBusiness   the UsersBusinessDTO to be saved
     * @return                   the ResponseEntity representing the result of the save operation
     */
    @PostMapping("/userBusiness")
    public ResponseEntity<?> save(@Valid @RequestBody UsersBusinessDTO prmUsersBusiness) {
        return usersBusinessService.save(prmUsersBusiness);
    }

    /**
     * Updates a user business.
     *
     * @param  userBusinessId  the ID of the user business to be updated
     * @param  prmUsersBusiness  the updated user business data
     * @return         	the ResponseEntity containing the result of the update operation
     */
    @PutMapping("/userBusiness/{userBusinessId}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId,
            @Valid @RequestBody UsersBusinessDTO prmUsersBusiness) {
        return usersBusinessService.update(userBusinessId, prmUsersBusiness);
    }

    /**
     * Deletes a user business by ID.
     *
     * @param  userBusinessId   the ID of the user business to delete
     * @return                  true if the user business was successfully deleted, false otherwise
     */
    @DeleteMapping("/userBusiness/{userBusinessId}")
    public boolean delete(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId) {
        return usersBusinessService.delete(userBusinessId);
    }

    /**
     * findById function to find user business by ID.
     *
     * @param  userBusinessId	description of the user business ID parameter
     * @return         	response entity with user business details
     */
    @GetMapping("/userBusiness/{userBusinessId}")
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId) {
        return usersBusinessService.findById(userBusinessId);
    }
    @GetMapping("/userBusiness/business/{businessId}")
    public ResponseEntity<?> findByBusinessId(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long businessId) {
        return usersBusinessService.findByBusiness(businessId);
    }
     @GetMapping("/userBusiness")
    public ResponseEntity<?> findByTerminalId(@RequestParam(name = "terminalId") String terminalId) {
        return usersBusinessService.findByTerminalId(terminalId);
    }

    /**
     * A description of the entire Java function.
     *
     * @param  userBusinessId   description of parameter
     * @param  enable           description of parameter
     * @return                  description of return value
     */
    @PutMapping("/userBusiness/{userBusinessId}/enable/{enable}")
    public ResponseEntity<?> updateEnable(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId,
            @Valid @PathVariable boolean enable) {
        return usersBusinessService.updateEnable(userBusinessId, enable);
    }
    /**
     * Update the enable status of a permission for a user business.
     *
     * @param  userBusinessId   the ID of the user business
     * @param  idPermission     the ID of the permission
     * @param  enable           the boolean value indicating whether to enable the permission
     * @return                  the ResponseEntity representing the result of the update operation
     */
    @PutMapping("/userBusiness/{userBusinessId}/permission/{idPermission}/enable/{enable}")
    public ResponseEntity<?> updateEnable(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId,
           @Valid @PathVariable @Positive(message = "idPermission.positive") Long idPermission, @Valid @PathVariable boolean enable) {
        return usersBusinessService.updatePermission(userBusinessId, idPermission, enable);
    }
    @GetMapping("/userBusiness/permissions")
    public ResponseEntity<?> getAllPermissions() {
        return usersBusinessService.getAllPermissions();
    }
    @GetMapping("/userBusiness/products/{userBusinessId}")
    public ResponseEntity<?> getProducts(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId) {
        return usersBusinessService.getProducts(userBusinessId);
    }
    @PutMapping("/userBusiness/products/{userBusinessId}")
    public ResponseEntity<?> updateDownloadProducts(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId, @Valid @RequestBody @NotEmpty(message = "product_ids.notempty") List<Long> product_ids) {
        return usersBusinessService.updateDownloadProducts(userBusinessId, product_ids);
    }
    @GetMapping("/userBusiness/category/{userBusinessId}")
    public ResponseEntity<?> getCategory(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId) {
        return usersBusinessService.getCategory(userBusinessId);
    }
    @PutMapping("/userBusiness/category/{userBusinessId}")
    public ResponseEntity<?> updateDownloadCategory(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId, @Valid @RequestBody @NotEmpty(message = "category_ids.notempty") List<Long> category_ids) {
        return usersBusinessService.updateDownloadCategory(userBusinessId, category_ids);
    }
    @GetMapping("/userBusiness/businessConfiguration/{userBusinessId}")
    public ResponseEntity<?> getBusinessConfiguration(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId) {
        return usersBusinessService.getBusinessConfiguration(userBusinessId);
    }
    @PutMapping("/userBusiness/businessConfiguration/{userBusinessId}")
    public ResponseEntity<?> updateDownloadbusinessConfiguration(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId, @Valid @RequestBody @NotEmpty(message = "configuration_ids.notempty") List<Long> configuration_ids) {
        return usersBusinessService.updateDownloadBusinessConfiguration(userBusinessId, configuration_ids);
    }
    @GetMapping("/userBusiness/userBusiness/{userBusinessId}")
    public ResponseEntity<?> getUsersBusiness(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId) {
        return usersBusinessService.getUsersBusiness(userBusinessId);
    }
    /**
     * Updates the download status for a specific user business.
     *
     * @param  userBusinessId  the ID of the user business
     * @return                 a ResponseEntity containing the updated user business and the HTTP status
     */
    @PutMapping("/userBusiness/download/{userBusinessId}")
    public ResponseEntity<?> updateDownloadCategory(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId) {
        return usersBusinessService.updateDownloadUserBusiness(userBusinessId);
    }
    /**
     * Saves an activity entry or exit for a user business.
     *
     * @param  prmEntryExit  the EntryExitDTO object containing the activity details
     * @return               a ResponseEntity containing the result of the operation
     */
    @PostMapping("/userBusiness/activity/exit")
    public ResponseEntity<?> saveExit(@RequestHeader("Authorization") String authToken,@Valid @RequestBody EmployeeAuthentication prmEmployeeAuthentication ) {
        return usersBusinessService.registerExit(authToken.replace("Bearer ", ""),prmEmployeeAuthentication);
    }
    /**
     * Saves an activity entry for a user business.
     *
     * @param  prmEntryExit  the AuthCredentials object containing the activity details
     * @return               a ResponseEntity containing the result of the operation
     */
    @PostMapping("/userBusiness/activity/entry")
    public ResponseEntity<?> saveEntry(@RequestHeader("Authorization") String authToken,@Valid @RequestBody EmployeeAuthentication prmEmployeeAuthentication) {
        return usersBusinessService.registerEntry( authToken.replace("Bearer ", ""),prmEmployeeAuthentication);
    }
    /**
     * Retrieves the last activity for a given user business.
     *
     * @param  userBusinessId  the ID of the user business
     * @return                 a ResponseEntity containing the last activity or an error message
     */
    @GetMapping("/userBusiness/activity/last/{userBusinessId}")
    public ResponseEntity<?> getActivity(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId) { 
        
        return usersBusinessService.getLastActivity(userBusinessId);
    }
    @PutMapping("/userBusiness/activity/{activityId}")
    public ResponseEntity<?> editPonche(@Valid @PathVariable @Positive(message = "activityId.positive") Long activityId,
    @RequestBody EntryExitDTO prmEntryExit) {
        return usersBusinessService.updatePonche(activityId, prmEntryExit);
    }
    @DeleteMapping("/userBusiness/activity/last/{userBusinessId}")
    public ResponseEntity<?> deleteLastActivity(@Valid @PathVariable @Positive(message = "userBusinessId.positive") Long userBusinessId) { 
        
        return usersBusinessService.deleteLastActivity(userBusinessId);
    }

    
    /* @GetMapping("/emailtest")
    public String eailtest() {
        this.emailService.lastDayNotificationEmail("juancampo201509@gmail.com", "juancamm", "Evolución Imparable");
        return new String("Email Enviado: ");
    } */
    
}
