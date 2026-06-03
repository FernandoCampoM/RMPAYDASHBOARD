package com.retailmanager.rmpaydashboard.services.services.BusinessService;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.BusinessDTO;
import com.retailmanager.rmpaydashboard.services.DTO.RegsitryBusinessDTO;

public interface IBusinessService {
    public ResponseEntity<?> save(BusinessDTO prmBusiness);
    public ResponseEntity<?> save(RegsitryBusinessDTO prmBusiness);
    public ResponseEntity<?> update(Long businessId, BusinessDTO prmBusiness);
    public boolean delete(Long businessId);
    public ResponseEntity<?> findById(Long businessId);
    public ResponseEntity<?> findByMerchantId(String merchantId);
    /**
     * Finds a business by its terminal id.
     *
     * @param terminalId the terminal id
     * @return the business found or an error message
     */
    public ResponseEntity<?> findByTerminalId(String terminalId)
    ;
    public ResponseEntity<?> getTerminals(Long businessId);
    public ResponseEntity<?> getCategories(Long businessId);
    public ResponseEntity<?> updateEnable(Long businessId, boolean enable);
    public ResponseEntity<?> findAll();
    public ResponseEntity<?> getActivations(Instant starDate, Instant endDate);
    public ResponseEntity<?> getMonthActivations();
    public ResponseEntity<?> deleteLogo(Long businessId);
    public ResponseEntity<?> deleteLogoATH(Long businessId);

    
}
