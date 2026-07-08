package com.retailmanager.rmpaydashboard.services.services.ScheduleCalendar;

import com.retailmanager.rmpaydashboard.models.ScheduleCalendar;
import com.retailmanager.rmpaydashboard.services.DTO.ScheduleCalendarDTO;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface IScheduleCalendarService {
    public ResponseEntity<?> save(ScheduleCalendarDTO prmService);

    public ResponseEntity<?> update(Long id, ScheduleCalendar prmService);

    public boolean delete(Long id);

    public ResponseEntity<?> findById(Long serviceId);

    public ResponseEntity<?> getAll(Long employeeId);

    public ResponseEntity<?> getAll(Long employeeId, LocalDate startDate, LocalDate endDate);

    ResponseEntity<?> getDetailedCalendar(Long employeeId);

    public ResponseEntity<?> getAllByBusinessId(Long prmBusinessId);


}
