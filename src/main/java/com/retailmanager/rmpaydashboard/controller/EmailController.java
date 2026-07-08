package com.retailmanager.rmpaydashboard.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.BatchCloseReportDTO;
import com.retailmanager.rmpaydashboard.services.services.AutomatedEmailService.AutomatedEmailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Validated
public class EmailController {

    @Autowired
    private AutomatedEmailService automatedEmailService;

    @PostMapping("/email/batch-close-report")
    public ResponseEntity<?> sendBatchCloseReport(@RequestBody @Valid BatchCloseReportDTO batchCloseReportDTO) {
        return automatedEmailService.sendBatchCloseReport(batchCloseReportDTO);
    }
}
