package com.retailmanager.rmpaydashboard.controller;

import java.time.LocalDate;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.services.DTO.ConfirmPaymentDTO;
import com.retailmanager.rmpaydashboard.services.DTO.PaymentDataDTO;
import com.retailmanager.rmpaydashboard.services.DTO.doPaymentDTO;
import com.retailmanager.rmpaydashboard.services.services.InvoiceServices.IInvoiceServices;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api")
@Validated
public class InvoiceController {
    @Autowired
    private IInvoiceServices invoiceService;

    @GetMapping("/invoices/historyByBusiness/{businessId}")
    public ResponseEntity<?> getMethodName(@PathVariable Long businessId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return this.invoiceService.getPaymentHistoryByBusiness(businessId, startDate, endDate);
    }
    
    @GetMapping("/invoices/history")
    public ResponseEntity<?> getMethodName(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate,@RequestParam(required = false) String filter) {
        return this.invoiceService.getPaymentHistor(startDate, endDate, filter);
    }
    @PostMapping("/invoices/token")
    public ResponseEntity<?> createToken(@Valid @RequestBody PaymentDataDTO prmPaymentInfo){
        return this.invoiceService.createToken(prmPaymentInfo);
    }
    @GetMapping("/invoices/token/exist")
    public ResponseEntity<?> exist(@Valid @RequestParam("businessid") String prmBusinessId){
       try {
            Long businessId = Long.parseLong(prmBusinessId);
            return this.invoiceService.existToken(businessId);
        } catch (Exception e) {
            HashMap<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    @GetMapping("/invoices/token")
    public ResponseEntity<?> getToken(@Valid @RequestParam("businessid") String prmBusinessId){
       try {
            Long businessId = Long.parseLong(prmBusinessId);
            return this.invoiceService.getToken(businessId);
        } catch (Exception e) {
            HashMap<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    @DeleteMapping("/invoices/token")
    public ResponseEntity<?> deleteToken(@Valid @RequestParam("businessid") String prmBusinessId){
       try {
            Long businessId = Long.parseLong(prmBusinessId);
            return this.invoiceService.deleteToken(businessId);
        } catch (Exception e) {
            HashMap<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    @PostMapping("/invoices/doPayment")
    public ResponseEntity<?> doPayment(@Valid @RequestBody doPaymentDTO prmPaymentInfo){
        return this.invoiceService.doPayment(prmPaymentInfo);
    }
    /**
     * Check the status of an ATHM transaction by invoice number
     * @param invoiceNumber the invoice number to check
     * @return a ResponseEntity containing the status of the transaction
     */
    @GetMapping("/invoices/ATHM/checkTransaction/{invoiceNumber}")
    public ResponseEntity<?> checkTransaction(@Valid @PathVariable @PositiveOrZero(message = "invoiceNumber.positiveOrZero") Long invoiceNumber){
        return this.invoiceService.checkStatusATHM(invoiceNumber);
    }
    /**
     * Confirm an ATHM transaction by invoice number
     * @param invoiceNumber the invoice number to confirm
     * @return a ResponseEntity containing the updated invoice information or an
     *         error message
     */
    @PostMapping("/invoices/ATHM/confirmTransaction/{invoiceNumber}")
    public ResponseEntity<?> confirmTransaction(@Valid @PathVariable @PositiveOrZero(message = "invoiceNumber.positiveOrZero") Long invoiceNumber){
        return this.invoiceService.confirmTransactionATHM(invoiceNumber);
    }
/**
 * Cancel an ATHM transaction by invoice number.
 * 
 * @param invoiceNumber the invoice number to cancel
 * @return a ResponseEntity containing the updated invoice information or an
 *         error message
 */

    @PostMapping("/invoices/ATHM/cancelTransaction/{invoiceNumber}")
    public ResponseEntity<?> cancelTransaction(@Valid @PathVariable @PositiveOrZero(message = "invoiceNumber.positiveOrZero") Long invoiceNumber){
        return this.invoiceService.cancelTransactionATHM(invoiceNumber);
    }
    /**
     * Confirms or rejects a payment based on the given invoice number and payment
     * information.
     *
     * @param invoiceNumber the invoice number to confirm or reject
     * @param prmPaymentInfo the payment information containing the confirmation
     *                       status and observation
     * @return a ResponseEntity containing the updated invoice information or an
     *         error message
     * @throws EntidadNoExisteException if the invoice with the given invoice number
     *                                  does not exist in the database
     */
    @PostMapping("/invoices/{invoiceNumber}/confirmOrReject")
    public ResponseEntity<?> confirmOrReject(@PathVariable Long invoiceNumber,@Valid @RequestBody ConfirmPaymentDTO prmPaymentInfo){
        return this.invoiceService.confirmOrRejectPaymnt(invoiceNumber,prmPaymentInfo);
    }
    @PostMapping("/invoices/test")
    public ResponseEntity<?> test(@Valid @RequestBody doPaymentDTO prmPaymentInfo){
        return this.invoiceService.testPayment(prmPaymentInfo);
    }
}
