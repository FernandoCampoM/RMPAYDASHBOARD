package com.retailmanager.rmpaydashboard.controller;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.services.services.ReportsServices.IReportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Validated
public class ReportsController {
    @Autowired
    private IReportService reportService;

    @GetMapping("/reports/daily-summary")
    public ResponseEntity<?> getDailySummary(@RequestParam(name = "businessId") Long businessId,
                                             @RequestParam(name = "startUtc") Instant startUtc,
                                             @RequestParam(name = "endUtc") Instant endUtc) {
        return reportService.getDailySummary(businessId, startUtc, endUtc);
    }

    @GetMapping("/reports/summary-by-date-range")
    public ResponseEntity<?> getSummaryByDateRange(@RequestParam(name = "businessId") Long businessId,
                                                   @RequestParam(name = "startUtc") Instant startUtc,
                                                   @RequestParam(name = "endUtc") Instant endUtc) {
        return reportService.getSummaryByDateRangee(businessId, startUtc, endUtc);
    }

    @GetMapping("/reports/low-inventory")
    public ResponseEntity<?> getLowInventory(@RequestParam(name = "businessId") Long businessId) {
        return reportService.getLowInventory(businessId);
    }

    @GetMapping("/reports/best-selling-items")
    public ResponseEntity<?> getBestSellingItems(@RequestParam(name = "businessId") Long businessId,
                                                 @RequestParam(name = "startUtc") Instant startUtc,
                                                 @RequestParam(name = "endUtc") Instant endUtc,
                                                  @RequestParam(name = "category") String categoria) {
        return reportService.getBestSellingItems(businessId, startUtc, endUtc, categoria);
    }
    @GetMapping("/reports/employee-weeklyScheduleDetail-report")
    public ResponseEntity<?> getEmployeeWeeklyScheduleDetail(@RequestParam(name = "employeeId",required = false) Long employeeId,
    @RequestParam(name = "businessId",required = false) Long businessId,
                                                 @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                                 @RequestParam(name = "endDate") @Valid LocalDate endDate) {
        return reportService.getEmployeeWeeklyScheduleDetail(businessId,employeeId, startDate, endDate);
    }
    @GetMapping("/reports/user-weeklySchedule-report")
    public ResponseEntity<?> Report_UserWeeklySchedule(@RequestParam(name = "employeeId",required = false) Long employeeId,
                                                 @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                                 @RequestParam(name = "endDate") @Valid LocalDate endDate) {
        return reportService.Report_UserWeeklySchedule(employeeId, startDate, endDate);
    }
    @GetMapping("/reports/work-hours-report")
    public ResponseEntity<?> WorkHoursReportService(@RequestParam(name = "businessId",required = false) Long businessId,
                                                 @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                                 @RequestParam(name = "endDate") @Valid LocalDate endDate) {
        return reportService.WorkHoursReportService(businessId, startDate, endDate);
    }
    @GetMapping("/reports/summary-workHoursVsScheduleHours")
    public ResponseEntity<?> workHoursVsScheduleHours(@RequestParam(name = "businessId") Long businessId,
                                                 @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                                 @RequestParam(name = "endDate") @Valid LocalDate endDate) {
        return reportService.workHoursVsScheduleHours(businessId, startDate, endDate);
    }
    @GetMapping("/reports/sales-by-category")
    public ResponseEntity<?> getSalesByCategory(@RequestParam(name = "businessId") Long businessId,
                                                @RequestParam(name = "startUtc") Instant startUtc,
                                                @RequestParam(name = "endUtc") Instant endUtc) {
        return reportService.getSalesByCategory(businessId, startUtc, endUtc);
    }

    @GetMapping("/reports/earnings-report")
    public ResponseEntity<?> getEarningsReport(@RequestParam(name = "businessId") Long businessId,
                                               @RequestParam(name = "startUtc") Instant startUtc,
                                               @RequestParam(name = "endUtc") Instant endUtc) {
        return reportService.getEarningsReport(businessId, startUtc, endUtc);
    }

    @GetMapping("/reports/tips")
    public ResponseEntity<?> getTips(@RequestParam(name = "businessId") Long businessId,
                                     @RequestParam(name = "startUtc") Instant startUtc,
                                     @RequestParam(name = "endUtc") Instant endUtc) {
        return reportService.getTips(businessId, startUtc, endUtc);
    }

    @GetMapping("/reports/taxes")
    public ResponseEntity<?> getTaxes(@RequestParam(name = "businessId") Long businessId,
                                      @RequestParam(name = "startUtc") Instant startUtc,
                                      @RequestParam(name = "endUtc") Instant endUtc) {
        return reportService.getTaxes(businessId, startUtc, endUtc);
    }
    @GetMapping("/reports/receipts")
    public ResponseEntity<?> getReceipts(@RequestParam(name = "businessId") Long businessId,
                                         @RequestParam(name = "startUtc") Instant startUtc,
                                         @RequestParam(name = "endUtc") Instant endUtc) {
        return reportService.getReceipts(businessId, startUtc, endUtc);
    }
    @GetMapping("/reports/home")
    public ResponseEntity<?> getHomeReport(@RequestParam(name = "businessId") Long businessId,
                                       @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                       @RequestParam(name = "endDate",required = false) @Valid LocalDate endDate,@RequestParam(name = "filter",required = false) String filter) {
        return reportService.getHomeReport(businessId, startDate, endDate, filter);
    }
    @GetMapping("/reports/ponches")
    public ResponseEntity<?> getPonchesReport(@RequestParam(name = "businessId") Long businessId,
                                       @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                       @RequestParam(name = "endDate") @Valid LocalDate endDate,@RequestParam(name = "userBusinessId",required = false) Long userBusinessId) {
        return reportService.getReportPonches(businessId, startDate, endDate, userBusinessId);
    }
}
