package com.retailmanager.rmpaydashboard.services.services.SaleService;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.SaleDTO;

public interface ISaleService {
    public ResponseEntity<?> addSale(SaleDTO saleDTO);
    public ResponseEntity<?> UpdateSale(String saleId, SaleDTO saleDTO);
    public ResponseEntity<?> UpdateStatus(String saleId, String status);
    //public void closeSale(int id, String status, String endData) ;
    public ResponseEntity<?> getAllSales(String merchantId);
    public ResponseEntity<?> getAllSales(String merchantId,String terminalId);
    public ResponseEntity<?> getRecentSales(String merchantId, String terminalId, int days);

    public ResponseEntity<?> getCompletedSales(String merchantId);
    public ResponseEntity<?> getCompletedSalesByDateRange(String merchantId,LocalDate startDate, LocalDate endDate);
}
