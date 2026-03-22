package com.retailmanager.rmpaydashboard.services.services.SaleService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.ItemForSale;
import com.retailmanager.rmpaydashboard.models.Sale;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.ProductRepository;
import com.retailmanager.rmpaydashboard.repositories.SaleRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.services.DTO.SaleDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleService implements ISaleService {
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private BusinessRepository serviceDBBusiness;
    @Autowired
    private TerminalRepository serviceDBTerminal;
    @Autowired
    private SaleRepository serviceDBSale;
    @Autowired
    private ProductRepository serviceDBProduct;

    /**
     * Adds a sale to the database.
     *
     * @param saleDTO the sale to be added
     * @return a ResponseEntity containing the added sale or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> addSale(SaleDTO saleDTO) {

        Business business = this.serviceDBBusiness.findById(saleDTO.getBusinessId()).orElse(null);
        Terminal terminal = this.serviceDBTerminal.findById(saleDTO.getTerminalId()).get();
        if (this.serviceDBSale.existsById(saleDTO.getSaleID())) {
            throw new EntidadYaExisteException("La venta con saleID " + saleDTO.getSaleID() + " ya existe en la Base de datos");
        }
        Sale sale = mapper.map(saleDTO, Sale.class);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + saleDTO.getBusinessId() + " no existe en la Base de datos");
        }
        if (terminal == null) {
            throw new EntidadNoExisteException("El Terminal con businessId " + saleDTO.getBusinessId() + " no existe en la Base de datos");
        }

        if (sale != null) {
            try {
                sale.setTerminal(terminal);
                sale.setBusiness(business);
                sale.setItemsList(jsonToListItems(saleDTO.getItems(), sale));
                ////Descontar los items del los productos
                for (ItemForSale item : sale.getItemsList()) {
                    serviceDBProduct.reduceInventory(item.getProductId(), item.getQuantity());
                }
                sale = this.serviceDBSale.save(sale);
                saleDTO.setSaleID(sale.getSaleID());
                terminal.setLastTransmision(Instant.now());
                serviceDBTerminal.save(terminal);

                return new ResponseEntity<>(saleDTO, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);

    }

    /**
     * Retrieves all sales for a specific merchant.
     *
     * @param merchantId The ID of the merchant
     * @return ResponseEntity containing a list of SaleDTO objects with HttpStatus OK
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllSales(String merchantId) {
        Business business = this.serviceDBBusiness.findOneByMerchantId(merchantId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con merchantId " + merchantId + " no existe en la Base de datos");
        }
        List<Sale> sales = this.serviceDBSale.findByBusiness(business);


        List<SaleDTO> salesDTO = sales.stream()
                .map(sale -> {
                    SaleDTO saleDTO = SaleDTO.fromEntity(sale);
                    // ... añade todos los demás campos
                    return saleDTO;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<List<SaleDTO>>(salesDTO, HttpStatus.OK);
    }

    /**
     * Retrieves completed sales for a specific merchant.
     *
     * @param merchantId the ID of the merchant
     * @return a response entity containing a list of completed sales DTOs and HTTP status OK
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCompletedSales(String merchantId) {
        Business business = this.serviceDBBusiness.findOneByMerchantId(merchantId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con merchantId " + merchantId + " no existe en la Base de datos");
        }
        List<Sale> sales = this.serviceDBSale.findBySaleTransactionTypeAndSaleStatusAndBusiness("SALE", "SUCCEED", business);
        List<SaleDTO> salesDTO = sales.stream()
                .map(sale -> {
                    SaleDTO saleDTO = SaleDTO.fromEntity(sale);
                    // ... añade todos los demás campos
                    return saleDTO;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<List<SaleDTO>>(salesDTO, HttpStatus.OK);
    }

    /**
     * Retrieves completed sales within a specified date range for a given merchant.
     *
     * @param merchantId the ID of the merchant
     * @param startDate  the start date of the date range
     * @param endDate    the end date of the date range
     * @return a ResponseEntity containing a list of completed sales within the specified date range
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCompletedSalesByDateRange(String merchantId, LocalDate startDate, LocalDate endDate) {
        Business business = this.serviceDBBusiness.findOneByMerchantId(merchantId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con merchantId " + merchantId + " no existe en la Base de datos");
        }

        List<Sale> sales = this.serviceDBSale.findBySaleEndDateBetweenAndSaleTransactionTypeAndSaleStatusAndBusiness(startDate, endDate, "SALE", "SUCCEED", business);
        List<SaleDTO> salesDTO = sales.stream()
                .map(sale -> {
                    SaleDTO saleDTO = SaleDTO.fromEntity(sale);
                    // ... añade todos los demás campos
                    return saleDTO;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<List<SaleDTO>>(salesDTO, HttpStatus.OK);
    }


    /**
     * Converts a JSON string to a list of items for sale and associates each item with a sale object.
     *
     * @param json the JSON string to convert
     * @param sale the sale object to associate with each item
     * @return a list of items for sale with the sale object set
     */
    private List<ItemForSale> jsonToListItems(String json, Sale sale) {
        try {
            if (json == null) {
                return new ArrayList<>();
            }
            //Gson gson = new Gson();

            //List<ItemForSale> items = gson.fromJson(json, new com.google.gson.reflect.TypeToken<List<ItemForSale>>(){}.getType());
            ObjectMapper objectMapper = new ObjectMapper();

            // Convertir JSON a lista de objetos
            List<ItemForSale> items = objectMapper.readValue(json, new TypeReference<List<ItemForSale>>() {
            });
            items.forEach(item -> item.setSale(sale));
            return items;
        } catch (Exception e) {
            System.out.println("Eror en SaleService.jsonToListItems: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    /**
     * Updates a sale in the database.
     *
     * @param saleId  the ID of the sale to update
     * @param saleDTO the updated sale information
     * @return a ResponseEntity containing the updated sale or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> UpdateSale(String saleId, SaleDTO saleDTO) {
        Business business = this.serviceDBBusiness.findById(saleDTO.getBusinessId()).orElse(null);
        Terminal terminal = this.serviceDBTerminal.findById(saleDTO.getTerminalId()).orElse(null);
        Sale sale = this.serviceDBSale.findById(saleId).orElse(null);
        if (sale == null) {
            throw new EntidadNoExisteException("La venta con saleID " + saleId + " no existe en la Base de datos");
        }

        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + saleDTO.getBusinessId() + " no existe en la Base de datos");
        }
        if (terminal == null) {
            throw new EntidadNoExisteException("El Terminal con id " + saleDTO.getTerminalId() + " no existe en la Base de datos");
        }
        sale.setRemoto(saleDTO.getRemoto());
        sale.setSaleChange(saleDTO.getSaleChange());
        sale.setSaleCityTaxAmount(saleDTO.getSaleCityTaxAmount());
        sale.setSaleCreationDate(saleDTO.getSaleCreationDate());
        sale.setSaleEndDate(saleDTO.getSaleEndDate());
        sale.setSaleIvuNumber(saleDTO.getSaleIvuNumber());
        sale.setSaleMachineID(saleDTO.getSaleMachineID());
        sale.setSaleReduceTax(saleDTO.getSaleReduceTax());
        sale.setSaleStateTaxAmount(saleDTO.getSaleStateTaxAmount());
        sale.setSaleStatus(saleDTO.getSaleStatus());
        sale.setSaleSubtotal(saleDTO.getSaleSubtotal());
        sale.setSaleToRefund(saleDTO.getSaleToRefund());
        sale.setSaleTotalAmount(saleDTO.getSaleTotalAmount());
        sale.setSaleTransactionType(saleDTO.getSaleTransactionType());
        sale.setTipAmount(saleDTO.getTipAmount());
        sale.setTipPercentage(saleDTO.getTipPercentage());
        sale.setUserId(saleDTO.getUserId());
        if (sale != null) {
            try {
                sale.setTerminal(terminal);
                sale.setBusiness(business);
                sale.setItemsList(jsonToListItems(saleDTO.getItems(), sale));
                ////Descontar los items del los productos
                for (ItemForSale item : sale.getItemsList()) {
                    serviceDBProduct.reduceInventory(item.getProductId(), item.getQuantity());
                }
                sale = this.serviceDBSale.save(sale);
                saleDTO.setSaleID(sale.getSaleID());
                terminal.setLastTransmision(Instant.now());
                serviceDBTerminal.save(terminal);

                return new ResponseEntity<>(saleDTO, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllSales(String merchantId, String terminalId) {
        Business business = this.serviceDBBusiness.findOneByMerchantId(merchantId).orElse(null);
        if (business == null) {
            throw new EntidadNoExisteException("El Business con merchantId " + merchantId + " no existe en la Base de datos");
        }
        List<Sale> sales = this.serviceDBSale.findByMerchantIdAndTerminalId(terminalId, merchantId);

        List<SaleDTO> salesDTO = sales.stream()
                .map(sale -> {
                    SaleDTO saleDTO = SaleDTO.fromEntity(sale);
                    // ... añade todos los demás campos
                    return saleDTO;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<List<SaleDTO>>(salesDTO, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<?> UpdateStatus(String saleId, String status) {

        Sale sale = this.serviceDBSale.findById(saleId).orElse(null);
        if (sale == null) {
            throw new EntidadNoExisteException("La venta con saleID " + saleId + " no existe en la Base de datos");
        }


        sale.setSaleStatus(status);
        if (sale != null) {
            try {
                sale = this.serviceDBSale.save(sale);
                SaleDTO saleDTO = SaleDTO.fromEntity(sale);
                return new ResponseEntity<>(saleDTO, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
    }

}
