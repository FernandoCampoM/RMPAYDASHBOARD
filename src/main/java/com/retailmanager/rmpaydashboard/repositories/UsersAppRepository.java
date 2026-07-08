package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;

public interface UsersAppRepository extends CrudRepository<UsersBusiness, Long> {

    List<UsersBusiness> findByBusiness(Business business);

    Optional<UsersBusiness> findByUsername(String username);

    @Query("SELECT u FROM UsersBusiness u WHERE u.password = :password AND u.business.businessId = :businessId")
    List<UsersBusiness> findByPasswordAndBusinessId(String password, Long businessId);
    @Query("SELECT u FROM UsersBusiness u WHERE u.password = :password AND u.business.businessId = :businessId")
    Optional<UsersBusiness> findByPasswordAndBusiness(String password, Long businessId);

    @Modifying
    @Query("UPDATE UsersBusiness u SET u.enable = :enable WHERE u.userBusinessId = :userBusinessId")
    void updateEnable(Long userBusinessId, boolean enable);

    @Modifying
    @Query("UPDATE UsersBusiness u SET u.download = :download WHERE u.userBusinessId != :userBusinessId")
    void updateAllDownloadExceptMe(Long userBusinessId, boolean download);

    @Query(value = """
        WITH HorasProgramadas AS (
            SELECT
                sc.userBusinessId,
                SUM(DATEDIFF(HOUR, sc.dateStart, sc.dateEnd)) AS horas_programadas,
                SUM(DATEDIFF(HOUR, sc.dateStart, sc.dateEnd) * ub.costHour) AS costo_programado
            FROM ScheduleCalendar sc
            JOIN UsersBusiness ub ON sc.userBusinessId = ub.userBusinessId
            WHERE sc.dateStart >= :startDate AND sc.dateEnd <= :endDate
            GROUP BY sc.userBusinessId, ub.costHour
        ),
        HorasTrabajadas AS (
            SELECT
                e.userBusinessId,
                SUM(e.hoursWorkeD) AS horas_trabajadas,
                SUM(e.totalWorkCost) AS costo_real
            FROM EntryExit e
            WHERE e.entry = 0
              AND e.date BETWEEN :startDate AND :endDate
            GROUP BY e.userBusinessId
        )
        SELECT
            ub.username,
            COALESCE(hp.horas_programadas, 0) AS horas_programadas,
            COALESCE(ht.horas_trabajadas, 0) AS horas_trabajadas,
            COALESCE(ht.horas_trabajadas, 0) - COALESCE(hp.horas_programadas, 0) AS diferencia_horas,
            COALESCE(hp.costo_programado, 0) AS costo_programado,
            COALESCE(ht.costo_real, 0) AS costo_real,
            COALESCE(ht.costo_real, 0) - COALESCE(hp.costo_programado, 0) AS diferencia_costo
        FROM UsersBusiness ub
        LEFT JOIN HorasProgramadas hp ON ub.userBusinessId = hp.userBusinessId
        LEFT JOIN HorasTrabajadas ht ON ub.userBusinessId = ht.userBusinessId
        ORDER BY ub.username;
        """, nativeQuery = true)
    List<Object[]> reporteHorasTrabajadasVsHorasProgramadas(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    @Query(value = """
    WITH HorasProgramadas AS (
        SELECT
            sc.userBusinessId,
            SUM(DATEDIFF(HOUR, sc.dateStart, sc.dateEnd)) AS horas_programadas,
            SUM(DATEDIFF(HOUR, sc.dateStart, sc.dateEnd) * ub.costHour) AS costo_programado
        FROM ScheduleCalendar sc
        JOIN UsersBusiness ub ON sc.userBusinessId = ub.userBusinessId
        WHERE sc.dateStart >= :startDate 
          AND sc.dateEnd <= :endDate
          AND ub.businessId = :businessId
        GROUP BY sc.userBusinessId, ub.costHour
    ),
    HorasTrabajadas AS (
        SELECT
            e.userBusinessId,
            SUM(e.hoursWorkeD) AS horas_trabajadas,
            SUM(e.totalWorkCost) AS costo_real
        FROM EntryExit e
        JOIN UsersBusiness ub ON e.userBusinessId = ub.userBusinessId
        WHERE e.entry = 0
          AND e.date BETWEEN :startDate AND :endDate
          AND ub.businessId = :businessId
        GROUP BY e.userBusinessId
    )
    SELECT
        ub.username,
        COALESCE(hp.horas_programadas, 0) AS horas_programadas,
        COALESCE(ht.horas_trabajadas, 0) AS horas_trabajadas,
        COALESCE(ht.horas_trabajadas, 0) - COALESCE(hp.horas_programadas, 0) AS diferencia_horas,
        COALESCE(hp.costo_programado, 0) AS costo_programado,
        COALESCE(ht.costo_real, 0) AS costo_real,
        COALESCE(ht.costo_real, 0) - COALESCE(hp.costo_programado, 0) AS diferencia_costo
    FROM UsersBusiness ub
    LEFT JOIN HorasProgramadas hp ON ub.userBusinessId = hp.userBusinessId
    LEFT JOIN HorasTrabajadas ht ON ub.userBusinessId = ht.userBusinessId
    WHERE ub.businessId = :businessId
    ORDER BY ub.username;
    """, nativeQuery = true)
List<Object[]> reporteHorasTrabajadasVsHorasProgramadas(
    @Param("startDate") LocalDate startDate, 
    @Param("endDate") LocalDate endDate,
    @Param("businessId") Long businessId);
    @Query(value = """
        WITH HorasProgramadas AS (
            SELECT
                sc.userBusinessId,
                SUM(DATEDIFF(HOUR, sc.dateStart, sc.dateEnd)) AS horas_programadas,
                SUM(DATEDIFF(HOUR, sc.dateStart, sc.dateEnd) * ub.costHour) AS costo_programado
            FROM ScheduleCalendar sc
            JOIN UsersBusiness ub ON sc.userBusinessId = ub.userBusinessId
            WHERE sc.dateStart >= :startDate 
              AND sc.dateEnd <= :endDate
              AND ub.businessId = :businessId
            GROUP BY sc.userBusinessId, ub.costHour
        ),
        HorasTrabajadas AS (
            SELECT
                e.userBusinessId,
                SUM(e.hoursWorkeD) AS horas_trabajadas,
                SUM(e.totalWorkCost) AS costo_real
            FROM EntryExit e
            JOIN UsersBusiness ub ON e.userBusinessId = ub.userBusinessId
            WHERE e.entry = 0
              AND e.date BETWEEN :startDate AND :endDate
              AND ub.businessId = :businessId
            GROUP BY e.userBusinessId
        )
        SELECT
            SUM(COALESCE(hp.horas_programadas, 0)) AS total_horas_programadas,
            SUM(COALESCE(ht.horas_trabajadas, 0)) AS total_horas_trabajadas,
            SUM(COALESCE(ht.horas_trabajadas, 0)) - SUM(COALESCE(hp.horas_programadas, 0)) AS diferencia_total_horas,
            SUM(COALESCE(hp.costo_programado, 0)) AS total_costo_programado,
            SUM(COALESCE(ht.costo_real, 0)) AS total_costo_real,
            SUM(COALESCE(ht.costo_real, 0)) - SUM(COALESCE(hp.costo_programado, 0)) AS diferencia_total_costo
        FROM
            (SELECT * FROM UsersBusiness WHERE businessId = :businessId) ub
        LEFT JOIN HorasProgramadas hp ON ub.userBusinessId = hp.userBusinessId
        LEFT JOIN HorasTrabajadas ht ON ub.userBusinessId = ht.userBusinessId;
        """, nativeQuery = true)
        List<Object[]> resumenHorasTrabajadasVsHorasProgramadas(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate,
        @Param("businessId") Long businessId);
    

    @Query(value = """
                WITH RawSchedule AS (
                    SELECT
                        ub.username, sc.userBusinessId,
                        DATENAME(WEEKDAY, sc.dateStart) AS WeekDayName,
                        RIGHT(CONVERT(VARCHAR(20), CONVERT(TIME, sc.dateStart), 100), 7) + ' - ' +
                        RIGHT(CONVERT(VARCHAR(20), CONVERT(TIME, sc.dateEnd), 100), 7) AS WorkHours
                    FROM ScheduleCalendar sc
                    JOIN UsersBusiness ub ON sc.userBusinessId = ub.userBusinessId
                    WHERE sc.dateStart BETWEEN :startDate AND :endDate
                      AND sc.userBusinessId = :employeeId
                ),
                AggregatedSchedule AS (
                    SELECT
                        username, userBusinessId,
                        WeekDayName,
                        STRING_AGG(WorkHours, ' | ') AS WorkHours
                    FROM RawSchedule
                    GROUP BY username, userBusinessId, WeekDayName
                ),
                PivotedSchedule AS (
                    SELECT
                        username, userBusinessId,
                        ISNULL([Monday], 'Off') AS Monday,
                        ISNULL([Tuesday], 'Off') AS Tuesday,
                        ISNULL([Wednesday], 'Off') AS Wednesday,
                        ISNULL([Thursday], 'Off') AS Thursday,
                        ISNULL([Friday], 'Off') AS Friday,
                        ISNULL([Saturday], 'Off') AS Saturday,
                        ISNULL([Sunday], 'Off') AS Sunday
                    FROM AggregatedSchedule
                    PIVOT (
                        MAX(WorkHours)
                        FOR WeekDayName IN ([Monday], [Tuesday], [Wednesday], [Thursday], [Friday], [Saturday], [Sunday])
                    ) AS PivotTable
                )
                SELECT * FROM PivotedSchedule
                ORDER BY username, userBusinessId
            """, nativeQuery = true)
    List<Object[]> getUserWeeklySchedule(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("employeeId") Long employeeId);

    @Query(value = """
                WITH RawSchedule AS (
                    SELECT
                        ub.username, sc.userBusinessId,
                        DATENAME(WEEKDAY, sc.dateStart) AS WeekDayName,
                        RIGHT(CONVERT(VARCHAR(20), CONVERT(TIME, sc.dateStart), 100), 7) + ' - ' +
                        RIGHT(CONVERT(VARCHAR(20), CONVERT(TIME, sc.dateEnd), 100), 7) AS WorkHours
                    FROM ScheduleCalendar sc
                    JOIN UsersBusiness ub ON sc.userBusinessId = ub.userBusinessId
                    WHERE sc.dateStart BETWEEN :startDate AND :endDate
                ),
                AggregatedSchedule AS (
                    SELECT
                        username, userBusinessId,
                        WeekDayName,
                        STRING_AGG(WorkHours, ' | ') AS WorkHours
                    FROM RawSchedule
                    GROUP BY username, userBusinessId, WeekDayName
                ),
                PivotedSchedule AS (
                    SELECT
                        username, userBusinessId,
                        ISNULL([Monday], 'Off') AS Monday,
                        ISNULL([Tuesday], 'Off') AS Tuesday,
                        ISNULL([Wednesday], 'Off') AS Wednesday,
                        ISNULL([Thursday], 'Off') AS Thursday,
                        ISNULL([Friday], 'Off') AS Friday,
                        ISNULL([Saturday], 'Off') AS Saturday,
                        ISNULL([Sunday], 'Off') AS Sunday
                    FROM AggregatedSchedule
                    PIVOT (
                        MAX(WorkHours)
                        FOR WeekDayName IN ([Monday], [Tuesday], [Wednesday], [Thursday], [Friday], [Saturday], [Sunday])
                    ) AS PivotTable
                )
                SELECT * FROM PivotedSchedule
                ORDER BY username, userBusinessId
            """, nativeQuery = true)
    List<Object[]> getUserWeeklySchedule(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = """
                WITH DetalleHoras AS (
                    SELECT
                        sc.userBusinessId,
                        ub.username,
                        CONVERT(DATE, sc.dateStart) AS Fecha,
                        CASE
                            WHEN CAST(sc.dateStart AS TIME) < '12:00' THEN 'Mañana'
                            ELSE 'Tarde'
                        END AS Turno,
                        FORMAT(sc.dateStart, 'h:mm tt', 'en-us') + ' - ' + FORMAT(sc.dateEnd, 'h:mm tt', 'en-us') AS Horario,
                        DATEDIFF(MINUTE, sc.dateStart, sc.dateEnd) / 60.0 AS Horas
                    FROM ScheduleCalendar sc
                    JOIN UsersBusiness ub ON sc.userBusinessId = ub.userBusinessId
                    WHERE sc.userBusinessId = :userBusinessId
                      AND sc.dateStart BETWEEN :startDate AND :endDate
                ),
                HorasPorDia AS (
                    SELECT
                        userBusinessId,
                        Fecha,
                        SUM(Horas) AS HorasxDia
                    FROM DetalleHoras
                    GROUP BY userBusinessId, Fecha
                ),
                TotalSemana AS (
                    SELECT
                        userBusinessId,
                        SUM(Horas) AS TotalHorasSemana
                    FROM DetalleHoras
                    GROUP BY userBusinessId
                )
                SELECT
                    d.username,
                    d.Fecha,
                    d.Turno,
                    d.Horario,
                    d.Horas,
                    hpd.HorasxDia,
                    ts.TotalHorasSemana,
                    d.userBusinessId
                FROM DetalleHoras d
                JOIN HorasPorDia hpd ON d.userBusinessId = hpd.userBusinessId AND d.Fecha = hpd.Fecha
                JOIN TotalSemana ts ON d.userBusinessId = ts.userBusinessId
                ORDER BY d.Fecha, d.Turno;
            """, nativeQuery = true)
    List<Object[]> getEmployeeWeeklyScheduleDetailByEmployee(
            @Param("userBusinessId") Long userBusinessId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
            @Query(value = """
    WITH DetalleHoras AS (
        SELECT
            sc.userBusinessId,
            ub.username,
            CONVERT(DATE, sc.dateStart) AS Fecha,
            CASE
                WHEN CAST(sc.dateStart AS TIME) < '12:00' THEN 'Mañana'
                ELSE 'Tarde'
            END AS Turno,
            FORMAT(sc.dateStart, 'h:mm tt', 'en-us') + ' - ' + FORMAT(sc.dateEnd, 'h:mm tt', 'en-us') AS Horario,
            DATEDIFF(MINUTE, sc.dateStart, sc.dateEnd) / 60.0 AS Horas
        FROM ScheduleCalendar sc
        JOIN UsersBusiness ub ON sc.userBusinessId = ub.userBusinessId
        WHERE sc.userBusinessId = :userBusinessId
          AND ub.businessId = :businessId
          AND sc.dateStart BETWEEN :startDate AND :endDate
    ),
    HorasPorDia AS (
        SELECT
            userBusinessId,
            Fecha,
            SUM(Horas) AS HorasxDia
        FROM DetalleHoras
        GROUP BY userBusinessId, Fecha
    ),
    TotalSemana AS (
        SELECT
            userBusinessId,
            SUM(Horas) AS TotalHorasSemana
        FROM DetalleHoras
        GROUP BY userBusinessId
    )
    SELECT
        d.username,
        d.Fecha,
        d.Turno,
        d.Horario,
        d.Horas,
        hpd.HorasxDia,
        ts.TotalHorasSemana,
        d.userBusinessId
    FROM DetalleHoras d
    JOIN HorasPorDia hpd ON d.userBusinessId = hpd.userBusinessId AND d.Fecha = hpd.Fecha
    JOIN TotalSemana ts ON d.userBusinessId = ts.userBusinessId
    ORDER BY d.Fecha, d.Turno
""", nativeQuery = true)
List<Object[]> getEmployeeWeeklyScheduleDetail(
    @Param("userBusinessId") Long userBusinessId,
    @Param("businessId") Long businessId,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate
);
@Query(value = """
    WITH DetalleHoras AS (
        SELECT
            sc.userBusinessId,
            ub.username,
            CONVERT(DATE, sc.dateStart) AS Fecha,
            CASE
                WHEN CAST(sc.dateStart AS TIME) < '12:00' THEN 'Mañana'
                ELSE 'Tarde'
            END AS Turno,
            FORMAT(sc.dateStart, 'h:mm tt', 'en-us') + ' - ' + FORMAT(sc.dateEnd, 'h:mm tt', 'en-us') AS Horario,
            DATEDIFF(MINUTE, sc.dateStart, sc.dateEnd) / 60.0 AS Horas
        FROM ScheduleCalendar sc
        JOIN UsersBusiness ub ON sc.userBusinessId = ub.userBusinessId
        WHERE ub.businessId = :businessId
          AND sc.dateStart BETWEEN :startDate AND :endDate
    ),
    HorasPorDia AS (
        SELECT
            userBusinessId,
            Fecha,
            SUM(Horas) AS HorasxDia
        FROM DetalleHoras
        GROUP BY userBusinessId, Fecha
    ),
    TotalSemana AS (
        SELECT
            userBusinessId,
            SUM(Horas) AS TotalHorasSemana
        FROM DetalleHoras
        GROUP BY userBusinessId
    )
    SELECT
        d.username,
        d.Fecha,
        d.Turno,
        d.Horario,
        d.Horas,
        hpd.HorasxDia,
        ts.TotalHorasSemana,
        d.userBusinessId
    FROM DetalleHoras d
    JOIN HorasPorDia hpd ON d.userBusinessId = hpd.userBusinessId AND d.Fecha = hpd.Fecha
    JOIN TotalSemana ts ON d.userBusinessId = ts.userBusinessId
    ORDER BY d.username, d.Fecha, d.Turno
""", nativeQuery = true)
List<Object[]> getEmployeesWeeklyScheduleByBusiness(
    @Param("businessId") Long businessId,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate
);


    @Query(value = """
                WITH DetalleHoras AS (
                    SELECT
                        sc.userBusinessId,
                        ub.username,
                        CONVERT(DATE, sc.dateStart) AS Fecha,
                        CASE
                            WHEN CAST(sc.dateStart AS TIME) < '12:00' THEN 'Mañana'
                            ELSE 'Tarde'
                        END AS Turno,
                        FORMAT(sc.dateStart, 'h:mm tt', 'en-us') + ' - ' + FORMAT(sc.dateEnd, 'h:mm tt', 'en-us') AS Horario,
                        DATEDIFF(MINUTE, sc.dateStart, sc.dateEnd) / 60.0 AS Horas
                    FROM ScheduleCalendar sc
                    JOIN UsersBusiness ub ON sc.userBusinessId = ub.userBusinessId
                    WHERE sc.dateStart BETWEEN :startDate AND :endDate
                ),
                HorasPorDia AS (
                    SELECT
                        userBusinessId,
                        Fecha,
                        SUM(Horas) AS HorasxDia
                    FROM DetalleHoras
                    GROUP BY userBusinessId, Fecha
                ),
                TotalSemana AS (
                    SELECT
                        userBusinessId,
                        SUM(Horas) AS TotalHorasSemana
                    FROM DetalleHoras
                    GROUP BY userBusinessId
                )
                SELECT
                    d.username,
                    d.Fecha,
                    d.Turno,
                    d.Horario,
                    d.Horas,
                    hpd.HorasxDia,
                    ts.TotalHorasSemana,
                    d.userBusinessId
                FROM DetalleHoras d
                JOIN HorasPorDia hpd ON d.userBusinessId = hpd.userBusinessId AND d.Fecha = hpd.Fecha
                JOIN TotalSemana ts ON d.userBusinessId = ts.userBusinessId
                ORDER BY d.username, d.Fecha, d.Turno;
            """, nativeQuery = true)
    List<Object[]> getAllEmployeesWeeklyScheduleDetail(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

}
