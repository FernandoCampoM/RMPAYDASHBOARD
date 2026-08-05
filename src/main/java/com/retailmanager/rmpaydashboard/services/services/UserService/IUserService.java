package com.retailmanager.rmpaydashboard.services.services.UserService;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.RegistryDTO;
import com.retailmanager.rmpaydashboard.services.DTO.UserDTO;

public interface IUserService {
    /**
     * Saves a user.
     * 
     * @param prmUser the user data to be saved
     * @return the response entity for the saved user
     */
    public ResponseEntity<?> save(UserDTO prmUser);
    
    public ResponseEntity<?> update(Long userId, UserDTO prmUser);
    public boolean delete(Long userId);
    public ResponseEntity<?> findById(Long userId);
    public ResponseEntity<?> findByUsername(String  username);
    public ResponseEntity<?> getUserBusiness(Long userId);
    public ResponseEntity<?> updateEnable(Long userId, boolean enable);
    public ResponseEntity<?> registryWithBusiness(RegistryDTO prmRegistry);
    public ResponseEntity<?> findAll();
    public ResponseEntity<?> findAll(Pageable pageable,String filter);
    public ResponseEntity<?> findAll(Pageable pageable);
    public ResponseEntity<?> getActivesClients();
    public ResponseEntity<?> getUnregisteredClients();
    public ResponseEntity<?> getAllUsersManagers();

    public ResponseEntity<?> updatePasswordForAdmin(Long userId, String password);
    public ResponseEntity<?> resetClientPasswordByAdmin(Long userId, String newPassword);
    public ResponseEntity<?> requestPasswordRecovery(String email);
    public ResponseEntity<?> resetPassword(String token, String newPassword);
    

    
}
