package com.retailmanager.rmpaydashboard.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.retailmanager.rmpaydashboard.services.DTO.TerminalsDoPaymentDTO;
import com.retailmanager.rmpaydashboard.services.services.EmailService.EmailBodyData;
import com.retailmanager.rmpaydashboard.services.services.EmailService.IEmailService;
import com.retailmanager.rmpaydashboard.services.services.FileServices.IFileService;

@RestController
@RequestMapping("/api")
@Validated
public class FileController {
    @Autowired
    private IFileService fileService;

    @Autowired
    private IEmailService emailService;
    /**
     * Guarda un archivo en el sistema.
     *
     * @param  file	el archivo a guardar
     * @return     	el estado de la operación de guardado
     */
    @PostMapping("/file")
    public ResponseEntity<?> guardarArchivo(@RequestParam("file") MultipartFile file) {
        return fileService.save(file);
    }
    /**
     * Saves an image file to the system.
     *
     * @param  file  the image file to be saved
     * @return       the response entity representing the status of the save operation
     */
    @PostMapping("/file/image")
    public ResponseEntity<?> guardarImagen(@RequestParam("file") MultipartFile file) {
        return fileService.saveImage(file);
    }
    /**
     * Downloads an image file with the given fileId.
     *
     * @param  fileId  the id of the image file to be downloaded
     * @return           the response entity representing the downloaded image file
     */
    @GetMapping("/file/image/{fileId}")
    public ResponseEntity<?> downloadImage(@PathVariable Long fileId) {
        
        return fileService.downloadImage(fileId);
    }
    /**
     * Deletes an image file with the given fileId.
     *
     * @param  fileId   the ID of the image file to be deleted
     * @return          the response entity representing the result of the delete operation
     */
    @DeleteMapping("/file/image/{fileId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long fileId) {
        
        return fileService.deleteImage(fileId);
    }

    @GetMapping("/test/email")
    public ResponseEntity<?> testEmail(@RequestParam String email) {
        EmailBodyData emailData = new EmailBodyData();
        emailData.setEmail(email);
        emailData.setBusinessName("Business Test Email");
        emailData.setAdditionalTerminals(2);
        emailData.setPaymethod("Paymethod Test Email");
        emailData.setAdditionalTerminalsValue(2);
        emailData.setAmount(1000000);
        emailData.setAutomaticPayments(true);
        emailData.setInvoiceNumber(123456L);
        emailData.setBuyTerminal(false);
        emailData.setCardType("AMERICAN EXPRES");
        emailData.setCreditcarnumber("4111111111111111");
        emailData.setDiscount(500000);
        emailData.setErrorMessage("Test Error Mesage");
        emailData.setExpDateMonth("12");
        emailData.setExpDateYear("30");
        emailData.setNameoncard("Test Name On Card");
        emailData.setMerchantId("57917817CMJ");
        emailData.setPhone("3156837054");
        emailData.setReferenceNumber("57917817CMJ-REF");
        emailData.setSecuritycode("123");
        emailData.setServiceDescription("SERVICIO DE PRUEBA EMAIL");
        emailData.setServiceValue("50000");
        emailData.setStateTax(1000);
        emailData.setSubTotal(999000);
        emailData.setSubject("TEST EMAIL SERVICE");
        List<TerminalsDoPaymentDTO> terminals = new ArrayList<>();
        TerminalsDoPaymentDTO terminal1 = new TerminalsDoPaymentDTO();
        terminal1.setAmount(50000.0);
        terminal1.setIdService(1L);
        terminal1.setPrincipal(true);
        terminal1.setServiceDescription("Service 1 dia test");
        terminal1.setTerminalId("TER01");
        
        terminals.add(terminal1);

        TerminalsDoPaymentDTO terminal2 = new TerminalsDoPaymentDTO();
        terminal2.setAmount(50000.0);
        terminal2.setIdService(2L);
        terminal2.setPrincipal(true);
        terminal2.setServiceDescription("Service 1 dia test second");
        terminal2.setTerminalId("TER02");
        terminals.add(terminal2);
        emailData.setTerminalsDoPayment(terminals);
        System.out.println("Sending test email to: " + email);
        this.emailService.testEmailService(emailData);
        return new ResponseEntity<>("Email sent successfully", HttpStatus.OK);
    }
    
}
