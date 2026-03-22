package com.retailmanager.rmpaydashboard.services.services.ReportsServices;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.EntryExit;
import com.retailmanager.rmpaydashboard.models.ItemForSale;
import com.retailmanager.rmpaydashboard.models.Product;
import com.retailmanager.rmpaydashboard.models.Sale;
import com.retailmanager.rmpaydashboard.models.Transactions;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.EntryExitRepository;
import com.retailmanager.rmpaydashboard.repositories.ProductRepository;
import com.retailmanager.rmpaydashboard.repositories.SaleRepository;
import com.retailmanager.rmpaydashboard.repositories.TransactionsRepository;
import com.retailmanager.rmpaydashboard.repositories.UsersAppRepository;
import com.retailmanager.rmpaydashboard.services.DTO.EntryExitDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.DailySummaryDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.EarningsReportDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.TipsReportDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TransactionDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService implements IReportService {
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private BusinessRepository serviceDBBusiness;
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
        Object[] dailySummary = this.serviceDBSale.dailySummary(businessId, startUtc, endUtc);
        Object[] dailySummaryV = null;
        if (dailySummary != null && dailySummary[0] != null) {
            dailySummaryV = (Object[]) dailySummary[0];
        } else {
            return new ResponseEntity<String>("{\"message\":\"No existen ventas para el Business con businessId " + businessId + "\"}", HttpStatus.NOT_FOUND);
        }

        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }

        if (dailySummaryV[0] != null) {
            dailySummaryDTO.setTotalSales(Double.parseDouble(dailySummaryV[0].toString()));
        }
        if (dailySummaryV[1] != null) {
            dailySummaryDTO.setTotalRefunds(Double.parseDouble(dailySummaryV[1].toString()));
        }
        if (dailySummaryV[2] != null) {
            dailySummaryDTO.setStateTax(Double.parseDouble(dailySummaryV[2].toString()));
        }
        if (dailySummaryV[3] != null) {
            dailySummaryDTO.setMunicipalTax(Double.parseDouble(dailySummaryV[3].toString()));
        }
        if (dailySummaryV[4] != null) {
            dailySummaryDTO.setEstimatedRedTax(Double.parseDouble(dailySummaryV[4].toString()));
        }
        List<Sale> sales = this.serviceDBSale.findBySaleTransactionTypeAndSaleStatusAndBusinessAndSaleEndDateBetween("SALE", "SUCCEED", business, startUtc, endUtc);
        Double benefit = 0.0;
        Double propinas = 0.0;
        if (sales != null && sales.size() > 0) {
            for (Sale sale : sales) {
                for (ItemForSale item : sale.getItemsList()) {
                    benefit += item.getGrossProfit();
                }
                propinas += sale.getTipAmount();
            }
        }
        dailySummaryDTO.setBenefit(benefit);
        dailySummaryDTO.setTotalTips(propinas);

        Object[] dailySummaryByCategory = this.serviceDBSale.dailySummaryForCategory(businessId, startUtc, endUtc);
        if (dailySummaryByCategory != null) {
            for (int i = 0; i < dailySummaryByCategory.length; i++) {
                Object[] dailySummaryByCategoryV = (Object[]) dailySummaryByCategory[i];
                HashMap<String, String> salesByCategory = new HashMap<>();
                salesByCategory.put("category", objectToString(dailySummaryByCategoryV[0]));
                salesByCategory.put("totalAmount", objectToString(dailySummaryByCategoryV[1]));
                dailySummaryDTO.getSalesByCategory().add(salesByCategory);
            }

        }
        Object[] dailySummaryBestSellingItems = this.serviceDBSale.dailySummaryBestSellingItems(businessId, startUtc, endUtc);
        if (dailySummaryBestSellingItems != null) {
            for (int i = 0; i < dailySummaryBestSellingItems.length; i++) {
                Object[] dailySummaryBestSellingItemsV = (Object[]) dailySummaryBestSellingItems[i];
                HashMap<String, String> bestSellingProducts = new HashMap<>();
                bestSellingProducts.put("name", objectToString(dailySummaryBestSellingItemsV[4]));
                bestSellingProducts.put("quantity", objectToString(dailySummaryBestSellingItemsV[1]));
                bestSellingProducts.put("totalAmount", objectToString(dailySummaryBestSellingItemsV[2]));
                bestSellingProducts.put("benefit", objectToString(dailySummaryBestSellingItemsV[3]));
                dailySummaryDTO.getBestSellingProducts().add(bestSellingProducts);
            }

        }
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

        Object[] dailySummary = this.serviceDBSale.dailySummary(businessId, startUtc, endUtc);
        Object[] dailySummaryV = null;
        if (dailySummary != null && dailySummary[0] != null) {
            dailySummaryV = (Object[]) dailySummary[0];
        } else {
            return new ResponseEntity<String>("{\"message\":\"No existen ventas para el Business con businessId " + businessId + "\"}", HttpStatus.NOT_FOUND);
        }
        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }

        if (dailySummaryV[0] != null) {
            dailySummaryDTO.setTotalSales(Double.parseDouble(dailySummaryV[0].toString()));
        }
        if (dailySummaryV[1] != null) {
            dailySummaryDTO.setTotalRefunds(Double.parseDouble(dailySummaryV[1].toString()));
        }
        if (dailySummaryV[2] != null) {
            dailySummaryDTO.setStateTax(Double.parseDouble(dailySummaryV[2].toString()));
        }
        if (dailySummaryV[3] != null) {
            dailySummaryDTO.setMunicipalTax(Double.parseDouble(dailySummaryV[3].toString()));
        }
        if (dailySummaryV[4] != null) {
            dailySummaryDTO.setEstimatedRedTax(Double.parseDouble(dailySummaryV[4].toString()));
        }
        List<Sale> sales = this.serviceDBSale.findBySaleTransactionTypeAndSaleStatusAndBusinessAndSaleEndDateBetween("SALE", "SUCCEED", business, startUtc, endUtc);
        HashMap<String, Double> payMethosSales = new HashMap<>();
        Double benefit = 0.0;
        Double propinas = 0.0;
        if (sales != null && sales.size() > 0) {
            for (Sale sale : sales) {
                for (ItemForSale item : sale.getItemsList()) {
                    benefit += item.getGrossProfit();
                }
                for (Transactions transaction : sale.getTransactions()) {
                    if (payMethosSales.containsKey(transaction.getPaymentType())) {
                        payMethosSales.put(transaction.getPaymentType(), payMethosSales.get(transaction.getPaymentType()) + transaction.getAmount());
                    } else {
                        payMethosSales.put(transaction.getPaymentType(), transaction.getAmount());
                    }
                }
                propinas += sale.getTipAmount();
            }
        }
        dailySummaryDTO.setBenefit(benefit);
        dailySummaryDTO.setTotalTips(propinas);
        for (Map.Entry<String, Double> entry : payMethosSales.entrySet()) {
            HashMap<String, String> payMethosSalesV = new HashMap<>();
            payMethosSalesV.put("paymentType", entry.getKey());
            payMethosSalesV.put("totalAmount", entry.getValue().toString());
            dailySummaryDTO.getBestSellingPayMethods().add(payMethosSalesV);
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
        Object[] dailySummaryByCategory = this.serviceDBSale.summaryForCategory(businessId, startUtc, endUtc);
        if (dailySummaryByCategory != null) {
            for (int i = 0; i < dailySummaryByCategory.length; i++) {
                Object[] dailySummaryByCategoryV = (Object[]) dailySummaryByCategory[i];
                HashMap<String, String> salesByCategory = new HashMap<>();
                salesByCategory.put("category", objectToString(dailySummaryByCategoryV[0]));
                salesByCategory.put("totalAmount", objectToString(dailySummaryByCategoryV[1]));
                dailySummaryDTO.getSalesByCategory().add(salesByCategory);
            }

        }


        dailySummaryDTO.setBestSellingProducts(new ArrayList<>());

        Object[] dailySummaryBestSellingItems = this.serviceDBSale.dailySummaryBestSellingItems(businessId, startUtc, endUtc);
        if (dailySummaryBestSellingItems != null) {
            for (int i = 0; i < dailySummaryBestSellingItems.length; i++) {
                Object[] dailySummaryBestSellingItemsV = (Object[]) dailySummaryBestSellingItems[i];
                HashMap<String, String> bestSellingProducts = new HashMap<>();
                bestSellingProducts.put("name", objectToString(dailySummaryBestSellingItemsV[4]));
                bestSellingProducts.put("quantity", objectToString(dailySummaryBestSellingItemsV[1]));
                bestSellingProducts.put("totalAmount", objectToString(dailySummaryBestSellingItemsV[2]));
                bestSellingProducts.put("benefit", objectToString(dailySummaryBestSellingItemsV[3]));
                dailySummaryDTO.getBestSellingProducts().add(bestSellingProducts);
            }

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
        if (!this.serviceDBBusiness.existsById(businessId)) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }
        List<HashMap<String, String>> salesByCategoryDTOs = new ArrayList<>();
        Object[] salesByCategory = this.serviceDBSale.getBestSellingItemsXCategory(businessId, startUtc, endUtc);
        for (int i = 0; i < salesByCategory.length; i++) {
            HashMap<String, String> data = new HashMap<>();
            Object[] salesByCategoryV = (Object[]) salesByCategory[i];
            if (salesByCategoryV[0] != null) {
                data.put("category", objectToString(salesByCategoryV[0]));
            }
            if (salesByCategoryV[1] != null) {
                data.put("totalItems", objectToString(salesByCategoryV[1]));
            }
            if (salesByCategoryV[2] != null) {
                data.put("totalAmount", objectToString(salesByCategoryV[2]));
            }
            if (salesByCategoryV[3] != null) {
                data.put("totalCost", objectToString(salesByCategoryV[3]));
            }
            if (salesByCategoryV[4] != null) {
                data.put("totalGrossProfit", objectToString(salesByCategoryV[4]));
            }
            salesByCategoryDTOs.add(data);
        }

        return new ResponseEntity<>(salesByCategoryDTOs, HttpStatus.OK);
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

        Object[] dailySummary = this.serviceDBSale.dailySummary(businessId, startUtc, endUtc);
        Object[] dailySummaryV = null;
        if (dailySummary != null && dailySummary[0] != null) {
            dailySummaryV = (Object[]) dailySummary[0];
        } else {
            return new ResponseEntity<String>("{\"message\":\"No existen ventas para el Business con businessId " + businessId + "\"}", HttpStatus.NOT_FOUND);
        }
        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }

        if (dailySummaryV[0] != null) {
            dailySummaryDTO.setTotalSales(Double.parseDouble(dailySummaryV[0].toString()));
        }

        if (dailySummaryV[2] != null) {
            dailySummaryDTO.setStateTax(Double.parseDouble(dailySummaryV[2].toString()));
        }
        if (dailySummaryV[3] != null) {
            dailySummaryDTO.setMunicipalTax(Double.parseDouble(dailySummaryV[3].toString()));
        }
        if (dailySummaryV[4] != null) {
            dailySummaryDTO.setEstimatedRedTax(Double.parseDouble(dailySummaryV[4].toString()));
        }
        if (dailySummaryV[5] != null) {
            dailySummaryDTO.setSubTotalSales(Double.parseDouble(dailySummaryV[5].toString()));
        }
        List<Sale> sales = this.serviceDBSale.findBySaleTransactionTypeAndSaleStatusAndBusinessAndSaleEndDateBetween("SALE", "SUCCEED", business, startUtc, endUtc);

        Double benefit = 0.0;
        Double propinas = 0.0;
        if (sales != null && sales.size() > 0) {
            for (Sale sale : sales) {
                for (ItemForSale item : sale.getItemsList()) {
                    benefit += item.getGrossProfit();
                }
                propinas += sale.getTipAmount();
            }
        }
        dailySummaryDTO.setBenefit(benefit);
        List<HashMap<String, String>> earningsByCategoryDTOs = new ArrayList<>();
        Object[] salesByCategory = this.serviceDBSale.getBestSellingItemsXCategory(businessId, startUtc, endUtc);
        for (int i = 0; i < salesByCategory.length; i++) {
            HashMap<String, String> data = new HashMap<>();
            Object[] salesByCategoryV = (Object[]) salesByCategory[i];
            if (salesByCategoryV[0] != null) {
                data.put("category", objectToString(salesByCategoryV[0]));
            }

            if (salesByCategoryV[4] != null) {
                data.put("totalGrossProfit", objectToString(salesByCategoryV[4]));
            }
            earningsByCategoryDTOs.add(data);
        }
        dailySummaryDTO.setEarningsByCategory(earningsByCategoryDTOs);

        Object[] dailySummaryBestSellingItems = this.serviceDBSale.dailySummaryBestSellingItems(businessId, startUtc, endUtc);
        if (dailySummaryBestSellingItems != null) {
            for (int i = 0; i < dailySummaryBestSellingItems.length; i++) {
                Object[] dailySummaryBestSellingItemsV = (Object[]) dailySummaryBestSellingItems[i];
                HashMap<String, String> bestSellingProducts = new HashMap<>();
                bestSellingProducts.put("name", objectToString(dailySummaryBestSellingItemsV[4]));
                bestSellingProducts.put("quantity", objectToString(dailySummaryBestSellingItemsV[1]));
                bestSellingProducts.put("totalAmount", objectToString(dailySummaryBestSellingItemsV[2]));
                bestSellingProducts.put("benefit", objectToString(dailySummaryBestSellingItemsV[3]));
                dailySummaryDTO.getBestSellingProducts().add(bestSellingProducts);
            }

        }

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

        Object[] dailySummary = this.serviceDBSale.dailySummary(businessId, startUtc, endUtc);
        Object[] dailySummaryV = null;
        if (dailySummary != null && dailySummary[0] != null) {
            dailySummaryV = (Object[]) dailySummary[0];
        } else {
            return new ResponseEntity<String>("{\"message\":\"No existen ventas para el Business con businessId " + businessId + "\"}", HttpStatus.NOT_FOUND);
        }
        Business business = serviceDBBusiness.findById(businessId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }

        if (dailySummaryV[0] != null) {
            dailySummaryDTO.setTotalSales(Double.parseDouble(dailySummaryV[0].toString()));
        }
        if (dailySummaryV[5] != null) {
            dailySummaryDTO.setSubTotalSales(Double.parseDouble(dailySummaryV[5].toString()));
        }
        List<Sale> sales = this.serviceDBSale.findBySaleTransactionTypeAndSaleStatusAndBusinessAndSaleEndDateBetween("SALE", "SUCCEED", business, startUtc, endUtc);

        Double benefit = 0.0;
        Double propinas = 0.0;
        if (sales != null && sales.size() > 0) {
            for (Sale sale : sales) {
                for (ItemForSale item : sale.getItemsList()) {
                    benefit += item.getGrossProfit();
                }
                propinas += sale.getTipAmount();
            }
        }
        dailySummaryDTO.setTotalTips(propinas);
        dailySummaryDTO.setUserTips(new ArrayList<>());
        Object[] tipsByUsers = this.serviceDBSale.getUserTipsReport(businessId, startUtc, endUtc);
        if (tipsByUsers != null) {
            for (int i = 0; i < tipsByUsers.length; i++) {
                Object[] dailySummaryBestSellingItemsV = (Object[]) tipsByUsers[i];
                HashMap<String, String> bestSellingProducts = new HashMap<>();
                bestSellingProducts.put("username", objectToString(dailySummaryBestSellingItemsV[1]));
                bestSellingProducts.put("totalSales", objectToString(dailySummaryBestSellingItemsV[2]));
                bestSellingProducts.put("subTotalSales", objectToString(dailySummaryBestSellingItemsV[3]));
                bestSellingProducts.put("totalTips", objectToString(dailySummaryBestSellingItemsV[4]));
                bestSellingProducts.put("benefit", objectToString(dailySummaryBestSellingItemsV[5]));
                dailySummaryDTO.getUserTips().add(bestSellingProducts);
            }
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

        ///////////Info para el reporte del dia
        Object[] dailySummary = this.serviceDBSale.dailySummary(businessId, startDate, endDate);
        Object[] dailySummaryV = null;
        if (dailySummary != null && dailySummary[0] != null) {
            dailySummaryV = (Object[]) dailySummary[0];
        } else {
            return new ResponseEntity<String>("{\"message\":\"No existen ventas para el Business con businessId " + businessId + "\"}", HttpStatus.NOT_FOUND);
        }
        HashMap<String, Object> dailySummaryDTO = new HashMap<>();
        if (dailySummaryV[0] != null) {
            dailySummaryDTO.put("totalSales", Double.parseDouble(dailySummaryV[0].toString()));
        } else {
            dailySummaryDTO.put("totalSales", 0.0);
        }
        Double totalTax = 0.0;
        if (dailySummaryV[2] != null) {
            totalTax = totalTax + Double.parseDouble(dailySummaryV[2].toString());
        }
        if (dailySummaryV[3] != null) {
            totalTax = totalTax + Double.parseDouble(dailySummaryV[3].toString());
        }
        if (dailySummaryV[4] != null) {
            totalTax = totalTax + Double.parseDouble(dailySummaryV[4].toString());
        }
        if (dailySummaryV[2] != null) {
            dailySummaryDTO.put("totalTax", totalTax);
        } else {
            dailySummaryDTO.put("totalTax", 0.0);
        }
        if (dailySummaryV[6] != null) {
            dailySummaryDTO.put("grossProfit", Double.parseDouble(dailySummaryV[6].toString()));
        } else {
            dailySummaryDTO.put("grossProfit", 0.0);
        }
        if (dailySummaryV[7] != null) {
            dailySummaryDTO.put("totalTips", Double.parseDouble(dailySummaryV[7].toString()));
        } else {
            dailySummaryDTO.put("totalTips", 0.0);
        }
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

}
