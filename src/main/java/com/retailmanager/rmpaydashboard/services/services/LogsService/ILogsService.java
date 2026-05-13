package com.retailmanager.rmpaydashboard.services.services.LogsService;

import com.retailmanager.rmpaydashboard.services.DTO.LogsDTO;
import org.springframework.http.ResponseEntity;

public interface ILogsService {
    ResponseEntity<?> add(LogsDTO logsDTO);

    ResponseEntity<?> removeAllByTerminalId(String terminalId);

    ResponseEntity<?> getAllByTerminalId(String terminalId);
}
