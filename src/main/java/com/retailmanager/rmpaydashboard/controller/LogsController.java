package com.retailmanager.rmpaydashboard.controller;

import com.retailmanager.rmpaydashboard.services.DTO.LogsDTO;
import com.retailmanager.rmpaydashboard.services.services.LogsService.ILogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @PostMapping("/list")
    public ResponseEntity<?> addList(
            @RequestBody List<LogsDTO> logsDTO
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