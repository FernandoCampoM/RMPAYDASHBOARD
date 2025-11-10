package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.ScheduleCalendar;

import jakarta.transaction.Transactional;

public interface ScheduleCalendarRepository extends CrudRepository<ScheduleCalendar, Long> {
    
    @Query("SELECT s FROM ScheduleCalendar s WHERE s.employee.userBusinessId = :employeeId")
    public Iterable<ScheduleCalendar> findByEmployeeId(Long employeeId);
    @Query("SELECT s FROM ScheduleCalendar s WHERE s.employee.userBusinessId = :employeeId AND s.dateStart BETWEEN :startDate AND :endDate")
    public List<ScheduleCalendar> findByEmployeeIdRange(Long employeeId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM ScheduleCalendar s WHERE s.employee.business.businessId = :prmBusinessId")
    public Iterable<ScheduleCalendar> findByBusinessId(Long prmBusinessId);
    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduleCalendar s WHERE s.employee.userBusinessId = :employeeId")
    public void deleteByEmployeeId(Long employeeId);
}
