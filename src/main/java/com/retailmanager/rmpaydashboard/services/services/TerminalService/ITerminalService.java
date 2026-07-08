package com.retailmanager.rmpaydashboard.services.services.TerminalService;


import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.BuyTerminalDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalDTO;


public interface ITerminalService {
    public ResponseEntity<?> save(TerminalDTO prmTerminal);
    public ResponseEntity<?> buyTerminal(BuyTerminalDTO prmTerminal);
    public ResponseEntity<?> update(String terminalId, TerminalDTO prmTerminal);
    public ResponseEntity<?> updateAutomaticPayments(String idTerminal, Boolean status);
    public boolean delete(String terminalId);
    public ResponseEntity<?> findById(String terminalId);
    public ResponseEntity<?> findBySerial(String  serial);
    public ResponseEntity<?> updateEnable(String terminalId, boolean enable);
    public ResponseEntity<?> getExpiredTerminals(Long businessId); 
    public String getTerminalId();
}
