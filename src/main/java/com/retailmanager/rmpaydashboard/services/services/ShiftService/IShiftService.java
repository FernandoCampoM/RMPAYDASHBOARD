package com.retailmanager.rmpaydashboard.services.services.ShiftService;

import com.retailmanager.rmpaydashboard.services.DTO.CloseShiftDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.ShiftDTO;

import java.time.Instant;

public interface IShiftService {
    public ResponseEntity<?> createShift(ShiftDTO shiftDTO);
    public ResponseEntity<?> updateShift(ShiftDTO shiftDTO);
    ResponseEntity<?> updateStatusShift(String shiftId, String status);
    public ResponseEntity<?> deleteShift(String shiftId);
    public ResponseEntity<?> closeShift(ShiftDTO shiftDTO);
    ResponseEntity<?> closeShiftWeb(CloseShiftDTO closeShiftDTO);
    public ResponseEntity<?> getShiftById(String shiftId);
    public ResponseEntity<?> getAllShiftsPageable(Long businessId,Long employeeId,
        String serialNumber,
        Instant startDate,
        Instant endDate,
        Boolean statusShiftBalance,
        Pageable pageable);
    ResponseEntity<?> getAllShiftsSync(String terminalId);
    ResponseEntity<?> updateShiftSync(CloseShiftDTO closeShiftDTO);
    

}
