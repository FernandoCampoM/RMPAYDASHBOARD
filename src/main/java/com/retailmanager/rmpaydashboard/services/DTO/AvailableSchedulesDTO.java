package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSchedulesDTO {
    private Long asId;
    private String title;
    private String duration; // Ejemplo: "01:30"
    private Long employeeId; // Relación con UsersBusiness
    private String color;
    
}
