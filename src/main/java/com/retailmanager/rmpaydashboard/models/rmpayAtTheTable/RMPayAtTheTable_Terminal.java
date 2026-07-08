package com.retailmanager.rmpaydashboard.models.rmpayAtTheTable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.RMPayAtTheTable_TerminalDTO;
@Entity
@Data
@NoArgsConstructor
public class RMPayAtTheTable_Terminal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long terminalId;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private Boolean active;

    @Column
    private LocalDate lastTransmissionDate;

    @Column(nullable = false)
    private LocalDate registrationDate;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private RMPayAtTheTable_User user;

    public RMPayAtTheTable_TerminalDTO toDTO(){
        RMPayAtTheTable_TerminalDTO terminalDTO = new RMPayAtTheTable_TerminalDTO();
        terminalDTO.setTerminalId(this.terminalId);
        terminalDTO.setSerialNumber(this.serialNumber);
        terminalDTO.setActive(this.active);
        terminalDTO.setLastTransmissionDate(this.lastTransmissionDate);
        terminalDTO.setRegistrationDate(this.registrationDate);
        terminalDTO.setUserId(this.user.getUserId());
        return terminalDTO;

    }
}
