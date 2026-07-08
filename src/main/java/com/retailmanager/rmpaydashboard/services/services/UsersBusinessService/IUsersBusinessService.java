package com.retailmanager.rmpaydashboard.services.services.UsersBusinessService;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.EmployeeAuthentication;
import com.retailmanager.rmpaydashboard.services.DTO.EntryExitDTO;
import com.retailmanager.rmpaydashboard.services.DTO.UsersBusinessDTO;

public interface IUsersBusinessService {
    public ResponseEntity<?> save(UsersBusinessDTO prmUsersBusiness);
    public ResponseEntity<?> update(Long userBusinessId, UsersBusinessDTO prmUsersBusiness);
    public boolean delete(Long userBusinessId);
    /**
     * Finds a user by their business ID.
     *
     * @param  userBusinessId	the ID of the user's business
     * @return         		the ResponseEntity containing the user's business details or a BAD_REQUEST response
     */
    public ResponseEntity<?> findById(Long userBusinessId)
    ;
    /**
     * Finds a user by their terminal ID.
     *
     * @param  terminalId  the ID of the terminal
     * @return         		the ResponseEntity containing the user's business details or a BAD_REQUEST response
     */
    public ResponseEntity<?> findByTerminalId(String terminalId)
    ;
    public ResponseEntity<?> updateEnable(Long userBusinessId, boolean enable);
    public ResponseEntity<?> findByBusiness(Long idBusiness);
    public ResponseEntity<?> updatePermission(Long idUser, Long idPermission, boolean enable);
    public ResponseEntity<?> getAllPermissions();
    public ResponseEntity<?> getProducts(Long userBusinessId);
    public ResponseEntity<?> getCategory(Long userBusinessId);
    public ResponseEntity<?> getBusinessConfiguration(Long userBusinessId);
    public ResponseEntity<?> updateDownloadBusinessConfiguration(Long userBusinessId, List<Long> configuration_ids);
    public ResponseEntity<?> getUsersBusiness(Long userBusinessId);
    public ResponseEntity<?> updateDownloadProducts(Long userBusinessId,List<Long> product_ids);
    public ResponseEntity<?> updateDownloadCategory(Long userBusinessId,List<Long> category_ids);
    public ResponseEntity<?> updateDownloadUserBusiness(Long userBusinessId);
    public ResponseEntity<?> registerEntry(String authToken, EmployeeAuthentication prmEmployeeAuthentication);
    public ResponseEntity<?> registerExit(String authToken,EmployeeAuthentication prmEmployeeAuthentication);
    public ResponseEntity<?> getLastActivity(Long prmUserBusinessId);
    public ResponseEntity<?> deleteLastActivity(Long prmUserBusinessId);
    public ResponseEntity<?> updatePonche(Long activityId, EntryExitDTO prmPonche);

    

}
