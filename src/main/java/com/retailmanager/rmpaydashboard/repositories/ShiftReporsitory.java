package com.retailmanager.rmpaydashboard.repositories;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.SaleReportProjection;
import com.retailmanager.rmpaydashboard.models.Shift;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;
import com.retailmanager.rmpaydashboard.models.enums.SyncStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShiftReporsitory extends CrudRepository<Shift, String>, PagingAndSortingRepository<Shift, String> {

    @Query(value = "select count(*) from RMPAY.dbo.Shift s join RMPAY.dbo.Terminal t on s.terminalId = t.terminalId where t.businessId = :businessId and t.serial = :serial and s.openShifBalance = :openShiftBalance", nativeQuery = true)
    int countShiftByBusinessIdAndSerialAndOpenShift(Long businessId, String serial, boolean openShiftBalance);

    @Query(value = "select   " +
            "  ISNULL(SUM(CASE   " +
            "        WHEN paymentType = 'ATH'  " +
            "         AND sa.saleTransactionType = 'SALE'  " +
            "         AND sa.saleStatus = 'SUCCEED'  " +
            "        THEN sa.saleTotalAmount   " +
            "        ELSE 0   " +
            "      END), 0) AS saleATH,  " +
            "   ISNULL(SUM(CASE   " +
            "        WHEN paymentType = 'CASH'  " +
            "         AND sa.saleTransactionType = 'SALE'  " +
            "         AND sa.saleStatus = 'SUCCEED'  " +
            "        THEN sa.saleTotalAmount   " +
            "        ELSE 0   " +
            "      END), 0) AS saleCash,  " +
            "  ISNULL(SUM(CASE   " +
            "        WHEN paymentType = 'DEBIT'  " +
            "         AND sa.saleTransactionType = 'SALE'  " +
            "         AND sa.saleStatus = 'SUCCEED'  " +
            "        THEN sa.saleTotalAmount   " +
            "        ELSE 0   " +
            "      END), 0) AS saleDebit,  " +
            "  ISNULL(SUM(CASE   " +
            "        WHEN paymentType = 'CREDIT'  " +
            "         AND sa.saleTransactionType = 'SALE'  " +
            "         AND sa.saleStatus = 'SUCCEED'  " +
            "        THEN sa.saleTotalAmount   " +
            "        ELSE 0   " +
            "      END), 0) AS saleCredit,  " +
            "  ISNULL(SUM(CASE   " +
            "        WHEN paymentType = 'ATH'  " +
            "         AND sa.saleTransactionType = 'SALE'  " +
            "         AND sa.saleStatus = 'REFUNDED'  " +
            "        THEN sa.saleTotalAmount   " +
            "        ELSE 0   " +
            "      END), 0) AS refundATH,  " +
            "   ISNULL(SUM(CASE   " +
            "        WHEN paymentType = 'DEBIT'  " +
            "         AND sa.saleTransactionType = 'SALE'  " +
            "         AND sa.saleStatus = 'REFUNDED'  " +
            "        THEN sa.saleTotalAmount   " +
            "        ELSE 0   " +
            "      END), 0) AS refundDebit,  " +
            "\tISNULL(SUM(CASE   " +
            "        WHEN paymentType = 'CASH'  " +
            "         AND sa.saleTransactionType = 'SALE'  " +
            "         AND sa.saleStatus = 'REFUNDED'  " +
            "        THEN sa.saleTotalAmount   " +
            "        ELSE 0   " +
            "      END), 0) AS refundCash,  " +
            "  ISNULL(SUM(CASE   " +
            "        WHEN paymentType = 'CREDIT'  " +
            "         AND sa.saleTransactionType = 'SALE'  " +
            "         AND sa.saleStatus = 'REFUNDED'  " +
            "        THEN sa.saleTotalAmount   " +
            "        ELSE 0   " +
            "      END), 0) AS refundCredit,  " +
            "  ISNULL(SUM(CASE   " +
            "        WHEN sa.saleTransactionType = 'SALE'  " +
            "         AND sa.saleStatus = 'SUCCEED'  " +
            "        THEN sa.saleStateTaxAmount  " +
            "        ELSE 0   " +
            "      END), 0) AS stateTax,  " +
            "  ISNULL(SUM(CASE   " +
            "        WHEN sa.saleTransactionType = 'SALE'  " +
            "         AND sa.saleStatus = 'SUCCEED'  " +
            "        THEN sa.saleCityTaxAmount  " +
            "        ELSE 0   " +
            "      END), 0) AS cityTax,  " +
            "  ISNULL(SUM(CASE   " +
            "        WHEN sa.saleTransactionType = 'SALE'  " +
            "         AND sa.saleStatus = 'SUCCEED'  " +
            "        THEN sa.saleReduceTax  " +
            "        ELSE 0   " +
            "      END), 0) AS reduceTax " +
            "from rmpay.dbo.Shift sh  " +
            "join rmpay.dbo.sale sa on sh.userBusinessId = sa.userId  " +
            "join rmpay.dbo.Transactions t on sa.saleID = t.saleId  " +
            "where 1=1  " +
            "and sa.userId = ?1 and sh.shiftId = ?2 " +
            "and sa.saleEndDate >= sh.startTime AND sa.saleEndDate <= ?3 ", nativeQuery = true)
    SaleReportProjection findSaleReport(Long userID, String shiftId, Instant endTime);

    @Modifying
    @Transactional
    @Query("UPDATE Shift s SET s.syncStatus = :status, s.lastSyncAt = :now WHERE s.shiftId = :shiftId")
    int updateStatus(String shiftId, SyncStatus status, Instant now);

    @Query("select s from Shift s where s.syncStatus IN ('PENDING', 'ERROR', 'MISSING') and  s.terminal.terminalId = :terminalId")
    List<Shift> findAllShift(String terminalId);

    @Query("select s from Shift s where s.userBusiness.userBusinessId = :userBusinessId and s.terminal.terminalId = :terminalId")
    public Shift findByEmployeeAndTerminal(Long userBusinessId, String terminalId);

    public Optional<Shift> findFirstByUserBusinessAndTerminal(UsersBusiness Employee, Terminal terminal);

    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific UsersBusiness
     * and Terminal, and have a start time within the specified date and time range.
     *
     * @param userBusiness the UsersBusiness entity to filter by.
     * @param terminal     the Terminal entity to filter by.
     * @param startTime    the start of the date-time range to filter shifts.
     * @param endTime      the end of the date-time range to filter shifts.
     * @param pageable     the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */

    public Page<Shift> findByUserBusinessAndTerminalAndStartTimeBetween(
            UsersBusiness userBusiness,
            Terminal terminal,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable
    );

    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific
     * UsersBusiness and Terminal.
     *
     * @param userBusiness the UsersBusiness entity to filter by.
     * @param terminal     the Terminal entity to filter by.
     * @param pageable     the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */

    public Page<Shift> findByUserBusinessAndTerminal(
            UsersBusiness userBusiness, Terminal terminal, Pageable pageable);

    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific
     * UsersBusiness and have a start time within the specified date and time range.
     *
     * @param userBusiness the UsersBusiness entity to filter by.
     * @param startTime    the start of the date-time range to filter shifts.
     * @param endTime      the end of the date-time range to filter shifts.
     * @param pageable     the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */

    public Page<Shift> findByUserBusinessAndStartTimeBetween(
            UsersBusiness userBusiness,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable
    );

    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific
     * Terminal and have a start time within the specified date and time range.
     *
     * @param terminal  the Terminal entity to filter by.
     * @param startTime the start of the date-time range to filter shifts.
     * @param endTime   the end of the date-time range to filter shifts.
     * @param pageable  the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */
    public Page<Shift> findByTerminalAndStartTimeBetween(
            Terminal terminal, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific
     * UsersBusiness.
     *
     * @param userBusiness the UsersBusiness entity to filter by.
     * @param pageable     the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */
    public Page<Shift> findByUserBusiness(
            UsersBusiness userBusiness, Pageable pageable);

    /**
     * Retrieves a paginated list of Shift entities that are associated with a specific
     * Terminal.
     *
     * @param terminal the Terminal entity to filter by.
     * @param pageable the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */
    public Page<Shift> findByTerminal(
            Terminal terminal, Pageable pageable);

    /**
     * Retrieves a paginated list of Shift entities that have a start time within the
     * specified date and time range.
     *
     * @param startTime the start of the date-time range to filter shifts.
     * @param endTime   the end of the date-time range to filter shifts.
     * @param pageable  the Pageable object containing pagination information.
     * @return a Page of Shift entities matching the criteria.
     */
    public Page<Shift> findByStartTimeBetween(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable
    );

    Page<Shift> findByUserBusinessAndTerminalAndStartTimeBetweenAndOpenShifBalance(UsersBusiness userBusiness, Terminal terminal, LocalDateTime start, LocalDateTime end, Boolean statusShiftBalance, Pageable pageable);

    Page<Shift> findByUserBusinessAndTerminalAndOpenShifBalance(UsersBusiness userBusiness, Terminal terminal, Boolean statusShiftBalance, Pageable pageable);

    Page<Shift> findByUserBusinessAndStartTimeBetweenAndOpenShifBalance(UsersBusiness userBusiness, LocalDateTime start, LocalDateTime end, Boolean statusShiftBalance, Pageable pageable);

    Page<Shift> findByTerminalAndStartTimeBetweenAndOpenShifBalance(Terminal terminal, LocalDateTime start, LocalDateTime end, Boolean statusShiftBalance, Pageable pageable);

    Page<Shift> findByUserBusinessAndOpenShifBalance(UsersBusiness userBusiness, Boolean statusShiftBalance, Pageable pageable);

    Page<Shift> findByTerminalAndOpenShifBalance(Terminal terminal, Boolean statusShiftBalance, Pageable pageable);

    Page<Shift> findByStartTimeBetweenAndOpenShifBalance(LocalDateTime start, LocalDateTime end, Boolean statusShiftBalance, Pageable pageable);

    Page<Shift> findByOpenShifBalance(Boolean statusShiftBalance, Pageable pageable); // Nuevo caso base

    // --- Métodos de Repositorio que incluyen Business como filtro obligatorio y OpenShifBalance ---

    // 1. Solo por Business
    Page<Shift> findByTerminal_Business(Business business, Pageable pageable);

    // 2. Business y UserBusiness
    Page<Shift> findByTerminal_BusinessAndUserBusiness(Business business, UsersBusiness userBusiness, Pageable pageable);

    // 3. Business y Terminal
    Page<Shift> findByTerminal_BusinessAndTerminal(Business business, Terminal terminal, Pageable pageable);

    // 4. Business y StartTimeBetween
    Page<Shift> findByTerminal_BusinessAndStartTimeBetween(Business business, Instant startDateTime, Instant endDateTime, Pageable pageable);

    // 5. Business y OpenShifBalance (Vuelto a OpenShifBalance)
    Page<Shift> findByTerminal_BusinessAndOpenShifBalance(Business business, Boolean openShifBalance, Pageable pageable);

    // 6. Business, UserBusiness y Terminal
    Page<Shift> findByTerminal_BusinessAndUserBusinessAndTerminal(Business business, UsersBusiness userBusiness, Terminal terminal, Pageable pageable);

    // 7. Business, UserBusiness y StartTimeBetween
    Page<Shift> findByTerminal_BusinessAndUserBusinessAndStartTimeBetween(Business business, UsersBusiness userBusiness, Instant startDateTime, Instant endDateTime, Pageable pageable);

    // 8. Business, Terminal y StartTimeBetween
    Page<Shift> findByTerminal_BusinessAndTerminalAndStartTimeBetween(Business business, Terminal terminal, Instant startDateTime, Instant endDateTime, Pageable pageable);

    // 9. Business, UserBusiness y OpenShifBalance
    Page<Shift> findByTerminal_BusinessAndUserBusinessAndOpenShifBalance(Business business, UsersBusiness userBusiness, Boolean openShifBalance, Pageable pageable);

    // 10. Business, Terminal y OpenShifBalance
    Page<Shift> findByTerminal_BusinessAndTerminalAndOpenShifBalance(Business business, Terminal terminal, Boolean openShifBalance, Pageable pageable);

    // 11. Business, StartTimeBetween y OpenShifBalance
    Page<Shift> findByTerminal_BusinessAndStartTimeBetweenAndOpenShifBalance(Business business, Instant startDateTime, Instant endDateTime, Boolean openShifBalance, Pageable pageable);

    // 12. Business, UserBusiness, Terminal y StartTimeBetween
    Page<Shift> findByTerminal_BusinessAndUserBusinessAndTerminalAndStartTimeBetween(Business business, UsersBusiness userBusiness, Terminal terminal, Instant startDateTime, Instant endDateTime, Pageable pageable);

    // 13. Business, UserBusiness, Terminal y OpenShifBalance
    Page<Shift> findByTerminal_BusinessAndUserBusinessAndTerminalAndOpenShifBalance(Business business, UsersBusiness userBusiness, Terminal terminal, Boolean openShifBalance, Pageable pageable);

    // 14. Business, UserBusiness, StartTimeBetween y OpenShifBalance
    Page<Shift> findByTerminal_BusinessAndUserBusinessAndStartTimeBetweenAndOpenShifBalance(Business business, UsersBusiness userBusiness, Instant startDateTime, Instant endDateTime, Boolean openShifBalance, Pageable pageable);

    // 15. Business, Terminal, StartTimeBetween y OpenShifBalance
    Page<Shift> findByTerminal_BusinessAndTerminalAndStartTimeBetweenAndOpenShifBalance(Business business, Terminal terminal, Instant startDateTime, Instant endDateTime, Boolean openShifBalance, Pageable pageable);

    // 16. Business, UserBusiness, Terminal, StartTimeBetween y OpenShifBalance (la combinación más completa)
    Page<Shift> findByTerminal_BusinessAndUserBusinessAndTerminalAndStartTimeBetweenAndOpenShifBalance(Business business, UsersBusiness userBusiness, Terminal terminal, Instant startDateTime, Instant endDateTime, Boolean openShifBalance, Pageable pageable);
}   
