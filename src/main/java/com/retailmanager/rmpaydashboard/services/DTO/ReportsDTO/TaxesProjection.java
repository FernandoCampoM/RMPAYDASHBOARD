package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

public interface TaxesProjection {

    Double getTotalTax();

    Double getTotalSales();
    Double getSubTotalSales();

    Double getTotalStatalTax();

    Double getTotalCityTax();

    Double getTotalReduceTax();

    Double getTotalTaxableSales();
}