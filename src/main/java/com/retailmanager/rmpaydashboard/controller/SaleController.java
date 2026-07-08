package com.retailmanager.rmpaydashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.services.DTO.SaleDTO;
import com.retailmanager.rmpaydashboard.services.services.SaleService.ISaleService;

import jakarta.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@Validated
public class SaleController {

    @Autowired
    private ISaleService saleService;

    /**
     * addSale - A description of the entire Java function.
     *
     * @param  saleDTO	description of parameter
     * @return         	description of return value
     */
    @PostMapping("/sales")
    public ResponseEntity<?> addSale(@Valid @RequestBody SaleDTO saleDTO) {
        return saleService.addSale(saleDTO);
    }
    @PutMapping("/sales/{saleId}")
    public ResponseEntity<?> updateSale(@PathVariable String saleId, @Valid @RequestBody SaleDTO saleDTO) {
        return saleService.UpdateSale(saleId, saleDTO);
    }
    @PutMapping("/sales/{saleId}/status/{status}")
    public ResponseEntity<?> updateSaleStatus(@PathVariable String saleId, @PathVariable String status) {
        return saleService.UpdateStatus(saleId, status);
    }

    /**
     * Get all sales for the given merchant ID.
     *
     * @param  merchantId	description of parameter
     * @return         	description of return value
     */
    @GetMapping("/sales")
    public ResponseEntity<?> getAllSales(@RequestParam(name = "merchantId") @Valid String merchantId,
                                         @RequestParam( required = false) String terminalId) {
                                            if(terminalId!=null && !terminalId.isEmpty()){
                                                return saleService.getAllSales(merchantId, terminalId);
                                            }
        return saleService.getAllSales(merchantId);
    }

    @GetMapping("/sales/recent")
    public ResponseEntity<?> getRecentSales(@RequestParam(name = "merchantId") @Valid String merchantId,
                                            @RequestParam(required = false) String terminalId,
                                            @RequestParam(defaultValue = "2") int days) {
        return saleService.getRecentSales(merchantId, terminalId, days);
    }

    /**
     * Retrieves completed sales for a specific merchant.
     *
     * @param  merchantId   the ID of the merchant
     * @return              the ResponseEntity containing the completed sales
     */
    @GetMapping("/sales/succeed")
    public ResponseEntity<?> getCompletedSales(@RequestParam(name = "merchantId") @Valid String merchantId) {
        return saleService.getCompletedSales(merchantId);
    }

    /**
     * Get completed sales by date range.
     *
     * @param  merchantId	description of parameter
     * @param  startDate	description of parameter
     * @param  endDate	    description of parameter
     * @return         	description of return value
     */
    @GetMapping("/sales/succeed/range")
    public ResponseEntity<?> getCompletedSalesByDateRange(@RequestParam(name = "merchantId") @Valid String merchantId,
                                                          @RequestParam(name = "startDate") @Valid LocalDate startDate,
                                                          @RequestParam(name = "endDate") @Valid LocalDate endDate) {
        return saleService.getCompletedSalesByDateRange(merchantId, startDate, endDate);
    }
}
