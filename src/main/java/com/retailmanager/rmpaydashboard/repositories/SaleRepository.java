package com.retailmanager.rmpaydashboard.repositories;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.ItemForSale;
import com.retailmanager.rmpaydashboard.models.Sale;
import com.retailmanager.rmpaydashboard.models.Interface.DailySalesProjection;
import com.retailmanager.rmpaydashboard.models.Interface.HourlySalesProjection;
import com.retailmanager.rmpaydashboard.models.Interface.LaborMetricsProjection;
import com.retailmanager.rmpaydashboard.models.Interface.ShiftTransactionProjection;
import com.retailmanager.rmpaydashboard.models.Interface.TransactionDetailProjection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends CrudRepository<Sale, String> {
    /**
     * Obtiene una lista de ventas por identificador de comercio.
     *
     * @param merchantId identificador de comercio
     * @return listado de ventas
     */
    //
    @Query("SELECT s FROM Sale s WHERE s.business.merchantId = :merchantId")
    public List<Sale> findByMerchantId(String merchantId);

    /**
     * Obtiene una lista de ventas por tipo de transacción, estado y identificador de comercio.
     *
     * @param saleTransactionType tipo de transacción
     * @param saleStatus          estado
     * @param merchantId          identificador de comercio
     * @return listado de ventas
     */
    //
    public List<Sale> findBySaleTransactionTypeAndSaleStatusAndBusiness(String saleTransactionType, String saleStatus, Business business);

    /**
     * Obtiene una lista de ventas por tipo de transacción y identificador de comercio.
     *
     * @param saleTransactionType tipo de transacción
     * @param merchantId          identificador de comercio
     * @return listado de ventas
     */
    //
    public List<Sale> findBySaleTransactionTypeAndBusiness(String saleTransactionType, Business business);

    public List<Sale> findByBusiness(Business business);

    @Query("SELECT s FROM Sale s WHERE s.saleTransactionType = :saleTransactionType AND s.terminal.terminalId = :terminalId AND s.business.merchantId = :merchantId")
    public List<Sale> findByMerchantIdAndTerminalId(String saleTransactionType, String terminalId, String merchantId);

    @Query("SELECT s FROM Sale s WHERE s.terminal.terminalId = :terminalId AND s.business.merchantId = :merchantId")
    public List<Sale> findByMerchantIdAndTerminalId(String terminalId, String merchantId);

    /**
     * Obtiene las ventas entre dos fechas y por tipo de transacción y estado e identificador de comercio.
     *
     * @param startDate           fecha de inicio
     * @param endDate             fecha de fin
     * @param saleTransactionType tipo de transacción
     * @param saleStatus          estado
     * @param business            identificador de comercio
     * @return listado de ventas
     */
    //
    public List<Sale> findBySaleEndDateBetweenAndSaleTransactionTypeAndSaleStatusAndBusiness(LocalDate startDate, LocalDate endDate, String saleTransactionType, String saleStatus, Business business);


    ///FUNCIONES DE REPORTES
    //TODO: MODIFICAR EL CALCULO DE REFUND DE ACUERDO AL TIPO DE TRANSACCION

    /**
     * Retrieves daily summary data for a specific business.
     *
     * @param businessId the ID of the business
     * @return an array containing total sales, total refund, state tax, city tax, and reduced tax
     */
    @Query(value = " SELECT \r\n" +
            "  (SELECT sum(saleTotalAmount) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where saleEndDate BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId ) as totalSales,\r\n" +
            "  \r\n" +
            "  (SELECT sum(s2.saleTotalAmount) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] s1 JOIN [RMPAY].[dbo].[Sale] s2 ON s1.saleID = s2.saleToRefund WHERE s1.saleEndDate BETWEEN :startDate AND :endDate AND s1.businessId = :businessId )as totalRefund,\r\n" +
            "\r\n" +
            "  (SELECT sum(saleStateTaxAmount)\r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where saleEndDate BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId ) as stateTax,\r\n" +
            "\r\n" +
            "  (SELECT sum(saleCityTaxAmount) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where saleEndDate BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId )  as cityTax,\r\n" +
            "\r\n" +
            "  (SELECT sum(saleReduceTax) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where saleEndDate BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId  ) as redTax, " +
            " (SELECT sum(saleSubtotal) " +
            "  FROM [RMPAY].[dbo].[Sale] where saleEndDate BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId  ) as subTotalSales, " +
            " (SELECT  SUM(it.grossProfit) AS profit FROM [RMPAY].[dbo].[ItemForSale] it JOIN [RMPAY].[dbo].[Sale] s ON it.saleID = s.saleID WHERE s.saleEndDate BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' AND s.businessId = :businessId) as  grossBenefit, " +
            " (SELECT  SUM(s.tipAmount) from [RMPAY].[dbo].[Sale] s  WHERE s.saleEndDate BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' AND s.businessId = :businessId) as totalTips ", nativeQuery = true)
    public Object[] dailySummary(Long businessId, Instant startDate, Instant endDate);


    @Query(value = " SELECT \r\n" +
            "  (SELECT sum(saleTotalAmount) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId ) as totalSales,\r\n" +
            "  \r\n" +
            "  (SELECT sum(s2.saleTotalAmount) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] s1 JOIN [RMPAY].[dbo].[Sale] s2 ON s1.saleID = s2.saleToRefund WHERE CAST(s1.saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND s1.businessId = :businessId )as totalRefund,\r\n" +
            "\r\n" +
            "  (SELECT sum(saleStateTaxAmount)\r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId ) as stateTax,\r\n" +
            "\r\n" +
            "  (SELECT sum(saleCityTaxAmount) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId )  as cityTax,\r\n" +
            "\r\n" +
            "  (SELECT sum(saleReduceTax) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId  ) as redTax, " +
            " (SELECT sum(saleSubtotal) " +
            "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId  ) as subTotalSales, " +
            " (SELECT  SUM(it.grossProfit) AS profit FROM [RMPAY].[dbo].[ItemForSale] it JOIN [RMPAY].[dbo].[Sale] s ON it.saleID = s.saleID WHERE CAST(s.saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' AND s.businessId = :businessId) as  grossBenefit, " +
            " (SELECT  SUM(s.tipAmount) from [RMPAY].[dbo].[Sale] s  WHERE CAST(s.saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' AND s.businessId = :businessId) as totalTips ", nativeQuery = true)
    public Object[] dailySummary(Long businessId, LocalDate startDate, LocalDate endDate);


    @Query(value = " SELECT \r\n" +
            "  (SELECT sum(saleTotalAmount) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where YEAR(saleEndDate) = :year AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId ) as totalSales,\r\n" +
            "  \r\n" +
            "  (SELECT sum(saleTotalAmount) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where YEAR(saleEndDate) = :year AND saleTransactionType IN ('REFUND','PARTIAL_REFUND') AND saleStatus IN ('REFUNDED','PARTIAL_REFUNDED') and businessId=:businessId )as totalRefund,\r\n" +
            "\r\n" +
            "  (SELECT sum(saleStateTaxAmount)\r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where YEAR(saleEndDate) = :year AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId ) as stateTax,\r\n" +
            "\r\n" +
            "  (SELECT sum(saleCityTaxAmount) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where YEAR(saleEndDate) = :year AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId )  as cityTax,\r\n" +
            "\r\n" +
            "  (SELECT sum(saleReduceTax) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where YEAR(saleEndDate) = :year AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId  ) as redTax, " +
            " (SELECT sum(saleSubtotal) " +
            "  FROM [RMPAY].[dbo].[Sale] where YEAR(saleEndDate) = :year AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId  ) as subTotalSales, " +
            " (SELECT  SUM(it.grossProfit) AS profit FROM [RMPAY].[dbo].[ItemForSale] it JOIN [RMPAY].[dbo].[Sale] s ON it.saleID = s.saleID WHERE YEAR(s.saleEndDate) = :year AND saleTransactionType='SALE'AND saleStatus='SUCCEED' AND s.businessId = :businessId) as  grossBenefit, " +
            " (SELECT  SUM(s.tipAmount) from [RMPAY].[dbo].[Sale] s  WHERE year(s.saleEndDate) = :year AND saleTransactionType='SALE'AND saleStatus='SUCCEED' AND s.businessId = :businessId) as totalTips, " +
            " ( SELECT sum(e.totalWorkCost) FROM [RMPAY].[dbo].[EntryExit] e inner join [RMPAY].[dbo].[UsersBusiness] ub on e.userBusinessId=ub.userBusinessId where year(e.date) = :year and ub.businessId=:businessId) as totalWorkCost", nativeQuery = true)
    public Object[] annualSummary(Long businessId, int year);

    @Query(value = " SELECT \r\n" +
            "  (SELECT sum(saleTotalAmount) \r\n" +
            "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) = :fecha AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId ) as totalSales", nativeQuery = true)
    public Object[] monthlySummary(Long businessId, LocalDate fecha);

    /**
     * Generate daily summary for a specific category.
     *
     * @param businessId the ID of the business
     * @return an array containing the daily summary for the category
     */
    @Query(value = "  SELECT category, sum(s.saleTotalAmount) as totalSales " +
            " FROM [RMPAY].[dbo].[ItemForSale] it join [RMPAY].[dbo].[Sale] s on it.saleID=s.saleID  where s.saleEndDate >=:startUtc AND s.saleEndDate < :endUtc and s.businessId=:businessId " +
            " group by category order by sum(s.saleTotalAmount) desc", nativeQuery = true)
    Object[] dailySummaryForCategory(Long businessId, Instant startUtc, Instant endUtc);

    /**
     * Generate daily summary for a specific category.
     *
     * @param businessId the ID of the business
     * @return an array containing the daily summary for the category
     */
    @Query(value = "  SELECT category, sum(s.saleTotalAmount) as totalSales " +
            " FROM [RMPAY].[dbo].[ItemForSale] it join [RMPAY].[dbo].[Sale] s on it.saleID=s.saleID  " +
            " where CAST(s.saleEndDate AS date) BETWEEN :startDate AND :endDate and s.businessId=:businessId " +
            " group by category order by sum(s.saleTotalAmount) desc", nativeQuery = true)
    Object[] summaryForCategory(Long businessId, LocalDate startDate, LocalDate endDate);

    @Query(value = "  SELECT category, sum(s.saleTotalAmount) as totalSales " +
            " FROM [RMPAY].[dbo].[ItemForSale] it join [RMPAY].[dbo].[Sale] s on it.saleID=s.saleID  " +
            " where s.saleEndDate >= :startUtc AND s.saleEndDate < :endUtc and s.businessId=:businessId " +
            " group by category order by sum(s.saleTotalAmount) desc", nativeQuery = true)
    Object[] summaryForCategory(Long businessId, Instant startUtc, Instant endUtc);

    /**
     * Obtiene una lista de los productos mas vendidos en orden descendente
     *
     * @param businessId identificador del negocio
     * @return
     */
    @Query(value = "SELECT productId, sum(it.quantity) as quantity,sum(it.quantity*it.price) as totalAmount, sum(it.grossProfit) as profit, (select top(1) name from [RMPAY].[dbo].[ItemForSale] ift where ift.productId=it.productId) as name,  it.price,  it.category " + //
            "  FROM [RMPAY].[dbo].[ItemForSale] it join [RMPAY].[dbo].[Sale] s on it.saleID=s.saleID  \r\n" + //
            "  where CAST(s.saleEndDate AS DATE) BETWEEN :startDate AND :endDate and s.businessId=:businessId \r\n" +
            "  AND saleTransactionType = 'SALE' AND saleStatus = 'SUCCEED' " + //
            "  group by productId,  it.category,  it.price " + //
            "  order by sum(it.quantity) desc ", nativeQuery = true)
    public Object[] dailySummaryBestSellingItems(Long businessId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT productId, sum(it.quantity) as quantity,sum(it.quantity*it.price) as totalAmount, sum(it.grossProfit) as profit, (select top(1) name from [RMPAY].[dbo].[ItemForSale] ift where ift.productId=it.productId) as name,  it.price,  it.category " + //
            "  FROM [RMPAY].[dbo].[ItemForSale] it join [RMPAY].[dbo].[Sale] s on it.saleID=s.saleID  \r\n" + //
            "  where s.saleEndDate  >= :startDate AND s.saleEndDate < :endDate and s.businessId=:businessId \r\n" +
            "  AND saleTransactionType = 'SALE' AND saleStatus = 'SUCCEED' " + //
            "  group by productId,  it.category,  it.price " + //
            "  order by sum(it.quantity) desc ", nativeQuery = true)
    public Object[] dailySummaryBestSellingItems(Long businessId, Instant startDate, Instant endDate);

    /**
     * Obtiene una lista de ventas por tipo de transacción, estado, fecha y negocio al que pertenece
     *
     * @param saleTransactionType tipo de transacción
     * @param saleStatus          estado de la transacción
     * @param business            negocio que realiza la venta
     * @return
     */
    //
    public List<Sale> findBySaleTransactionTypeAndSaleStatusAndBusinessAndSaleEndDateBetween(String saleTransactionType, String saleStatus, Business business, LocalDateTime startDate, LocalDateTime endDate);

    public List<Sale> findBySaleTransactionTypeAndSaleStatusAndBusinessAndSaleEndDateBetween(String saleTransactionType, String saleStatus, Business business, Instant startUtc, Instant endUtc);

    @Query(value = "  SELECT t.paymentType, sum(t.amount) as totalAmount\r\n" + //
            "  FROM [RMPAY].[dbo].[Sale] S INNER JOIN [RMPAY].[dbo].[Transactions] t ON s.saleID=t.saleId  \r\n" + //
            "  WHERE saleEndDate BETWEEN :startDate AND :endDate AND s.businessId=:businessId and s.saleTransactionType in ('REFUND','PARTIAL_REFUND') AND s.saleStatus IN ('REFUNDED','PARTIAL_REFUNDED')\r\n" + //
            "  group by paymentType\r\n" + //
            "  ORDER BY totalAmount DESC", nativeQuery = true)
    Object[] refundSumaryByRange(Long businessId, LocalDate startDate, LocalDate endDate);

    @Query(value = "  SELECT t.paymentType, sum(t.amount) as totalAmount\r\n" + //
            "  FROM [RMPAY].[dbo].[Sale] S INNER JOIN [RMPAY].[dbo].[Transactions] t ON s.saleID=t.saleId  \r\n" + //
            "  WHERE s.saleEndDate >= :startDate AND s.saleEndDate < :endDate AND s.businessId=:businessId and s.saleTransactionType in ('REFUND','PARTIAL_REFUND') AND s.saleStatus IN ('SUCCEED')\r\n" + //
            "  group by paymentType\r\n" + //
            "  ORDER BY totalAmount DESC", nativeQuery = true)
    Object[] refundSumaryByRange(Long businessId, Instant startDate, Instant endDate);


    /**
     * Retrieves a list of ItemForSale objects based on the provided businessId.
     *
     * @param businessId the ID of the business
     * @return a list of ItemForSale objects associated with the business
     */
    @Query(value = "select i from ItemForSale i where i.sale.business.businessId=:businessId and i.sale.saleEndDate between :startDate and :endDate order by i.quantity desc")
    List<ItemForSale> getBestSellingItems(Long businessId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "select i from ItemForSale i where i.sale.business.businessId=:businessId and i.sale.saleEndDate >= :startDate and i.sale.saleEndDate < :endDate order by i.quantity desc")
    List<ItemForSale> getBestSellingItems(Long businessId, Instant startDate, Instant endDate);

    /**
     * Retrieves the best selling items by category within a specified date range for a given business.
     *
     * @param businessId the ID of the business
     * @param startDate  the start date of the date range
     * @param endDate    the end date of the date range
     * @param category   the category of items to retrieve
     * @return a list of ItemForSale objects representing the best selling items by category
     */
    @Query(value = "select i from ItemForSale i where i.sale.business.businessId=:businessId and i.sale.saleEndDate between :startDate and :endDate  and i.category=:category order by i.quantity desc")
    List<ItemForSale> getBestSellingItemsByCategory(Long businessId, LocalDateTime startDate, LocalDateTime endDate, String category);

    @Query(value = "select i from ItemForSale i where i.sale.business.businessId=:businessId and i.sale.saleEndDate >= :startDate and i.sale.saleEndDate < :endDate  and i.category=:category order by i.quantity desc")
    List<ItemForSale> getBestSellingItemsByCategory(Long businessId, Instant startDate, Instant endDate, String category);

    @Query(value = "select Category, count(productId) as totalQuantity, sum(s.saleSubtotal) as totalAmount, sum(ifs.cost) as cost, sum(ifs.grossProfit) as grossProfit\r\n" + //
            "  from [RMPAY].[dbo].[ItemForSale] ifs inner join [RMPAY].[dbo].[Sale] s on ifs.saleID=s.saleID  \r\n" + //
            "  where s.businessId=:businessId and s.saleEndDate between :startDate and :endDate " +
            "  group by category\r\n" + //
            "  order by totalQuantity desc ", nativeQuery = true)
    public Object[] getBestSellingItemsXCategory(Long businessId, LocalDate startDate, LocalDate endDate);

    @Query(value = "select Category, count(productId) as totalQuantity, sum(s.saleSubtotal) as totalAmount, sum(ifs.cost) as cost, sum(ifs.grossProfit) as grossProfit\r\n" + //
            "  from [RMPAY].[dbo].[ItemForSale] ifs inner join [RMPAY].[dbo].[Sale] s on ifs.saleID=s.saleID  \r\n" + //
            "  where s.businessId=:businessId and s.saleEndDate >= :startDate and s.saleEndDate < :endDate " +
            "  group by category\r\n" + //
            "  order by totalQuantity desc ", nativeQuery = true)
    Object[] getBestSellingItemsXCategory(Long businessId, Instant startDate, Instant endDate);

    @Query(value = "select s from Sale s where s.business.businessId=:businessId and s.saleEndDate between :startDate and :endDate and s.saleStatus='SUCCEED'")
    public List<Sale> getSalesByDateRange(Long businessId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "select s from Sale s where s.business.businessId=:businessId and s.saleEndDate >= :startDate and s.saleEndDate < :endDate and s.saleStatus='SUCCEED'")
    public List<Sale> getSalesByDateRange(Long businessId, Instant startDate, Instant endDate);

    @Query(value = "SELECT s.userId, ub.username, sum(s.saleTotalAmount) as totalSales, \r\n" + //
            "sum(s.saleSubtotal) as subTotalSales, sum(s.tipAmount) as tipAmount, \r\n" + //
            "(SELECT sum(grossProfit)\r\n" + //
            "  FROM [RMPAY].[dbo].[ItemForSale]  as ifs where ifs.saleID in (select ss.saleID from [RMPAY].[dbo].[Sale] as ss where ss.userId=s.userId )) as totalProfits\r\n" + //
            "  FROM [RMPAY].[dbo].[Sale] as s left outer join [RMPAY].[dbo].[UsersBusiness] ub on s.userId=ub.userBusinessId \r\n" + //
            "  where s.saleEndDate between :startDate and :endDate and s.businessId= :businessId " + //
            "  group by s.userId, ub.username", nativeQuery = true)
    public Object[] getUserTipsReport(Long businessId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT s.userId, ub.username, sum(s.saleTotalAmount) as totalSales, \r\n" + //
            "sum(s.saleSubtotal) as subTotalSales, sum(s.tipAmount) as tipAmount, \r\n" + //
            "(SELECT sum(grossProfit)\r\n" + //
            "  FROM [RMPAY].[dbo].[ItemForSale]  as ifs where ifs.saleID in (select ss.saleID from [RMPAY].[dbo].[Sale] as ss where ss.userId=s.userId )) as totalProfits\r\n" + //
            "  FROM [RMPAY].[dbo].[Sale] as s left outer join [RMPAY].[dbo].[UsersBusiness] ub on s.userId=ub.userBusinessId \r\n" + //
            "  where s.saleEndDate >= :startDate and s.saleEndDate < :endDate and s.businessId= :businessId " + //
            "  group by s.userId, ub.username", nativeQuery = true)
    Object[] getUserTipsReport(Long businessId, Instant startDate, Instant endDate);


     @Query(value = """

        WITH SalesBase AS (
            SELECT *
            FROM Sale
            WHERE businessId = :businessId
              AND saleStatus IN ('SUCCEED','PARTIAL_REFUNDED')
              AND saleTransactionType = 'SALE'
        ),

        ProfitBase AS (
            SELECT
                s.saleId,
                SUM(
                    TRY_CAST(JSON_VALUE(item.value,'$.grossProfit') AS FLOAT)
                    *
                    TRY_CAST(JSON_VALUE(item.value,'$.quantity') AS FLOAT)
                ) AS Profit
            FROM Sale s
            CROSS APPLY OPENJSON(s.saleItems) item
            WHERE s.businessId = :businessId
            GROUP BY s.saleId
        )

        SELECT

        -- SALES YTD
        ISNULL(SUM(CASE
            WHEN s.saleCreationDate >= DATEFROMPARTS(YEAR(GETDATE()),1,1)
            THEN s.saleTotalAmount
        END),0) AS salesYTD,

        -- SALES LY
        ISNULL(SUM(CASE
            WHEN s.saleCreationDate >= DATEFROMPARTS(YEAR(GETDATE())-1,1,1)
             AND s.saleCreationDate < DATEADD(YEAR,-1,GETDATE())
            THEN s.saleTotalAmount
        END),0) AS salesLY,

        -- PROFIT YTD
        ISNULL(SUM(CASE
            WHEN s.saleCreationDate >= DATEFROMPARTS(YEAR(GETDATE()),1,1)
            THEN p.Profit
        END),0) AS profitYTD,

        -- PROFIT LY
       ISNULL(
    SUM(
        CASE
            WHEN s.saleCreationDate >= DATEFROMPARTS(YEAR(GETDATE())-1,1,1)
             AND s.saleCreationDate < DATEADD(YEAR,-1,GETDATE())
            THEN p.Profit
        END
    ),0
) AS profitLY,

        -- AVG TICKET
        ISNULL(AVG(CASE
            WHEN s.saleCreationDate >= DATEFROMPARTS(YEAR(GETDATE()),1,1)
            THEN s.saleTotalAmount
        END),0) AS avgTicketYTD,

        -- LAST YEAR AVG TICKET
        ISNULL(
    AVG(
        CASE
            WHEN s.saleCreationDate >= DATEFROMPARTS(YEAR(GETDATE()) - 1,1,1)
             AND s.saleCreationDate < DATEADD(YEAR,-1,GETDATE())
            THEN s.saleTotalAmount
        END
    ),0
) AS avgTicketLY,

        -- TODAY SALES
        ISNULL(SUM(CASE
            WHEN CAST(s.saleCreationDate AS DATE)=CAST(GETDATE() AS DATE)
            THEN s.saleTotalAmount
        END),0) AS todaySales,

        -- YESTERDAY SALES
        ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         = CAST(DATEADD(DAY,-1,GETDATE()) AS DATE)
    THEN s.saleTotalAmount
END),0) AS yesterdaySales,

-- SALES TWO DAYS AGO
ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         = CAST(DATEADD(DAY,-2,GETDATE()) AS DATE)
    THEN s.saleTotalAmount
END),0) AS twoDaysAgoSales,

-- THIS WEEK SALES (Monday -> TODAY)
ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         >= DATEADD(DAY, 2 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))
    THEN s.saleTotalAmount
END),0) AS thisWeekSales,

-- LAST WEEK SALES UNTIL SAME DAY AS TODAY
ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         >= DATEADD(
                WEEK,
                -1,
                DATEADD(
                    DAY,
                    2 - DATEPART(WEEKDAY, GETDATE()),
                    CAST(GETDATE() AS DATE)
                )
            )

     AND CAST(s.saleCreationDate AS DATE)
         < DATEADD(
                DAY,
                1,
                DATEADD(
                    WEEK,
                    -1,
                    CAST(GETDATE() AS DATE)
                )
            )

    THEN s.saleTotalAmount
END),0) AS lastWeekSalesUntilToday,

        -- TODAY TAXES
        ISNULL(SUM(CASE
            WHEN CAST(s.saleCreationDate AS DATE)=CAST(GETDATE() AS DATE)
            THEN ISNULL(s.saleCityTaxAmount,0)
               + ISNULL(s.saleStateTaxAmount,0)
        END),0) AS todayTaxes,

        -- YESTERDAY TAXES
        ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         = CAST(DATEADD(DAY,-1,GETDATE()) AS DATE)
    THEN ISNULL(s.saleCityTaxAmount,0)
       + ISNULL(s.saleStateTaxAmount,0)
END),0) AS yesterdayTaxes,

 -- Two Days Ago TAXES
        ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         = CAST(DATEADD(DAY,-2,GETDATE()) AS DATE)
    THEN ISNULL(s.saleCityTaxAmount,0)
       + ISNULL(s.saleStateTaxAmount,0)
END),0) AS twoDaysAgoTaxes,

-- THIS WEEK TAXES (MONDAY -> TODAY)
ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         >= DATEADD(
                DAY,
                2 - DATEPART(WEEKDAY, GETDATE()),
                CAST(GETDATE() AS DATE)
            )
    THEN ISNULL(s.saleCityTaxAmount,0)
       + ISNULL(s.saleStateTaxAmount,0)
END),0) AS thisWeekTaxes,

-- LAST WEEK TAXES UNTIL SAME DAY AS TODAY
ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         >= DATEADD(
                WEEK,
                -1,
                DATEADD(
                    DAY,
                    2 - DATEPART(WEEKDAY, GETDATE()),
                    CAST(GETDATE() AS DATE)
                )
            )

     AND CAST(s.saleCreationDate AS DATE)
         < DATEADD(
                DAY,
                1,
                DATEADD(
                    WEEK,
                    -1,
                    CAST(GETDATE() AS DATE)
                )
            )

    THEN ISNULL(s.saleCityTaxAmount,0)
       + ISNULL(s.saleStateTaxAmount,0)
END),0) AS lastWeekTaxesUntilToday,

        -- TODAY PROFIT
        ISNULL(SUM(CASE
            WHEN CAST(s.saleCreationDate AS DATE)=CAST(GETDATE() AS DATE)
            THEN p.Profit
        END),0) AS todayProfit,
        -- YESTERDAY PROFIT
ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         = CAST(DATEADD(DAY,-1,GETDATE()) AS DATE)
    THEN p.Profit
END),0) AS yesterdayProfit,

-- TWO DAYS AGO PROFIT
ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         = CAST(DATEADD(DAY,-2,GETDATE()) AS DATE)
    THEN p.Profit
END),0) AS twoDaysAgoProfit,

-- THIS WEEK PROFIT (Monday -> TODAY)
ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         >= DATEADD(
                DAY,
                2 - DATEPART(WEEKDAY, GETDATE()),
                CAST(GETDATE() AS DATE)
            )
    THEN p.Profit
END),0) AS thisWeekProfit,

-- LAST WEEK PROFIT UNTIL SAME DAY AS TODAY
ISNULL(SUM(CASE
    WHEN CAST(s.saleCreationDate AS DATE)
         >= DATEADD(
                WEEK,
                -1,
                DATEADD(
                    DAY,
                    2 - DATEPART(WEEKDAY, GETDATE()),
                    CAST(GETDATE() AS DATE)
                )
            )

     AND CAST(s.saleCreationDate AS DATE)
         < DATEADD(
                DAY,
                1,
                DATEADD(
                    WEEK,
                    -1,
                    CAST(GETDATE() AS DATE)
                )
            )

    THEN p.Profit
END),0) AS lastWeekProfitUntilToday,

        -- TODAY TX
        ISNULL(SUM(CASE
            WHEN CAST(s.saleCreationDate AS DATE)=CAST(GETDATE() AS DATE)
            THEN 1
        END),0) AS todayTransactions,
        COUNT(
        CASE
                WHEN s.saleCreationDate >= DATEFROMPARTS(YEAR(GETDATE()),1,1)
                AND s.saleCreationDate < DATEFROMPARTS(YEAR(GETDATE()) + 1,1,1)
                THEN 1
        END
        ) AS transactionsYTD,
        -- Taxes YTD
         SUM(
    CASE
        WHEN s.saleCreationDate >= DATEFROMPARTS(YEAR(GETDATE()),1,1)
         AND s.saleCreationDate < DATEFROMPARTS(YEAR(GETDATE()) + 1,1,1)
        THEN ISNULL(s.saleCityTaxAmount,0)
           + ISNULL(s.saleStateTaxAmount,0)
        ELSE 0
    END
) AS taxesYTD

        FROM SalesBase s
        LEFT JOIN ProfitBase p
            ON s.saleId = p.saleId

        """, nativeQuery = true)
    DashboardKpiProjection getDashboardKpis(Long businessId);

    @Query(value = """

    SELECT

        -- CURRENT YEAR

        ISNULL(SUM(CASE
            WHEN date >= DATEFROMPARTS(YEAR(GETDATE()),1,1)
            THEN totalWorkCost
        END),0) AS laborCostYTD,

        ISNULL(SUM(CASE
            WHEN date >= DATEFROMPARTS(YEAR(GETDATE()),1,1)
            THEN hoursWorked
        END),0) AS laborHoursYTD,


        -- LAST YEAR

        ISNULL(SUM(CASE
            WHEN date >= DATEFROMPARTS(YEAR(GETDATE())-1,1,1)
             AND date < DATEADD(YEAR,-1,GETDATE())
            THEN totalWorkCost
        END),0) AS laborCostLY,

        ISNULL(SUM(CASE
            WHEN date >= DATEFROMPARTS(YEAR(GETDATE())-1,1,1)
             AND date < DATEADD(YEAR,-1,GETDATE())
            THEN hoursWorked
        END),0) AS laborHoursLY

    FROM EntryExit
    WHERE userBusinessId IN (
        SELECT userBusinessId
        FROM UsersBusiness
        WHERE businessId = :businessId
    )

    """, nativeQuery = true)
LaborMetricsProjection getLaborMetrics(Long businessId);

    @Query(value = """

        SELECT
    DATEPART(HOUR, saleCreationDate) AS hour,
    COUNT(*) AS transactions,
    ISNULL(SUM(saleTotalAmount),0) AS sales
FROM Sale
WHERE businessId = :businessId
  AND saleStatus = 'SUCCEED'
  AND CAST(saleCreationDate AS DATE)=CAST(GETDATE() AS DATE)
GROUP BY DATEPART(HOUR, saleCreationDate)
ORDER BY hour

        """, nativeQuery = true)
    List<HourlySalesProjection> getHourlySales(Long businessId);

    @Query(value = """

    SELECT
    DAY(saleCreationDate) AS dayOfMonth,
    COUNT(*) AS transactions,
    ISNULL(SUM(saleTotalAmount),0) AS sales
FROM Sale
WHERE businessId = :businessId
  AND saleStatus = 'SUCCEED'
  AND saleCreationDate >= DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)
  AND saleCreationDate < DATEADD(DAY,1,CAST(GETDATE() AS DATE))
GROUP BY DAY(saleCreationDate)
ORDER BY dayOfMonth

    """, nativeQuery = true)
List<DailySalesProjection> getDailySalesCurrentMonth(Long businessId);

@Query(value = """

    SELECT
        DAY(saleCreationDate) AS dayOfMonth,
        COUNT(*) AS transactions,
        ISNULL(SUM(saleTotalAmount),0) AS sales
    FROM Sale
    WHERE businessId = :businessId
      AND saleStatus = 'SUCCEED'

      -- PRIMER DÍA DEL MES PASADO
      AND saleCreationDate >= DATEFROMPARTS(
            YEAR(DATEADD(MONTH,-1,GETDATE())),
            MONTH(DATEADD(MONTH,-1,GETDATE())),
            1
      )

      -- MISMO DÍA DEL MES ACTUAL PERO EN MES PASADO
      AND saleCreationDate < DATEADD(
            MONTH,
            -1,
            DATEADD(DAY,1,CAST(GETDATE() AS DATE))
      )

    GROUP BY DAY(saleCreationDate)
    ORDER BY dayOfMonth

    """, nativeQuery = true)
List<DailySalesProjection> getDailySalesLastMonthUntilToday(Long businessId);

@Query(value = """

    SELECT

          CONVERT(VARCHAR(33), s.saleCreationDate, 127) AS saleCreationDate,

        t.globalUId AS globalUId,

        t.paymentType AS paymentType,

        ISNULL(s.saleSubtotal,0) AS saleSubtotal,

        ISNULL(s.saleCityTaxAmount,0)
        + ISNULL(s.saleStateTaxAmount,0) AS taxes,

        ISNULL(s.saleTotalAmount,0) AS saleTotalAmount

    FROM Transactions t
    INNER JOIN Sale s
        ON t.saleId = s.saleID

    WHERE s.businessId = :businessId

      AND s.saleStatus IN ('SUCCEED','PARTIAL_REFUNDED')

      AND s.saleCreationDate >= :startDate

      AND s.saleCreationDate < :endDate

    ORDER BY s.saleCreationDate DESC

    """, nativeQuery = true)
List<TransactionDetailProjection> getTransactionDetails(
        Long businessId,
        Instant startDate,
        Instant endDate
);

@Query(value = """

    SELECT

        sft.shiftId AS shiftId,

        sft.userBusinessId AS userBusinessId,

        sft.userName AS userName,

        t.globalUId AS globalUId,

        t.paymentType AS paymentType,

        t.amount AS amount,

        t.authCode AS authCode,

        t.cardType AS cardType,

        CONVERT(VARCHAR(33), t.date, 127) AS transactionDate,

        s.saleID AS saleId,

        ISNULL(s.saleSubtotal,0) AS saleSubtotal,

        ISNULL(s.saleCityTaxAmount,0)
        + ISNULL(s.saleStateTaxAmount,0) AS taxes,

        ISNULL(s.saleTotalAmount,0) AS saleTotalAmount,

        ISNULL(s.tipAmount,0) AS tipAmount,

        s.saleStatus AS saleStatus,
        s.terminalId AS terminalId

    FROM Shift sft

    INNER JOIN Sale s
        ON s.userId = sft.userBusinessId

    INNER JOIN Transactions t
        ON t.saleId = s.saleID

    WHERE sft.shiftId = :shiftId

      AND sft.userBusinessId = :userBusinessId

      AND t.date >= sft.startTime

      AND (
            sft.endTime IS NULL
            OR t.date <= sft.endTime
          )

      AND s.saleStatus IN ('SUCCEED','PARTIAL_REFUNDED')

    ORDER BY t.date DESC

    """, nativeQuery = true)
List<ShiftTransactionProjection> getShiftTransactions(
        String shiftId,
        Long userBusinessId
);

}

