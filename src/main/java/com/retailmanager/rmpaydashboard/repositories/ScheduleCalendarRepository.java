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
    Iterable<ScheduleCalendar> findByEmployeeId(Long employeeId);
    @Query("SELECT s FROM ScheduleCalendar s WHERE s.employee.userBusinessId = :employeeId AND s.dateStart BETWEEN :startDate AND :endDate")
    List<ScheduleCalendar> findByEmployeeIdRange(Long employeeId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM ScheduleCalendar s WHERE s.employee.business.businessId = :prmBusinessId")
    Iterable<ScheduleCalendar> findByBusinessId(Long prmBusinessId);
    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduleCalendar s WHERE s.employee.userBusinessId = :employeeId")
    void deleteByEmployeeId(Long employeeId);

    @Query(value = "WITH N AS ( " +
            "    SELECT 0 AS n " +
            "    UNION ALL " +
            "    SELECT n + 1 " +
            "    FROM n " +
            "    WHERE n < 1000 " +
            ")," +
            "RangosPorDia AS ( " +
            "    SELECT " +
            "        s.*, " +
            "        DATEADD(DAY, n, CAST(s.dateStart AS DATE)) AS FechaDia " +
            "    FROM ScheduleCalendar s " +
            "    JOIN N " +
            "        ON DATEADD(DAY, n, CAST(s.dateStart AS DATE)) " +
            "           <= CAST(s.dateEnd AS DATE) " +
            "), " +
            "SemanaCalculada AS ( " +
            "    SELECT " +
            "        r.*, " +
            "        DATEADD(DAY, " +
            "            -((DATEPART(WEEKDAY, r.FechaDia) + @@DATEFIRST - 2) % 7), " +
            "            r.FechaDia " +
            "        ) AS Lunes " +
            "    FROM RangosPorDia r " +
            "), " +
            "Limites AS (\n" +
            "    SELECT\n" +
            "        DATEADD(DAY, -((DATEPART(WEEKDAY, CAST(GETDATE() AS DATE)) + @@DATEFIRST - 2) % 7), CAST(GETDATE() AS DATE)) AS LunesSemanaActual " +
            ") " +
            "SELECT " +
            " id, " +
            "    FORMAT(Lunes, 'd ''de'' MMMM', 'es-ES') " +
            "    + ' - ' + " +
            "    FORMAT(DATEADD(DAY, 6, Lunes), 'd ''de'' MMMM', 'es-ES') " +
            "    AS DescripcionSemana, " +
            "    FORMAT(FechaDia, 'dddd | dd/MM/yyyy', 'es-ES') AS DiaFormateado, " +
            "    LOWER( " +
            "        FORMAT( " +
            "            CASE " +
            "                WHEN FechaDia = CAST(dateStart AS DATE) THEN dateStart " +
            "                ELSE DATEADD(DAY, DATEDIFF(DAY, 0, FechaDia), 0) " +
            "            END, " +
            "            'h:mmtt', 'en-US' " +
            "        ) " +
            "    ) " +
            "    + ' - ' + " +
            "    LOWER( " +
            "        FORMAT( " +
            "            CASE " +
            "                WHEN FechaDia = CAST(dateEnd AS DATE) THEN dateEnd " +
            "                ELSE DATEADD(MINUTE, -1, " +
            "                     DATEADD(DAY, DATEDIFF(DAY, 0, FechaDia) + 1, 0)) " +
            "            END,'h:mmtt', 'en-US' ) " +
            "    ) AS HoraFormateada, " +
            "    dateStart, " +
            "    dateEnd, " +
            "    title, " +
            "    userBusinessId " +
            "FROM SemanaCalculada " +
            "CROSS JOIN Limites " +
            "WHERE userBusinessId = :employeeId " +
            "AND Lunes >= LunesSemanaActual " +
            "AND Lunes <  DATEADD(DAY, 14, LunesSemanaActual) " +
            "ORDER BY dateStart, FechaDia " +
            "OPTION (MAXRECURSION 1000) ", nativeQuery = true)
    Iterable<Object[]> getDetailedCalendar(Long employeeId);
}
