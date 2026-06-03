package com.retailmanager.rmpaydashboard.controller.RMPayAtTheTable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.RMPayAtTheTable_TerminalDTO;
import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.RMPayAtTheTable_UserDTO;
import com.retailmanager.rmpaydashboard.services.services.RMPayAtTheTable.TerminalService.ITerminalPayAtTableService;
import com.retailmanager.rmpaydashboard.services.services.RMPayAtTheTable.UserServices.IUserPayAtTableService;

import jakarta.validation.Valid;

import java.util.List;
@RestController
@Validated
@RequestMapping("/api/payAtTheTable/terminals")
public class TerminalPayAtTableController {
    @Autowired
    private ITerminalPayAtTableService terminalService;

    @Autowired
    private IUserPayAtTableService userService;

    @PostMapping
    public ResponseEntity<?> createTerminal(@Valid @RequestBody RMPayAtTheTable_TerminalDTO terminalDTO) {
        return terminalService.createTerminal(terminalDTO);
    }

    @PutMapping("/{terminalId}")
    public ResponseEntity<?> updateTerminal(@Valid @PathVariable Long terminalId,@Valid @RequestBody RMPayAtTheTable_TerminalDTO terminalDTO) {
        return terminalService.updateTerminal(terminalId, terminalDTO);
    }

    @DeleteMapping("/{terminalId}")
    public ResponseEntity<?> deleteTerminal(@Valid @PathVariable Long terminalId) {
        return terminalService.deleteTerminal(terminalId);
    }
    @DeleteMapping("/serialNumber/{serialNumber}")
    public ResponseEntity<?> deleteTerminal(@Valid @PathVariable String serialNumber) {
        return terminalService.deleteTerminal(serialNumber);
    }

    @GetMapping("/{terminalId}")
    public ResponseEntity<RMPayAtTheTable_TerminalDTO> getTerminalById(@Valid @PathVariable Long terminalId) {
        return terminalService.getTerminalById(terminalId);
    }
    @GetMapping("/serialNumber/{serialNumber}")
    public ResponseEntity<RMPayAtTheTable_TerminalDTO> getTerminalBySerial(@Valid @PathVariable String serialNumber) {
        return terminalService.getTerminalBySerialNumber(serialNumber);
    }

    @GetMapping
public ResponseEntity<List<RMPayAtTheTable_TerminalDTO>> getAllTerminals(
    @RequestParam(required = false) Long userId,
    @RequestParam(required = false) String merchantId) {
    
    // Si se proporciona userId, obtener terminales por usuario
    if (userId != null) {
        return terminalService.getTerminalsByUserId(userId);
    }
    
    // Si se proporciona merchantId, obtener terminales por merchantId
    if (merchantId != null) {
        RMPayAtTheTable_UserDTO user = userService.getUserByMerchantId(merchantId).getBody();
        if(user == null) throw new EntidadNoExisteException("El Negocio con merchantId " + merchantId + " no existe en la Base de datos");
        return terminalService.getTerminalsByUserId(user.getUserId());
    }
    
    // Si no se envía ningún parámetro, devolver todos los terminales
    return terminalService.getAllTerminals();
}

    @PutMapping("/{terminalId}/status/{active}")
    public ResponseEntity<?> changeTerminalStatus(@PathVariable Long terminalId, @PathVariable boolean active) {
        if(active) return terminalService.activateTerminal(terminalId);
        else return terminalService.deactivateTerminal(terminalId);
    }
}

