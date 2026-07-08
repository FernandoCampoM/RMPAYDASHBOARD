package com.retailmanager.rmpaydashboard.controller;

import com.retailmanager.rmpaydashboard.models.ScheduleCalendar;
import com.retailmanager.rmpaydashboard.services.DTO.ScheduleCalendarDTO;
import com.retailmanager.rmpaydashboard.services.services.ScheduleCalendar.IScheduleCalendarService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/schedule")
@Validated
public class ScheduleCalendarController {
    @Autowired
    private IScheduleCalendarService scheduleCalendarService;

    /**
     * Save a schedule calendar.
     *
     * @param prmService the ScheduleCalendarDTO to be saved
     * @return the ResponseEntity containing the result of the save operation
     */
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody ScheduleCalendarDTO prmService) {
        return scheduleCalendarService.save(prmService);
    }

    /**
     * Update a schedule calendar by ID.
     *
     * @param id         the ID of the schedule calendar to update
     * @param prmService the ScheduleCalendar object containing updated information
     * @return a ResponseEntity with the updated schedule calendar
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "scheduleId.positive") Long id,
                                    @Valid @RequestBody ScheduleCalendar prmService) {
        return scheduleCalendarService.update(id, prmService);
    }

    /**
     * Deletes a schedule calendar by ID.
     *
     * @param id the ID of the schedule calendar to delete
     * @return true if the schedule calendar is successfully deleted, false otherwise
     */
    @DeleteMapping("/{id}")
    public boolean delete(@Valid @PathVariable @Positive(message = "scheduleId.positive") Long id) {
        return scheduleCalendarService.delete(id);
    }

    /**
     * Find a schedule calendar by ID.
     *
     * @param id the ID of the schedule calendar to find
     * @return the ResponseEntity containing the found schedule calendar
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "scheduleId.positive") Long id) {
        return scheduleCalendarService.findById(id);
    }

    /**
     * Get all schedule calendars for an employee.
     *
     * @param employeeId the ID of the employee
     * @return the ResponseEntity containing all schedule calendars for the specified employee
     */
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(name = "employeeId", required = false) @Positive(message = "employeeId.positive") Long employeeId,
                                    @RequestParam(name = "businessId", required = false) @Positive(message = "businessId.positive") Long businessId,
                                    @RequestParam(name = "startDate", required = false) LocalDate startDate,
                                    @RequestParam(name = "endDate", required = false) LocalDate endDate) {
        if (businessId != null) return scheduleCalendarService.getAllByBusinessId(businessId);
        if (employeeId != null && startDate != null && endDate != null)
            return scheduleCalendarService.getAll(employeeId, startDate, endDate);
        return scheduleCalendarService.getAll(employeeId);
    }

    /**
     * Get all schedule calendars for an employee.
     *
     * @param employeeId the ID of the employee
     * @return the ResponseEntity containing all schedule calendars for the specified employee
     */
    @GetMapping("detailed")
    public ResponseEntity<?> getDetailedCalendar(@RequestParam(name = "employeeId") @Positive(message = "employeeId.positive") Long employeeId) {
        return scheduleCalendarService.getDetailedCalendar(employeeId);
    }
}
