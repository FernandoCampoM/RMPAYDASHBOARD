package com.retailmanager.rmpaydashboard.services.services.ReportsServices;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

public interface IReportService {

    public ResponseEntity<?> getDailySummary(Long businessId, Instant startUtc, Instant endUtc);
    public ResponseEntity<?> getSummaryByDateRangee(Long businessId, Instant startUtc, Instant endUtc);
    public ResponseEntity<?> getLowInventory(Long businessId);
    public ResponseEntity<?> getBestSellingItems(Long businessId,Instant startUtc, Instant endUtc, String categoria);
    public ResponseEntity<?> getSalesByCategory(Long businessId,Instant startUtc, Instant endUtc);
    public ResponseEntity<?> getEarningsReport(Long businessId,Instant startUtc, Instant endUtc);
    /**
     * Get tips(PROPINAS) by business and date range
     * @param businessId
     * @param startDate
     * @param endDate
     * @return
     */
    public ResponseEntity<?> getTips(Long businessId, Instant startUtc, Instant endUtc);
    public ResponseEntity<?> getTaxes(Long businessId, Instant startUtc, Instant endUtc);

    public ResponseEntity<?> getReceipts(Long businessId, Instant startUtc, Instant endUtc);

    public ResponseEntity<?> getActivationsReport(int month);

    public ResponseEntity<?> getHomeReport(Long businessId,LocalDate startDate, LocalDate endDate, String filter);

    public ResponseEntity<?> getReportPonches(Long businessId, LocalDate startDate, LocalDate endDate, Long filter);

    /*
     * Get labor hours vs hourly cost by business and date range
     */
    public ResponseEntity<?> WorkHoursReportService(Long businessId, LocalDate startDate, LocalDate endDate);
    public ResponseEntity<?> workHoursVsScheduleHours(Long businessId, LocalDate startDate, LocalDate endDate);
    public ResponseEntity<?> Report_UserWeeklySchedule(Long userBusinessId, LocalDate startDate, LocalDate endDate);
    public ResponseEntity<?> getEmployeeWeeklyScheduleDetail(Long businessId,Long userBusinessId, LocalDate startDate, LocalDate endDate);

}
