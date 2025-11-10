package com.retailmanager.rmpaydashboard.services.services.ScheduleCalendar;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.models.ScheduleCalendar;
import com.retailmanager.rmpaydashboard.services.DTO.ScheduleCalendarDTO;

public interface IScheduleCalendarService {
    public ResponseEntity<?> save(ScheduleCalendarDTO prmService);
    public ResponseEntity<?> update(Long id, ScheduleCalendar prmService);
    public boolean delete(Long id);
    public ResponseEntity<?> findById(Long serviceId);
    public ResponseEntity<?> getAll(Long employeeId);
    public ResponseEntity<?> getAll(Long employeeId,LocalDate startDate, LocalDate endDate);
    public ResponseEntity<?> getAllByBusinessId(Long prmBusinessId);

    
}
