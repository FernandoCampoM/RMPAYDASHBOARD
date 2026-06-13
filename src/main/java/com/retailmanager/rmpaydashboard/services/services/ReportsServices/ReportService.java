package com.retailmanager.rmpaydashboard.services.services.ReportsServices;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.EntryExit;
import com.retailmanager.rmpaydashboard.models.ItemForSale;
import com.retailmanager.rmpaydashboard.models.Product;
import com.retailmanager.rmpaydashboard.models.Sale;
import com.retailmanager.rmpaydashboard.models.Transactions;
import com.retailmanager.rmpaydashboard.models.Interface.LaborMetricsProjection;
import com.retailmanager.rmpaydashboard.models.enums.KPIStatus;
import com.retailmanager.rmpaydashboard.repositories.BusinessConfigurationRepository;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.DashboardKpiProjection;
import com.retailmanager.rmpaydashboard.repositories.EntryExitRepository;
import com.retailmanager.rmpaydashboard.repositories.ProductRepository;
import com.retailmanager.rmpaydashboard.repositories.SaleRepository;
import com.retailmanager.rmpaydashboard.repositories.TransactionsRepository;
import com.retailmanager.rmpaydashboard.repositories.UsersAppRepository;
import com.retailmanager.rmpaydashboard.services.DTO.EntryExitDTO;
import com.retailmanager.rmpaydashboard.services.DTO.HourlySalesDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.BestSellingCategoryProjection;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.BestSellingItemProjection;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.CategoryNetSalesProjection;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.DailySalesDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.DailySummaryDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.DailySummaryProjection;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.DashboardKpiDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.EarningsReportDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.PaymentNetProjection;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.TipsReportDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.UserTipsReportProjection;
import com.retailmanager.rmpaydashboard.services.DTO.TransactionDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ReportService implements IReportService {
    private final AccessDeniedHandler accessDeniedHandler;
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private BusinessConfigurationRepository serviceDBBusinessConfiguration;
    @Autowired
    private ProductRepository serviceDBProduct;
    @Autowired
    private SaleRepository serviceDBSale;
    @Autowired
    private TransactionsRepository serviceDBTransactions;

    @Autowired
    private EntryExitRepository serviceDBEntryExit;

    @Autowired
    private UsersAppRepository usersAppRepository;

    ReportService(AccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * Retrieves the daily summary for a given business ID.
     *
     * @param businessId the ID of the business
     * @return a ResponseEntity containing the daily summary data
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getDailySummary(Long businessId, Instant startUtc, Instant endUtc) {
        DailySummaryDTO dailySummaryDTO = new DailySummaryDTO();
        DailySummaryProjection dailySummary = this.serviceDBSale.dailySummary(businessId, startUtc, endUtc);
        if(businessId == null){
            throw new EntidadNoExisteException("El businessId no puede ser nulo");
        }

        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }

        
            dailySummaryDTO.setTotalSales(dailySummary.getSubTotalSales().subtract(dailySummary.getSubTotalRefund()));
        
        
            dailySummaryDTO.setTotalRefunds(dailySummary.getSubTotalRefund());
       
        
            dailySummaryDTO.setStateTax(dailySummary.getStateTaxSales().subtract(dailySummary.getStateTaxRefund()));
       
       
            dailySummaryDTO.setMunicipalTax(dailySummary.getCityTaxSales().subtract(dailySummary.getCityTaxRefund()));
        
        
            dailySummaryDTO.setEstimatedRedTax(dailySummary.getRedTaxSales().subtract(dailySummary.getRedTaxRefund()));
            dailySummaryDTO.setBenefit(dailySummary.getGrossBenefit());
            dailySummaryDTO.setTotalTips(dailySummary.getTotalTips().subtract(dailySummary.getTotalTipsRefund()));
      
        
       List<CategoryNetSalesProjection>  dailySummaryByCategory = this.serviceDBSale.summaryForCategory(businessId, startUtc, endUtc);
        dailySummaryDTO.setSalesByCategory(dailySummaryByCategory);
       List<BestSellingItemProjection> dailySummaryBestSellingItems = this.serviceDBSale.dailySummaryBestSellingItems(businessId, startUtc, endUtc);
        dailySummaryDTO.setBestSellingProducts(dailySummaryBestSellingItems);
        return new ResponseEntity<>(dailySummaryDTO, HttpStatus.OK);
    }

    /**
     * Generate a summary Sales report by date range for a given business ID.
     *
     * @param businessId The ID of the business
     * @param startDate  The start date of the date range
     * @param endDate    The end date of the date range
     * @return ResponseEntity containing the daily summary DTO
     */
    @Override
    public ResponseEntity<?> getSummaryByDateRangee(Long businessId, Instant startUtc, Instant endUtc) {
        DailySummaryDTO dailySummaryDTO = new DailySummaryDTO();

        DailySummaryProjection  dailySummary = this.serviceDBSale.dailySummary(businessId, startUtc, endUtc);
        if(businessId == null){
            throw new EntidadNoExisteException("El businessId no puede ser nulo");
        }
        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }
        
            dailySummaryDTO.setTotalSales(dailySummary.getSubTotalSales().subtract(dailySummary.getSubTotalRefund()));
        
        
            dailySummaryDTO.setTotalRefunds(dailySummary.getSubTotalRefund());

        
            dailySummaryDTO.setStateTax(dailySummary.getStateTaxSales().subtract(dailySummary.getStateTaxRefund()));
        
            dailySummaryDTO.setMunicipalTax(dailySummary.getCityTaxSales().subtract(dailySummary.getCityTaxRefund()));

        
            dailySummaryDTO.setEstimatedRedTax(dailySummary.getRedTaxSales().subtract(dailySummary.getRedTaxRefund()));
            dailySummaryDTO.setBenefit(dailySummary.getGrossBenefit());
                dailySummaryDTO.setTotalTips(dailySummary.getTotalTips().subtract(dailySummary.getTotalTipsRefund()));
        
        List<PaymentNetProjection> bestPaymentTypes = this.serviceDBSale.getBestPaymentTypes(businessId, startUtc, endUtc);
        if(bestPaymentTypes != null){
            dailySummaryDTO.setBestSellingPayMethods(bestPaymentTypes);
        }
        
        Object[] refundSummary = this.serviceDBSale.refundSumaryByRange(businessId, startUtc, endUtc);

        for (int i = 0; i < refundSummary.length; i++) {
            HashMap<String, String> data = new HashMap<>();
            Object[] refundSummaryV = (Object[]) refundSummary[i];
            if (refundSummaryV[0] != null) {
                data.put("patymentType", objectToString(refundSummaryV[0]));
            }
            if (refundSummaryV[1] != null) {
                data.put("totalAmount", objectToString(refundSummaryV[1]));
            }
            dailySummaryDTO.getRefundsSummay().add(data);
        }

        dailySummaryDTO.setSalesByCategory(new ArrayList<>());
        List<CategoryNetSalesProjection> dailySummaryByCategory = this.serviceDBSale.summaryForCategory(businessId, startUtc, endUtc);
        if(accessDeniedHandler != null){
            dailySummaryDTO.setSalesByCategory(dailySummaryByCategory);
        }


        dailySummaryDTO.setBestSellingProducts(new ArrayList<>());

        List<BestSellingItemProjection> dailySummaryBestSellingItems = this.serviceDBSale.dailySummaryBestSellingItems(businessId, startUtc, endUtc);
        if (dailySummaryBestSellingItems != null) {
            dailySummaryDTO.setBestSellingProducts(dailySummaryBestSellingItems);

        }

        return new ResponseEntity<>(dailySummaryDTO, HttpStatus.OK);
    }

    /**
     * getLowInventory method retrieves low inventory products for a given business ID.
     *
     * @param businessId the ID of the business
     * @return ResponseEntity with a list of low inventory products and HTTP status OK
     */
    @Override
    public ResponseEntity<?> getLowInventory(Long businessId) {
        List<Product> products = this.serviceDBProduct.getLowInventory(businessId);

        List<ProductDTO> productsDTO = new ArrayList<>();
        if (products != null && products.size() > 0) {
            productsDTO = products.stream().map(product -> ProductDTO.tOProduct(product)).collect(Collectors.toList());
        }
        productsDTO.sort(Comparator.comparing(ProductDTO::getSuggestedPurchase).reversed());
        return new ResponseEntity<>(productsDTO, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<?> getBestSellingItems(Long businessId, Instant startUtc, Instant endUtc,
                                                 String categoria) {
        if (businessId == null) {
            throw new EntidadNoExisteException("El businessId no puede ser nulo");
        }
        List<ItemForSale> productDTOs = new ArrayList<>();
        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if (business != null) {
            if (categoria.compareTo("TODAS") != 0) {
                productDTOs = this.serviceDBSale.getBestSellingItemsByCategory(businessId, startUtc, endUtc, categoria);
            } else {
                productDTOs = this.serviceDBSale.getBestSellingItems(businessId, startUtc, endUtc);
            }
        } else {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }
        productDTOs.forEach(productDTO -> productDTO.setSale(null));
        return new ResponseEntity<>(productDTOs, HttpStatus.OK);

    }

    /**
     * Generate a sales report category within a specified time frame.
     *
     * @param businessId the ID of the business
     * @param startDate  the start date of the time frame
     * @param endDate    the end date of the time frame
     * @param categoria  the category to filter the sales by
     * @return a ResponseEntity containing a list of sales grouped by category
     */
    @Override
    public ResponseEntity<?> getSalesByCategory(Long businessId, Instant startUtc, Instant endUtc) {
        if(businessId == null){
            throw new EntidadNoExisteException("El businessId no puede ser nulo");
        }
        if (!this.serviceDBBusiness.existsById(businessId)) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }
       List<BestSellingCategoryProjection> salesByCategory = this.serviceDBSale.getBestSellingItemsXCategory(businessId, startUtc, endUtc);
        if(salesByCategory != null){
           return new ResponseEntity<>(salesByCategory, HttpStatus.OK);
        }

        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
    }

    /**
     * A method to retrieve the earnings report for a specific business within a given time frame.
     *
     * @param businessId the ID of the business
     * @param startDate  the start date of the time frame
     * @param endDate    the end date of the time frame
     * @return a ResponseEntity containing the earnings report DTO and the HTTP status
     */
    @Override
    public ResponseEntity<?> getEarningsReport(Long businessId, Instant startUtc, Instant endUtc) {
        EarningsReportDTO dailySummaryDTO = new EarningsReportDTO();
        if(businessId == null){
            throw new EntidadNoExisteException("El businessId no puede ser nulo");
        }
       DailySummaryProjection dailySummary = this.serviceDBSale.dailySummary(businessId, startUtc, endUtc);
        
        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }

        
            dailySummaryDTO.setTotalSales(dailySummary.getTotalSales().subtract(dailySummary.getTotalRefund()));
       
        
            dailySummaryDTO.setStateTax(dailySummary.getStateTaxSales().subtract(dailySummary.getStateTaxRefund()));
        
       
            dailySummaryDTO.setMunicipalTax(dailySummary.getCityTaxSales().subtract(dailySummary.getCityTaxRefund()));

        
            dailySummaryDTO.setEstimatedRedTax(dailySummary.getRedTaxSales().subtract(dailySummary.getRedTaxRefund()));
        
            dailySummaryDTO.setSubTotalSales(dailySummary.getSubTotalSales());
            dailySummaryDTO.setBenefit(dailySummary.getGrossBenefit());
           
        List<BestSellingCategoryProjection> salesByCategory = this.serviceDBSale.getBestSellingItemsXCategory(businessId, startUtc, endUtc);
        
        dailySummaryDTO.setEarningsByCategory(salesByCategory);

        List<BestSellingItemProjection> dailySummaryBestSellingItems = this.serviceDBSale.dailySummaryBestSellingItems(businessId, startUtc, endUtc);
        
        dailySummaryDTO.setBestSellingProducts(dailySummaryBestSellingItems);

        return new ResponseEntity<>(dailySummaryDTO, HttpStatus.OK);
    }

    /**
     * Get tips report for a specified business within a date range.
     *
     * @param businessId the ID of the business
     * @param startDate  the start date of the report
     * @param endDate    the end date of the report
     * @return ResponseEntity containing the tips report
     */
    @Override
    public ResponseEntity<?> getTips(Long businessId, Instant startUtc, Instant endUtc) {
        TipsReportDTO dailySummaryDTO = new TipsReportDTO();
        if(businessId == null){
            throw new EntidadNoExisteException("El businessId no puede ser nulo");
        }
        DailySummaryProjection dailySummary = this.serviceDBSale.dailySummary(businessId, startUtc, endUtc);
        
        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }

            dailySummaryDTO.setTotalSales(dailySummary.getTotalSales().subtract(dailySummary.getTotalRefund()));
        
            dailySummaryDTO.setSubTotalSales(dailySummary.getSubTotalSales());
            dailySummaryDTO.setTotalTips(dailySummary.getTotalTips().subtract(dailySummary.getTotalTipsRefund()));

        dailySummaryDTO.setUserTips(new ArrayList<>());
        List<UserTipsReportProjection> tipsByUsers = this.serviceDBSale.getUserTipsReport(businessId, startUtc, endUtc);
        if (tipsByUsers != null) {
            dailySummaryDTO.setUserTips(tipsByUsers);
        } 
        return new ResponseEntity<>(dailySummaryDTO, HttpStatus.OK);
    }

    /**
     * Retrieves the report taxes for a given business within a specified date range.
     *
     * @param businessId the ID of the business
     * @param startDate  the start date of the date range
     * @param endDate    the end date of the date range
     * @return a ResponseEntity containing the taxes data
     */
    @Override
    public ResponseEntity<?> getTaxes(Long businessId, Instant startUtc, Instant endUtc) {
        
        if(businessId == null){
            throw new EntidadNoExisteException("El businessId no puede ser nulo");
        }
        if (!this.serviceDBBusiness.existsById(businessId)) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }
        List<Sale> sales = this.serviceDBSale.getSalesByDateRange(businessId, startUtc, endUtc);
        HashMap<String, String> data = new HashMap<>();
        data.put("totalTax", String.valueOf(0));
        data.put("totalSales", String.valueOf(0));
        data.put("totalStatalTax", String.valueOf(0));
        data.put("totalCityTax", String.valueOf(0));
        data.put("totalReduceTax", String.valueOf(0));
        data.put("totalTaxableSales", String.valueOf(0));
        if (sales != null && sales.size() > 0) {
            double totalTax = 0;
            double totalSales = 0;
            double totalStatalTax = 0;
            double totalCityTax = 0;
            double totalReduceTax = 0;
            for (Sale sale : sales) {
                totalSales += sale.getSaleTotalAmount();
                totalStatalTax += sale.getSaleStateTaxAmount();
                totalCityTax += sale.getSaleCityTaxAmount();
                totalReduceTax += sale.getSaleReduceTax();
                totalTax += sale.getSaleCityTaxAmount() + sale.getSaleStateTaxAmount() + sale.getSaleReduceTax();
            }
            data.put("totalTax", String.valueOf(totalTax));
            data.put("totalSales", String.valueOf(totalSales));
            data.put("totalStatalTax", String.valueOf(totalStatalTax));
            data.put("totalCityTax", String.valueOf(totalCityTax));
            data.put("totalReduceTax", String.valueOf(totalReduceTax));
            data.put("totalTaxableSales", String.valueOf(totalSales - totalStatalTax - totalCityTax - totalReduceTax));
        }
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    private String objectToString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    /**
     * Retrieves a list of receipts for a given business within a specified date range.
     *
     * @param businessId the ID of the business
     * @param startDate  the start date of the date range
     * @param endDate    the end date of the date range
     * @return a ResponseEntity containing a list of TransactionDTO objects representing the receipts
     * for the specified business within the date range
     * @throws EntidadNoExisteException if the business with the given ID does not exist in the database
     */
    @Override
    public ResponseEntity<?> getReceipts(Long businessId, Instant startUtc, Instant endUtc) {
        if(businessId == null){
            throw new EntidadNoExisteException("El businessId no puede ser nulo");
        }
        if (!this.serviceDBBusiness.existsById(businessId)) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }
        List<Transactions> transactions = this.serviceDBTransactions.getTransactionsByRange(businessId, startUtc, endUtc);

        List<TransactionDTO> transactionsDTOs = new ArrayList<>();

        if (transactions != null && transactions.size() > 0) {
            for (Transactions transaction : transactions) {
                TransactionDTO transactionDTO = this.mapper.map(transaction, TransactionDTO.class);
                transactionDTO.setInfoSale(transaction.getSale().toDTO());

                transactionsDTOs.add(transactionDTO);
            }
        }

        return new ResponseEntity<>(transactionsDTOs, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getActivationsReport(int month) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActivationsReport'");
    }

    /**
     * Retrieves the home report for a given business, start date, end date, and filter.
     *
     * @param businessId the ID of the business
     * @param startDate  the start date of the report
     * @param endDate    the end date of the report (optional)
     * @param filter     the filter for the report (optional)
     * @return a ResponseEntity containing the home report as a HashMap
     * @throws EntidadNoExisteException if the business with the given ID does not exist in the database
     */
    @Override
    public ResponseEntity<?> getHomeReport(Long businessId, LocalDate startDate, LocalDate endDate, String filter) {
        
        if(businessId == null){
            throw new EntidadNoExisteException("El businessId no puede ser nulo");
        }
        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }
        if (filter != null) {
            switch (filter.trim().toUpperCase(Locale.ROOT)) {
                case "YESTERDAY" -> {
                    LocalDate d = startDate.minusDays(1);
                    startDate = d;
                    endDate = d;
                }
                case "TODAY" -> endDate = startDate;
                case "WEEK" -> {
                    LocalDate monday = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    startDate = monday;
                    endDate = monday.plusDays(6);
                }
                case "MONTH" -> {
                    LocalDate first = startDate.with(TemporalAdjusters.firstDayOfMonth());
                    startDate = first;
                    endDate = first.with(TemporalAdjusters.lastDayOfMonth());
                }
                default -> {
                    endDate = startDate;
                }
            }
        } else {
            if (endDate == null) {
                endDate = startDate;
            }
        }
        ZoneId zone = ZoneOffset.UTC;
        Instant startInstant = startDate.atStartOfDay(zone).toInstant();
Instant endInstant = endDate.plusDays(1).atStartOfDay(zone).toInstant();
        ///////////Info para el reporte del dia
        DailySummaryProjection dailySummary = this.serviceDBSale.dailySummary(businessId, startInstant, endInstant);
       
        HashMap<String, Object> dailySummaryDTO = new HashMap<>();
       
            dailySummaryDTO.put("totalSales", dailySummary.getSubTotalSales().subtract(dailySummary.getSubTotalRefund()));
        
        BigDecimal totalTax = BigDecimal.ZERO;
            totalTax = totalTax.add(dailySummary.getStateTaxSales().subtract(dailySummary.getStateTaxRefund()));
        
            totalTax = totalTax.add(dailySummary.getCityTaxSales().subtract(dailySummary.getCityTaxRefund()));
       
            totalTax = totalTax.add(dailySummary.getRedTaxSales().subtract(dailySummary.getRedTaxRefund()));
        
            dailySummaryDTO.put("totalTax", totalTax);
        
        
            dailySummaryDTO.put("grossProfit", dailySummary.getGrossBenefit());
       
        
            dailySummaryDTO.put("totalTips", dailySummary.getTotalTips().subtract(dailySummary.getTotalTipsRefund()));
        
        ///////////Info para el reporte del ANUAL
        Object[] annualSummary = this.serviceDBSale.annualSummary(businessId, startDate.getYear());
        Object[] annualSummaryV = null;
        if (annualSummary != null && annualSummary[0] != null) {
            annualSummaryV = (Object[]) annualSummary[0];
        }
        Object[] annualSummarybefore = this.serviceDBSale.annualSummary(businessId, startDate.getYear() - 1);
        Object[] annualSummaryVbefore = null;
        if (annualSummarybefore != null && annualSummarybefore[0] != null) {
            annualSummaryVbefore = (Object[]) annualSummarybefore[0];
        }
        HashMap<String, Object> annualSummaryDTO = new HashMap<>();
        if (annualSummaryV != null) {
            if (annualSummaryV[0] != null) {
                Double totalSales = Double.parseDouble(annualSummaryV[0].toString());
                annualSummaryDTO.put("totalSales", totalSales);
                Double totalSalesBefore = 0.0;
                if (annualSummaryVbefore != null && annualSummaryVbefore[0] != null) {
                    totalSalesBefore = Double.parseDouble(annualSummaryVbefore[0].toString());


                }
                if (totalSales > totalSalesBefore) {
                    annualSummaryDTO.put("totalSalesStatus", 1);
                } else if (totalSales < totalSalesBefore) {
                    annualSummaryDTO.put("totalSalesStatus", -1);
                } else {
                    annualSummaryDTO.put("totalSalesStatus", 0);
                }
            } else {
                annualSummaryDTO.put("totalSales", 0.0);
                annualSummaryDTO.put("totalSalesStatus", 0);
            }
            Double totalTaxAnnual = 0.0;
            if (annualSummaryV[2] != null) {

                totalTaxAnnual = totalTaxAnnual + Double.parseDouble(annualSummaryV[2].toString());
            }
            if (annualSummaryV[3] != null) {
                totalTaxAnnual = totalTaxAnnual + Double.parseDouble(annualSummaryV[3].toString());
            }
            if (annualSummaryV[4] != null) {
                totalTaxAnnual = totalTaxAnnual + Double.parseDouble(annualSummaryV[4].toString());
            }
            Double totalTaxAnnualBefore = 0.0;
            if (annualSummaryVbefore != null && annualSummaryVbefore[2] != null) {

                totalTaxAnnualBefore = totalTaxAnnualBefore + Double.parseDouble(annualSummaryVbefore[2].toString());
            }
            if (annualSummaryVbefore != null && annualSummaryVbefore[3] != null) {
                totalTaxAnnualBefore = totalTaxAnnualBefore + Double.parseDouble(annualSummaryVbefore[3].toString());
            }
            if (annualSummaryVbefore != null && annualSummaryVbefore[4] != null) {
                totalTaxAnnualBefore = totalTaxAnnualBefore + Double.parseDouble(annualSummaryVbefore[4].toString());
            }
            annualSummaryDTO.put("totalTax", totalTaxAnnual);
            if (totalTaxAnnual > totalTaxAnnualBefore) {
                annualSummaryDTO.put("totalTaxStatus", 1);
            } else if (totalTaxAnnual < totalTaxAnnualBefore) {
                annualSummaryDTO.put("totalTaxStatus", -1);
            } else {
                annualSummaryDTO.put("totalTaxStatus", 0);
            }

            if (annualSummaryV[6] != null) {
                Double grossProfit = Double.parseDouble(annualSummaryV[6].toString());
                annualSummaryDTO.put("grossProfit", grossProfit);
                Double grossProfitBefore = 0.0;
                if (annualSummaryVbefore != null && annualSummaryVbefore[6] != null) {
                    grossProfitBefore = Double.parseDouble(annualSummaryVbefore[6].toString());

                }
                if (grossProfit > grossProfitBefore) {
                    annualSummaryDTO.put("grossProfitStatus", 1);
                } else if (grossProfit < grossProfitBefore) {
                    annualSummaryDTO.put("grossProfitStatus", -1);
                } else {
                    annualSummaryDTO.put("grossProfitStatus", 0);
                }

            } else {
                annualSummaryDTO.put("grossProfit", 0.0);
                annualSummaryDTO.put("grossProfitStatus", 0);
            }
            if (annualSummaryV[8] != null) {
                Double totalWorkCost = Double.parseDouble(annualSummaryV[8].toString());
                annualSummaryDTO.put("totalWorkCost", totalWorkCost);
                Double totalWorkCostBefore = 0.0;
                if (annualSummaryVbefore != null && annualSummaryVbefore[8] != null) {
                    totalWorkCostBefore = Double.parseDouble(annualSummaryVbefore[8].toString());

                }
                if (totalWorkCost > totalWorkCostBefore) {
                    annualSummaryDTO.put("totalWorkCostStatus", 1);
                } else if (totalWorkCost < totalWorkCostBefore) {
                    annualSummaryDTO.put("totalWorkCostStatus", -1);
                } else {
                    annualSummaryDTO.put("totalWorkCostStatus", 0);
                }
            } else {
                annualSummaryDTO.put("totalWorkCost", 0.0);
                annualSummaryDTO.put("totalWorkCostStatus", 0);
            }
        }


        dailySummaryDTO.put("annualSummary", annualSummaryDTO);

        //ACTIVIDAD DE VENTA MENSUAL
        YearMonth yearMonth = YearMonth.of(startDate.getYear(), startDate.getMonth());
        int diasEnMes = yearMonth.lengthOfMonth();
        List<Double> monthlySales = new ArrayList<>();
        for (int i = 0; i < diasEnMes; i++) {
            LocalDate date = LocalDate.of(startDate.getYear(), startDate.getMonth(), i + 1);
            Object[] monthlySummary = this.serviceDBSale.monthlySummary(businessId, date);

            if (monthlySummary != null && monthlySummary[0] != null) {
                System.out.println("MES " + (i + 1) + " -> " + date.toString());
                System.out.println("monthlySummary[0].toString(): " + monthlySummary[0].toString());
                monthlySales.add(Double.parseDouble(monthlySummary[0].toString()));
            } else {
                monthlySales.add(0.0);
            }
        }
        dailySummaryDTO.put("monthlySales", monthlySales);
        //ACTIVIDAD DE VENTA MENSUAL del mes previo
        yearMonth = YearMonth.of(startDate.getYear(), startDate.getMonth().minus(1));
        diasEnMes = yearMonth.lengthOfMonth();
        List<Double> previusMonthlySales = new ArrayList<>();
        for (int i = 0; i < diasEnMes; i++) {
            LocalDate date = LocalDate.of(startDate.getYear(), startDate.getMonth().minus(1), i + 1);
            Object[] monthlySummary = this.serviceDBSale.monthlySummary(businessId, date);

            if (monthlySummary != null && monthlySummary[0] != null) {
                previusMonthlySales.add(Double.parseDouble(monthlySummary[0].toString()));
            } else {
                previusMonthlySales.add(0.0);
            }
        }
        dailySummaryDTO.put("previusMonthlySales", previusMonthlySales);

        /////////////Info para el reporte de LOS 10 PODUCTOS MAS VENDIDOS DEL DIA
        List<HashMap<String, String>> dailySummaryBestSellingProducts = new ArrayList<>();
        Object[] dailySummaryBestSellingItems = this.serviceDBSale.dailySummaryBestSellingItems(businessId, startDate, endDate);
        if (dailySummaryBestSellingItems != null) {
            for (int i = 0; i < dailySummaryBestSellingItems.length; i++) {
                Object[] dailySummaryBestSellingItemsV = (Object[]) dailySummaryBestSellingItems[i];
                HashMap<String, String> bestSellingProducts = new HashMap<>();
                bestSellingProducts.put("name", objectToString(dailySummaryBestSellingItemsV[4]));
                bestSellingProducts.put("quantity", objectToString(dailySummaryBestSellingItemsV[1]));
                bestSellingProducts.put("totalAmount", objectToString(dailySummaryBestSellingItemsV[2]));
                bestSellingProducts.put("benefit", objectToString(dailySummaryBestSellingItemsV[3]));
                bestSellingProducts.put("category", objectToString(dailySummaryBestSellingItemsV[6]));
                bestSellingProducts.put("cost", objectToString(dailySummaryBestSellingItemsV[5]));
                dailySummaryBestSellingProducts.add(bestSellingProducts);
            }
        }
        dailySummaryDTO.put("bestSellingProducts", dailySummaryBestSellingProducts);
        return new ResponseEntity<>(dailySummaryDTO, HttpStatus.OK);
    }

    public static Duration calculateDuration(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
        return Duration.between(startDateTime, endDateTime);
    }

    @Override
    public ResponseEntity<?> getReportPonches(Long businessId, LocalDate startDate, LocalDate endDate, Long filter) {
        List<EntryExitDTO> entryExitDTOs = new ArrayList<>();
        List<EntryExit> entryExits = new ArrayList<>();
        HashMap<Long, Object> empleados = new HashMap<>();

        if (filter != null && filter > 0) {
            entryExits = serviceDBEntryExit.findByUserBusinessIdAndDate(businessId, startDate, endDate, filter);
        } else {
            entryExits = serviceDBEntryExit.findByUserBusinessIdAndDate(businessId, startDate, endDate);
        }
        for (EntryExit entryExit : entryExits) {
            entryExit.setHour(entryExit.getHour().withNano(0));
            if (empleados.containsKey(entryExit.getUserBusiness().getUserBusinessId())) {
                HashMap<String, Object> info = (HashMap<String, Object>) empleados.get(entryExit.getUserBusiness().getUserBusinessId());
                List<EntryExit> ponchesList = (List<EntryExit>) info.get("ponches");
                ponchesList.add(entryExit);
                info.put("ponches", ponchesList);
                empleados.put(entryExit.getUserBusiness().getUserBusinessId(), info);
            } else {
                HashMap<String, Object> info = new HashMap<>();
                List<EntryExit> ponchesList = new ArrayList<>();
                ponchesList.add(entryExit);
                info.put("ponches", ponchesList);
                info.put("userBusinessId", entryExit.getUserBusiness().getUserBusinessId());
                info.put("userName", entryExit.getUserBusiness().getUsername());
                empleados.put(entryExit.getUserBusiness().getUserBusinessId(), info);
            }
        }

        for (Long userBusinessId : empleados.keySet()) {
            HashMap<String, Object> info = (HashMap<String, Object>) empleados.get(userBusinessId);
            List<EntryExit> ponchesList = (List<EntryExit>) info.get("ponches");
            entryExitDTOs = this.mapper.map(ponchesList, new TypeToken<List<EntryExitDTO>>() {
            }.getType());

            info.put("ponches", entryExitDTOs);
            empleados.put(userBusinessId, info);
        }
        List<HashMap<String, Object>> list = new ArrayList<>();
        for (Long userBusinessId : empleados.keySet()) {
            HashMap<String, Object> info = (HashMap<String, Object>) empleados.get(userBusinessId);
            list.add(info);
        }
        return new ResponseEntity<>(list, HttpStatus.OK);

    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> WorkHoursReportService(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> laborHoursVsHourlyCost;
        if (businessId != null) {
            laborHoursVsHourlyCost = usersAppRepository.reporteHorasTrabajadasVsHorasProgramadas(startDate, endDate, businessId);
        } else {
            laborHoursVsHourlyCost = usersAppRepository.reporteHorasTrabajadasVsHorasProgramadas(startDate, endDate);
        }
        return new ResponseEntity<>(mapearReporte(laborHoursVsHourlyCost), HttpStatus.OK);

    }

    public List<HashMap<String, Object>> mapearReporte(List<Object[]> resultados) {
        List<HashMap<String, Object>> listaMapeada = new ArrayList<>();

        for (Object[] fila : resultados) {
            HashMap<String, Object> mapa = new HashMap<>();
            mapa.put("username", fila[0]);
            mapa.put("horas_programadas", fila[1]);
            mapa.put("horas_trabajadas", fila[2]);
            mapa.put("diferencia_horas", fila[3]);
            mapa.put("costo_programado", fila[4]);
            mapa.put("costo_real", fila[5]);
            mapa.put("diferencia_costo", fila[6]);

            listaMapeada.add(mapa);
        }

        return listaMapeada;
    }

    /**
     * Returns a list of user weekly schedules for a given business and date range.
     * The list contains the userBusinessId and the hours for each day of the week.
     * If the businessId is null, the list will contain all users.
     *
     * @param businessId the ID of the business
     * @param startDate  the start date of the date range
     * @param endDate    the end date of the date range
     * @return a ResponseEntity containing a list of user weekly schedules
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> Report_UserWeeklySchedule(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> horariosSemanales = new ArrayList<>();

        if (employeeId != null) {
            horariosSemanales = usersAppRepository.getUserWeeklySchedule(startDate, endDate, employeeId);
        } else {
            horariosSemanales = usersAppRepository.getUserWeeklySchedule(startDate, endDate);
        }

        return new ResponseEntity<>(mapearHorarioSemanal(horariosSemanales), HttpStatus.OK);
    }

    public List<HashMap<String, Object>> mapearHorarioSemanal(List<Object[]> resultados) {
        List<HashMap<String, Object>> listaMapeada = new ArrayList<>();

        for (Object[] fila : resultados) {
            HashMap<String, Object> mapa = new HashMap<>();
            mapa.put("username", fila[0]);
            mapa.put("userBusinessId", fila[1]);
            mapa.put("monday", fila[2]);
            mapa.put("tuesday", fila[3]);
            mapa.put("wednesday", fila[4]);
            mapa.put("thursday", fila[5]);
            mapa.put("friday", fila[6]);
            mapa.put("saturday", fila[7]);
            mapa.put("sunday", fila[8]);
            listaMapeada.add(mapa);
        }

        return listaMapeada;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getEmployeeWeeklyScheduleDetail(Long businessId, Long userBusinessId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> detallesHoras = new ArrayList<>();

        if (businessId != null && userBusinessId == null) {
            detallesHoras = usersAppRepository.getEmployeesWeeklyScheduleByBusiness(businessId, startDate, endDate);
        } else if (businessId != null && userBusinessId != null) {
            detallesHoras = usersAppRepository.getEmployeeWeeklyScheduleDetail(businessId, userBusinessId, startDate, endDate);
        } else if (userBusinessId != null && businessId == null) {
            detallesHoras = usersAppRepository.getEmployeeWeeklyScheduleDetailByEmployee(userBusinessId, startDate, endDate);
        } else {
            detallesHoras = usersAppRepository.getAllEmployeesWeeklyScheduleDetail(startDate, endDate);
        }

        return new ResponseEntity<>(mapearDetalleHorarioSemanal(detallesHoras), HttpStatus.OK);
    }

    public List<HashMap<String, Object>> mapearDetalleHorarioSemanal(List<Object[]> resultados) {
        List<HashMap<String, Object>> listaMapeada = new ArrayList<>();

        for (Object[] fila : resultados) {
            HashMap<String, Object> mapa = new HashMap<>();
            mapa.put("username", fila[0]);
            mapa.put("fecha", fila[1]);
            mapa.put("turno", fila[2]);
            mapa.put("horario", fila[3]);
            mapa.put("horas", fila[4]);
            mapa.put("horasxDia", fila[5]);
            mapa.put("totalHoras", fila[6]);
            mapa.put("userBusinessId", fila[7]);

            listaMapeada.add(mapa);
        }

        return listaMapeada;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> workHoursVsScheduleHours(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> resultado;

        resultado = usersAppRepository.resumenHorasTrabajadasVsHorasProgramadas(startDate, endDate, businessId);


        HashMap<String, Object> response = new HashMap<>();
        if (resultado == null) {
            response.put("total_horas_programadas", 0);
            response.put("total_horas_trabajadas", 0);
            response.put("diferencia_total_horas", 0);
            response.put("total_costo_programado", 0);
            response.put("total_costo_real", 0);
            response.put("diferencia_total_costo", 0);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("total_horas_programadas", resultado.get(0)[0]);
        response.put("total_horas_trabajadas", resultado.get(0)[1]);
        response.put("diferencia_total_horas", resultado.get(0)[2]);
        response.put("total_costo_programado", resultado.get(0)[3]);
        response.put("total_costo_real", resultado.get(0)[4]);
        response.put("diferencia_total_costo", resultado.get(0)[5]);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @Override
    public ResponseEntity<?> getDashboardKpis(Long businessId) {
        Integer workingHours=Integer.parseInt(this.serviceDBBusinessConfiguration.findByKey("Report.workingHours", businessId).getValue());
        DashboardKpiProjection projection =
                serviceDBSale.getDashboardKpis(businessId);
        LaborMetricsProjection labor =
        serviceDBSale.getLaborMetrics(businessId);
        List<HourlySalesDTO> hourlySales =
            serviceDBSale.getHourlySales(businessId)
                    .stream()
                    .map(item -> HourlySalesDTO.builder()
                            .hour(item.getHour())
                            .transactions(item.getTransactions())
                            .sales(item.getSales())
                            .build())
                    .toList();
        Double totalHourlySales = hourlySales.stream().mapToDouble(HourlySalesDTO::getSales).sum();            
        Double salesYoY=projection.getSalesYTD() != null && projection.getSalesLY() != null && projection.getSalesLY() != 0 ? ((projection.getSalesYTD() - projection.getSalesLY()) / projection.getSalesLY()) * 100 : 0;
        Double marginPercentYTD = projection.getProfitYTD() != null && projection.getSalesYTD() != null && projection.getSalesYTD() != 0 ? (projection.getProfitYTD() / projection.getSalesYTD()) * 100 : 0;
        Double marginPercentLY = projection.getProfitLY() != null && projection.getSalesLY() != null && projection.getSalesLY() != 0 ? (projection.getProfitLY() / projection.getSalesLY()) * 100 : 0;
        Double marginPercentYoY = marginPercentLY != null && marginPercentLY != 0 ? ((marginPercentYTD - marginPercentLY) / marginPercentLY) * 100 : 0;
        Double currentCostPerHour =
        labor.getLaborHoursYTD() != null
        && labor.getLaborHoursYTD() != 0
        ? labor.getLaborCostYTD() / labor.getLaborHoursYTD()
        : 0;
        Double lastYearCostPerHour =
        labor.getLaborHoursLY() != null
        && labor.getLaborHoursLY() != 0
        ? labor.getLaborCostLY() / labor.getLaborHoursLY()
        : 0;
        Double costPerHourYoY =
        lastYearCostPerHour != null
        && lastYearCostPerHour != 0
        ? ((currentCostPerHour - lastYearCostPerHour)
            / lastYearCostPerHour) * 100
        : 0;
        Double avgTikectYoY = projection.getAvgTicketYTD() != null && projection.getAvgTicketLY() != null && projection.getAvgTicketLY() != 0 ? ((projection.getAvgTicketYTD() - projection.getAvgTicketLY()) / projection.getAvgTicketLY()) * 100 : 0;
        Double efficiency = projection.getTodaySales() != null && workingHours != null && workingHours != 0 ? projection.getTodaySales() / workingHours : 0;
        Double dailyGrowth = projection.getTodaySales() != null && projection.getYesterdaySales() != null && projection.getYesterdaySales() != 0 ? ((projection.getTodaySales() - projection.getYesterdaySales()) / projection.getYesterdaySales()) * 100 : 0;
        Double salesYesterdayVsPreviousDay = projection.getYesterdaySales() != null && projection.getTwoDaysAgoSales() != null && projection.getTwoDaysAgoSales() != 0 ? ((projection.getYesterdaySales() - projection.getTwoDaysAgoSales()) / projection.getTwoDaysAgoSales()) * 100 : 0;
        Double salesWoW= projection.getThisWeekSales() != null && projection.getLastWeekSalesUntilToday() != null && projection.getLastWeekSalesUntilToday() != 0 ? ((projection.getThisWeekSales() - projection.getLastWeekSalesUntilToday()) / projection.getLastWeekSalesUntilToday()) * 100 : 0;
        Double taxesGrowth = projection.getTodayTaxes() != null && projection.getYesterdayTaxes() != null && projection.getYesterdayTaxes() != 0 ? ((projection.getTodayTaxes() - projection.getYesterdayTaxes()) / projection.getYesterdayTaxes()) * 100 : 0;
        Double taxesYesterdayVsPreviousDay = projection.getYesterdayTaxes() != null && projection.getTwoDaysAgoTaxes() != null && projection.getTwoDaysAgoTaxes() != 0 ? ((projection.getYesterdayTaxes() - projection.getTwoDaysAgoTaxes()) / projection.getTwoDaysAgoTaxes()) * 100 : 0;
        Double taxesWoW = projection.getThisWeekTaxes() != null && projection.getLastWeekTaxesUntilToday() != null && projection.getLastWeekTaxesUntilToday() != 0 ? ((projection.getThisWeekTaxes() - projection.getLastWeekTaxesUntilToday()) / projection.getLastWeekTaxesUntilToday()) * 100 : 0;
        Double profitGrowth = projection.getTodayProfit() != null && projection.getYesterdayProfit() != null && projection.getYesterdayProfit() != 0 ? ((projection.getTodayProfit() - projection.getYesterdayProfit()) / projection.getYesterdayProfit()) * 100 : 0;
        Double profitYesterdayVsPreviousDay = projection.getYesterdayProfit() != null && projection.getTwoDaysAgoProfit() != null && projection.getTwoDaysAgoProfit() != 0 ? ((projection.getYesterdayProfit() - projection.getTwoDaysAgoProfit()) / projection.getTwoDaysAgoProfit()) * 100 : 0;
        Double profitWoW = projection.getThisWeekProfit() != null && projection.getLastWeekProfitUntilToday() != null && projection.getLastWeekProfitUntilToday() != 0 ? ((projection.getThisWeekProfit() - projection.getLastWeekProfitUntilToday()) / projection.getLastWeekProfitUntilToday()) * 100 : 0;
        List<DailySalesDTO> dailySales = serviceDBSale.getDailySalesCurrentMonth(businessId).stream().map(sale -> DailySalesDTO.builder()
                .dayOfMonth(sale.getDayOfMonth())
                .transactions(sale.getTransactions())
                .sales(sale.getSales())
                .build()).collect(Collectors.toList());
        List<DailySalesDTO> previousDailySales = serviceDBSale.getDailySalesLastMonthUntilToday(businessId).stream().map(sale -> DailySalesDTO.builder()
                .dayOfMonth(sale.getDayOfMonth())
                .transactions(sale.getTransactions())
                .sales(sale.getSales())
                .build()).collect(Collectors.toList());
        DashboardKpiDTO response = DashboardKpiDTO.builder()
                .salesYTD(projection.getSalesYTD())
                .salesLY(projection.getSalesLY())
                .profitYTD(projection.getProfitYTD())
                .profitLY(projection.getProfitLY())
                .profitYoY(projection.getProfitYTD() != null && projection.getProfitLY() != null && projection.getProfitLY() != 0 ? ((projection.getProfitYTD() - projection.getProfitLY()) / projection.getProfitLY()) * 100 : 0)
                .avgTicketYTD(projection.getAvgTicketYTD())
                .avgTicketLY(projection.getAvgTicketLY())
                .avgTicketYoY(avgTikectYoY)
                .todaySales(projection.getTodaySales())
                .yesterdaySales(projection.getYesterdaySales())
                .salesYesterdayVsPreviousDay(salesYesterdayVsPreviousDay)
                .salesYesterdayVsPreviousDayStatus(calculateSalesStatus(salesYesterdayVsPreviousDay).name())
                .salesWoW(salesWoW)
                .salesWoWStatus(calculateSalesStatus(salesWoW).name())
                .todayTaxes(projection.getTodayTaxes())
                .yesterdayTaxes(projection.getYesterdayTaxes())
                .taxesYesterdayVsPreviousDay(taxesYesterdayVsPreviousDay)
                .taxesYesterdayVsPreviousDayStatus(calculateTaxesStatus(taxesYesterdayVsPreviousDay).name())
                .taxesWoW(taxesWoW)
                .taxesWoWStatus(calculateTaxesStatus(taxesWoW).name())
                .taxesYTD(projection.getTaxesYTD())
                .todayProfit(projection.getTodayProfit())
                .yesterdayProfit(projection.getYesterdayProfit())
                .profitYesterdayVsPreviousDay(profitYesterdayVsPreviousDay)
                .profitYesterdayVsPreviousDayStatus(calculateProfitStatus(profitYesterdayVsPreviousDay).name())
                .profitWoW(profitWoW)
                .profitWoWStatus(calculateProfitStatus(profitWoW).name())
                .profitGrowthPercent(profitGrowth)
                .profitGrowthPercentStatus(calculateProfitStatus(profitGrowth).name())
                .todayTransactions(projection.getTodayTransactions())
                .transactionsYTD(projection.getTransactionsYTD())
                .laborCostYTD(labor.getLaborCostYTD())
                .laborHoursYTD(labor.getLaborHoursYTD())
                .laborCostLY(labor.getLaborCostLY())
                .laborHoursLY(labor.getLaborHoursLY())
                .hourlySales(hourlySales)
                .salesYoY(salesYoY  )
                .marginPercentYTD(marginPercentYTD)
                .marginPercentLY(marginPercentLY)
                .marginPercentYoY(marginPercentYoY)
                .todayMarginPercent((projection.getTodayProfit() != null && projection.getTodaySales() != null && projection.getTodaySales() != 0) ? (projection.getTodayProfit() / projection.getTodaySales()) * 100 : 0)
                .costPerHourYoY(costPerHourYoY)
                .avgSalesPerHour(projection.getTodaySales()/workingHours)
                .avgTransactionsPerHour(projection.getTodayTransactions() != null && workingHours != null && workingHours != 0 ? (double) projection.getTodayTransactions() / workingHours : 0)
                .dailyGrowthPercent(dailyGrowth)
                .dailyGrowthPercentStatus(calculateDailyStatus(dailyGrowth).name())
                .taxesGrowthPercent(taxesGrowth)
                .taxesGrowthPercentStatus(calculateTaxesStatus(taxesGrowth).name())
                .salesStatus(calculateSalesStatus(salesYoY).name())
                .marginStatus(calculateMarginStatus(marginPercentYoY).name()) //TODO: AJUSTAR LOS VALORES DE LOS KPI DEL SEMAPHORE
                .laborStatus(calculateLaborStatus(costPerHourYoY).name())   
                .avgTicketStatus(calculateAvgTicketStatus(avgTikectYoY).name())
                .efficiency(efficiency)
                .dailySales(dailySales)
                .previousDailySales(previousDailySales)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    private KPIStatus calculateProfitStatus(Double profitGrowth) {

    if (profitGrowth == null) {
        return KPIStatus.RED;
    }

    // >= +2%
    if (profitGrowth >= 2) {
        return KPIStatus.GREEN;
    }

    // entre -2% y +2%
    if (profitGrowth >= -2 && profitGrowth < 2) {
        return KPIStatus.YELLOW;
    }

    // < -2%
    return KPIStatus.RED;
}
    private KPIStatus calculateTaxesStatus(Double taxesGrowth) {

    if (taxesGrowth == null) {
        return KPIStatus.RED;
    }

    // >= +2%
    if (taxesGrowth >= 2) {
        return KPIStatus.GREEN;
    }

    // entre -2% y +2%
    if (taxesGrowth >= -2 && taxesGrowth < 2) {
        return KPIStatus.YELLOW;
    }

    // < -2%
    return KPIStatus.RED;
}
    private KPIStatus calculateDailyStatus(Double dailyGrowth) {

    if (dailyGrowth == null) {
        return KPIStatus.RED;
    }

    // >= +2%
    if (dailyGrowth >= 2) {
        return KPIStatus.GREEN;
    }

    // entre -2% y +2%
    if (dailyGrowth >= -2 && dailyGrowth < 2) {
        return KPIStatus.YELLOW;
    }

    // < -2%
    return KPIStatus.RED;
}
    private KPIStatus calculateSalesStatus(Double salesYoY) {

    if (salesYoY == null) {
        return KPIStatus.RED;
    }

    if (salesYoY >= 2) {
        return KPIStatus.GREEN;
    }

    if (salesYoY >= -5) {
        return KPIStatus.YELLOW;
    }

    return KPIStatus.RED;
}

private KPIStatus calculateMarginStatus(Double marginPercentYoY) {

    if (marginPercentYoY == null) {
        return KPIStatus.RED;
    }

    // VERDE >= +0.3 pts
    if (marginPercentYoY >= 0.3) {
        return KPIStatus.GREEN;
    }

    // AMARILLO entre -0.5 y +0.3
    if (marginPercentYoY > -0.5) {
        return KPIStatus.YELLOW;
    }

    // ROJO <= -0.5
    return KPIStatus.RED;
}

private KPIStatus calculateLaborStatus(Double costPerHourYoY) {

    if (costPerHourYoY == null) {
        return KPIStatus.RED;
    }

    // BAJAR COSTO = BUENO

    if (costPerHourYoY <= -2) {
        return KPIStatus.GREEN;
    }

    if (costPerHourYoY < 5) {
        return KPIStatus.YELLOW;
    }

    return KPIStatus.RED;
}
private KPIStatus calculateAvgTicketStatus(Double avgTicketYoy) {

    if (avgTicketYoy == null) {
        return KPIStatus.RED;
    }

    // >= +2%
    if (avgTicketYoy >= 2) {
        return KPIStatus.GREEN;
    }

    // entre -2% y +2%
    if (avgTicketYoy > -2 && avgTicketYoy < 2) {
        return KPIStatus.YELLOW;
    }

    // <= -2%
    return KPIStatus.RED;
}
}
