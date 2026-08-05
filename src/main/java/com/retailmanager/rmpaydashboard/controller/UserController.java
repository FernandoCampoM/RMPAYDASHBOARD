package com.retailmanager.rmpaydashboard.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.services.DTO.RegistryDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ClientPasswordResetRequestDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ForgotPasswordRequestDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ResetPasswordRequestDTO;
import com.retailmanager.rmpaydashboard.services.DTO.UpdatePasswordAdminDTO;
import com.retailmanager.rmpaydashboard.services.DTO.UserDTO;
import com.retailmanager.rmpaydashboard.services.services.UserService.IUserService;




@RestController
@RequestMapping("/api")
@Validated
public class UserController {
    @Autowired
    private IUserService userService;

    
    
 
    /**
     * A method to find a user by their ID.
     *
     * @param  userId   the ID of the user to find
     * @return          the ResponseEntity containing the user found by ID
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "userId.positive")Long userId){
        return this.userService.findById(userId);
    }
    /**
     * Finds a user by username.
     *
     * @param  username	description of the username parameter
     * @return         	description of the ResponseEntity return value
     */
    @GetMapping("/users")
    public ResponseEntity<?> findByUsername(@Valid @RequestParam(name = "username") @NotBlank(message = "username.notBlank") String username){
        return this.userService.findByUsername(username);
    }
    /**
     * Find all users.
     *
     * @return         description of return value
     */
    @GetMapping("/users/all")
    public ResponseEntity<?> findAll(){
        return this.userService.findAll();
    }
    /**
     * Find all users with pagination and filtering.
     *
     * @param  pageable pagination information
     * @param  filter   filter criteria
     * @return          response entity with user data
     */
    @GetMapping("/users/all/pageable")
    public ResponseEntity<?> findAll(@PageableDefault(size = 200,page = 0) Pageable pageable,@RequestParam(required=false) String filter){
        if(filter!=null){
            return this.userService.findAll(pageable,filter);
        }   
        return this.userService.findAll(pageable);
    }
    /**
     * Find all unregistered users.
     *
     * @return         response entity with all unregistered users
     */
    @GetMapping("/users/unregistered")
    public ResponseEntity<?> getUnregisteredClients(){
        return this.userService.getUnregisteredClients();
    }
    
    /**
     * Save a user.
     *
     * @param  prmUser  the user to be saved
     * @return          the response entity
     */
    @PostMapping("/users")
    public ResponseEntity<?> save(@Valid @RequestBody UserDTO prmUser){
        return this.userService.save(prmUser);
    }
    /**
     * Updates a user by userId.
     *
     * @param  userId   the ID of the user to update
     * @param  prmUser  the updated user information
     * @return          the ResponseEntity containing the updated user
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "userId.positive")Long userId,@Valid @RequestBody UserDTO prmUser){
        return this.userService.update(userId,prmUser);
    }
    /**
     * Delete user by ID.
     *
     * @param  userId	ID of the user to be deleted
     * @return        	true if the user is successfully deleted, false otherwise
     */
    @DeleteMapping("/users/{userId}")
    public Boolean delete(@Valid @PathVariable @Positive(message = "userId.positive")Long userId){
        return this.userService.delete(userId);
    }

    /**
     * Retrieves the business information for a specific user.
     *
     * @param  userId   the ID of the user
     * @return          the ResponseEntity containing the user's business information
     */
    @GetMapping("/users/{userId}/business")
    public ResponseEntity<?> getUserBusiness(@Valid @PathVariable @Positive(message = "userId.positive")Long userId){
        return this.userService.getUserBusiness(userId);
    }
    /**
     * Updates the enable status for the specified user.
     *
     * @param  userId  the ID of the user to be updated
     * @param  enable  the new enable status
     * @return         the response entity representing the result of the update operation
     */
    @PutMapping("/users/{userId}/enable/{enable}")
    public ResponseEntity<?> updateEnable(@Valid @PathVariable @Positive(message = "userId.positive")Long userId, @Valid @PathVariable boolean enable){
        return this.userService.updateEnable(userId, enable);
    }
    
    /**
     * A method to create a new registry of a user with a business.
     *
     * @param  prmRegistry   the registry data to be validated and processed
     * @return               a ResponseEntity containing the result of the registry operation
     */
    @PostMapping("/register")
    public ResponseEntity<?> newRegistry(@Valid @RequestBody RegistryDTO prmRegistry){
        return this.userService.registryWithBusiness(prmRegistry);
    }

    @PostMapping("/users/password-recovery/request")
    public ResponseEntity<?> requestPasswordRecovery(@Valid @RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO){
        return this.userService.requestPasswordRecovery(forgotPasswordRequestDTO.getEmail());
    }

    @PostMapping("/users/password-recovery/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO){
        return this.userService.resetPassword(resetPasswordRequestDTO.getToken(), resetPasswordRequestDTO.getNewPassword());
    }


    /**
     * Retrieves a list of users with manager roles.
     *
     * @return  a ResponseEntity containing the list of UserDTO objects representing the users with manager roles
     */
    @GetMapping("/users/managers")
    public ResponseEntity<?> getManagers(){
        return this.userService.getAllUsersManagers();
    }

    /**
     * Updates the password for a user with a manager role.
     *
     * @param  userId         the ID of the user to update
     * @param  prmPassword    the new password data to be validated and processed
     * @return                a ResponseEntity containing the result of the update operation
     */
    @PutMapping("/users/password/{userId}")
    public ResponseEntity<?> updatePasswordAdmin(@Valid @PathVariable @Positive(message = "userId.positive")Long userId, @RequestBody UpdatePasswordAdminDTO prmPassword){
        return this.userService.updatePasswordForAdmin(userId, prmPassword.getNewPassword());
    }

    @PutMapping("/users/{userId}/password/reset-client")
    public ResponseEntity<?> resetClientPassword(@Valid @PathVariable @Positive(message = "userId.positive") Long userId, @Valid @RequestBody ClientPasswordResetRequestDTO passwordResetRequestDTO){
        return this.userService.resetClientPasswordByAdmin(userId, passwordResetRequestDTO.getNewPassword());
    }
}
