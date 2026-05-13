package com.retailmanager.rmpaydashboard.controller;

import com.retailmanager.rmpaydashboard.services.DTO.LogsDTO;
import com.retailmanager.rmpaydashboard.services.services.LogsService.ILogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@Validated
@RequiredArgsConstructor
public class LogsController {

    private final ILogsService logsService;

    @PostMapping
    public ResponseEntity<?> add(
            @RequestBody LogsDTO logsDTO
    ) {
        return logsService.add(logsDTO);
    }

    @GetMapping("/{terminalId}")
    public ResponseEntity<?> getAllByTerminalId(
            @PathVariable String terminalId
    ) {
        return logsService.getAllByTerminalId(terminalId);
    }

    @DeleteMapping("/{terminalId}")
    public ResponseEntity<?> removeAllByTerminalId(
            @PathVariable String terminalId
    ) {
        return logsService.removeAllByTerminalId(terminalId);
    }
}