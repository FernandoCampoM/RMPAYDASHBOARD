package com.retailmanager.rmpaydashboard.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.ItemForSale;
import com.retailmanager.rmpaydashboard.models.Sale;

public interface SaleRepository extends CrudRepository<Sale, String>  {
    /**
     * Obtiene una lista de ventas por identificador de comercio.
     *
     * @param  merchantId	identificador de comercio
     * @return         	listado de ventas
     */
    //
    @Query("SELECT s FROM Sale s WHERE s.business.merchantId = :merchantId")
    public List<Sale> findByMerchantId(String merchantId);
    /**
     * Obtiene una lista de ventas por tipo de transacción, estado y identificador de comercio.
     *
     * @param  saleTransactionType	tipo de transacción
     * @param  saleStatus			estado
     * @param  merchantId			identificador de comercio
     * @return         			listado de ventas
     */
    //
    public List<Sale> findBySaleTransactionTypeAndSaleStatusAndBusiness(String saleTransactionType, String saleStatus, Business business);
    /**
     * Obtiene una lista de ventas por tipo de transacción y identificador de comercio.
     *
     * @param  saleTransactionType  tipo de transacción
     * @param  merchantId           identificador de comercio
     * @return                      listado de ventas
     */
    //
    public List<Sale> findBySaleTransactionTypeAndBusiness(String saleTransactionType, Business business);
    public List<Sale> findByBusiness( Business business);
    @Query("SELECT s FROM Sale s WHERE s.saleTransactionType = :saleTransactionType AND s.terminal.terminalId = :terminalId AND s.business.merchantId = :merchantId")
    public List<Sale> findByMerchantIdAndTerminalId(String saleTransactionType, String terminalId, String merchantId);
    @Query("SELECT s FROM Sale s WHERE s.terminal.terminalId = :terminalId AND s.business.merchantId = :merchantId")
    public List<Sale> findByMerchantIdAndTerminalId( String terminalId, String merchantId);
    /**
     * Obtiene las ventas entre dos fechas y por tipo de transacción y estado e identificador de comercio.
     *
     * @param  startDate          fecha de inicio
     * @param  endDate            fecha de fin
     * @param  saleTransactionType tipo de transacción
     * @param  saleStatus         estado
     * @param  business         identificador de comercio
     * @return                    listado de ventas
     */
    //
    public List<Sale> findBySaleEndDateBetweenAndSaleTransactionTypeAndSaleStatusAndBusiness(LocalDate startDate, LocalDate endDate, String saleTransactionType, String saleStatus, Business business);


    ///FUNCIONES DE REPORTES
    //TODO: MODIFICAR EL CALCULO DE REFUND DE ACUERDO AL TIPO DE TRANSACCION
    /**
     * Retrieves daily summary data for a specific business.
     *
     * @param  businessId   the ID of the business
     * @return              an array containing total sales, total refund, state tax, city tax, and reduced tax
     */
    @Query(value=" SELECT \r\n" + 
                "  (SELECT sum(saleTotalAmount) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId ) as totalSales,\r\n" + 
                "  \r\n" + 
                "  (SELECT sum(saleTotalAmount) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType IN ('REFUND','PARTIAL_REFUND') AND saleStatus IN ('REFUNDED','PARTIAL_REFUNDED') and businessId=:businessId )as totalRefund,\r\n" + 
                "\r\n" + 
                "  (SELECT sum(saleStateTaxAmount)\r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId ) as stateTax,\r\n" + 
                "\r\n" + 
                "  (SELECT sum(saleCityTaxAmount) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId )  as cityTax,\r\n" + 
                "\r\n" + 
                "  (SELECT sum(saleReduceTax) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId  ) as redTax, "+
                " (SELECT sum(saleSubtotal) " + 
                "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId  ) as subTotalSales, "+
                " (SELECT  SUM(it.grossProfit) AS profit FROM [RMPAY].[dbo].[ItemForSale] it JOIN [RMPAY].[dbo].[Sale] s ON it.saleID = s.saleID WHERE CAST(s.saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' AND s.businessId = :businessId) as  grossBenefit, "+
                " (SELECT  SUM(s.tipAmount) from [RMPAY].[dbo].[Sale] s  WHERE CAST(s.saleEndDate AS DATE) BETWEEN :startDate AND :endDate AND saleTransactionType='SALE'AND saleStatus='SUCCEED' AND s.businessId = :businessId) as totalTips ", nativeQuery = true)
    public Object[] dailySummary(Long businessId,LocalDate startDate, LocalDate endDate);
    @Query(value=" SELECT \r\n" + 
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
                "  FROM [RMPAY].[dbo].[Sale] where YEAR(saleEndDate) = :year AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId  ) as redTax, "+
                " (SELECT sum(saleSubtotal) " + 
                "  FROM [RMPAY].[dbo].[Sale] where YEAR(saleEndDate) = :year AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId  ) as subTotalSales, "+
                " (SELECT  SUM(it.grossProfit) AS profit FROM [RMPAY].[dbo].[ItemForSale] it JOIN [RMPAY].[dbo].[Sale] s ON it.saleID = s.saleID WHERE YEAR(s.saleEndDate) = :year AND saleTransactionType='SALE'AND saleStatus='SUCCEED' AND s.businessId = :businessId) as  grossBenefit, "+
                " (SELECT  SUM(s.tipAmount) from [RMPAY].[dbo].[Sale] s  WHERE year(s.saleEndDate) = :year AND saleTransactionType='SALE'AND saleStatus='SUCCEED' AND s.businessId = :businessId) as totalTips, "+
                " ( SELECT sum(e.totalWorkCost) FROM [RMPAY].[dbo].[EntryExit] e inner join [RMPAY].[dbo].[UsersBusiness] ub on e.userBusinessId=ub.userBusinessId where year(e.date) = :year and ub.businessId=:businessId) as totalWorkCost", nativeQuery = true)
    public Object[] annualSummary(Long businessId,int year);
    @Query(value=" SELECT \r\n" + 
                "  (SELECT sum(saleTotalAmount) \r\n" + 
                "  FROM [RMPAY].[dbo].[Sale] where CAST(saleEndDate AS DATE) = :fecha AND saleTransactionType='SALE'AND saleStatus='SUCCEED' and businessId=:businessId ) as totalSales", nativeQuery = true)
    public Object[] monthlySummary(Long businessId,LocalDate fecha);

    /**
     * Generate daily summary for a specific category.
     *
     * @param  businessId  the ID of the business
     * @return             an array containing the daily summary for the category
     */
    @Query(value="  SELECT category, sum(s.saleTotalAmount) as totalSales "+
    " FROM [RMPAY].[dbo].[ItemForSale] it join [RMPAY].[dbo].[Sale] s on it.saleID=s.saleID  where s.saleEndDate=:prmDate and s.businessId=:businessId "+
    " group by category order by sum(s.saleTotalAmount) desc", nativeQuery = true)
    public Object[] dailySummaryForCategory(Long businessId, LocalDate prmDate);

    /**
     * Obtiene una lista de los productos mas vendidos en orden descendente
     *
     * @param  businessId	identificador del negocio
     * @return         	
     */
    @Query(value="SELECT productId, sum(it.quantity) as quantity,sum(it.quantity*it.price) as totalAmount, sum(it.grossProfit) as profit, (select top(1) name from [RMPAY].[dbo].[ItemForSale] ift where ift.productId=it.productId) as name,  it.price,  it.category " + //
                "  FROM [RMPAY].[dbo].[ItemForSale] it join [RMPAY].[dbo].[Sale] s on it.saleID=s.saleID  \r\n" + //
                "  where CAST(s.saleEndDate AS DATE) BETWEEN :startDate AND :endDate and s.businessId=:businessId \r\n" + //
                "  group by productId,  it.category,  it.price " + //
                "  order by sum(it.quantity) desc ", nativeQuery = true)
    public Object[] dailySummaryBestSellingItems(Long businessId,LocalDate startDate,LocalDate endDate);
    
    /**
     * Obtiene una lista de ventas por tipo de transacción, estado, fecha y negocio al que pertenece
     *
     * @param  saleTransactionType	tipo de transacción
     * @param  saleStatus			estado de la transacción
     * @param  business	negocio que realiza la venta
     * @return         	
     */
    //
    public List<Sale> findBySaleTransactionTypeAndSaleStatusAndBusinessAndSaleEndDateBetween(String saleTransactionType, String saleStatus, Business business, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "  SELECT t.paymentType, sum(t.amount) as totalAmount\r\n" + //
                "  FROM [RMPAY].[dbo].[Sale] S INNER JOIN [RMPAY].[dbo].[Transactions] t ON s.saleID=t.saleId  \r\n" + //
                "  WHERE saleEndDate BETWEEN :startDate AND :endDate AND s.businessId=:businessId and s.saleTransactionType in ('REFUND','PARTIAL_REFUND') AND s.saleStatus IN ('REFUNDED','PARTIAL_REFUNDED')\r\n" + //
                "  group by paymentType\r\n" + //
                "  ORDER BY totalAmount DESC", nativeQuery = true)
    public Object[] refundSumaryByRange(Long businessId,LocalDate startDate, LocalDate endDate);

   
    /**
     * Retrieves a list of ItemForSale objects based on the provided businessId.
     * @param  businessId  the ID of the business
     * @return             a list of ItemForSale objects associated with the business
     */
    @Query(value = "select i from ItemForSale i where i.sale.business.businessId=:businessId and i.sale.saleEndDate between :startDate and :endDate order by i.quantity desc")
    public List<ItemForSale> getBestSellingItems(Long businessId, LocalDateTime startDate, LocalDateTime endDate);
    /**
     * Retrieves the best selling items by category within a specified date range for a given business.
     * 
     * @param  businessId  the ID of the business
     * @param  startDate   the start date of the date range
     * @param  endDate     the end date of the date range
     * @param  category    the category of items to retrieve
     * @return             a list of ItemForSale objects representing the best selling items by category
     */
    @Query(value = "select i from ItemForSale i where i.sale.business.businessId=:businessId and i.sale.saleEndDate between :startDate and :endDate  and i.category=:category order by i.quantity desc")
    public List<ItemForSale> getBestSellingItemsByCategory(Long businessId, LocalDateTime startDate, LocalDateTime endDate, String category);
    
    @Query(value = "select Category, count(productId) as totalQuantity, sum(s.saleSubtotal) as totalAmount, sum(ifs.cost) as cost, sum(ifs.grossProfit) as grossProfit\r\n" + //
                "  from [RMPAY].[dbo].[ItemForSale] ifs inner join [RMPAY].[dbo].[Sale] s on ifs.saleID=s.saleID  \r\n" + //
                "  where s.businessId=:businessId and s.saleEndDate between :startDate and :endDate " + 
                "  group by category\r\n" + //
                "  order by totalQuantity desc ", nativeQuery = true)
    public Object[] getBestSellingItemsXCategory(Long businessId, LocalDate startDate, LocalDate endDate);

    @Query(value = "select s from Sale s where s.business.businessId=:businessId and s.saleEndDate between :startDate and :endDate and s.saleStatus='SUCCEED'")
    public List<Sale> getSalesByDateRange(Long businessId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "SELECT s.userId, ub.username, sum(s.saleTotalAmount) as totalSales, \r\n" + //
                "sum(s.saleSubtotal) as subTotalSales, sum(s.tipAmount) as tipAmount, \r\n" + //
                "(SELECT sum(grossProfit)\r\n" + //
                "  FROM [RMPAY].[dbo].[ItemForSale]  as ifs where ifs.saleID in (select ss.saleID from [RMPAY].[dbo].[Sale] as ss where ss.userId=s.userId )) as totalProfits\r\n" + //
                "  FROM [RMPAY].[dbo].[Sale] as s left outer join [RMPAY].[dbo].[UsersBusiness] ub on s.userId=ub.userBusinessId \r\n" + //
                "  where s.saleEndDate between :startDate and :endDate and s.businessId= :businessId " + //
                "  group by s.userId, ub.username", nativeQuery = true)
    public Object[] getUserTipsReport(Long businessId, LocalDate startDate, LocalDate endDate);

}

