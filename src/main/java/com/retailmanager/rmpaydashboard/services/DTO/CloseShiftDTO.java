package com.retailmanager.rmpaydashboard.services.DTO;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloseShiftDTO {

    //@Positive(message = "{shift.shiftId.positive}") // Si es para un ID en una operación de actualización
    @NotBlank(message = "{shift.userName.notBlank}")
    private String shiftId; // Para DTOs de respuesta o para actualizaciones donde el ID es necesario

    @Digits(integer = 19, fraction = 2, message = "{shift.balanceFinal.digits}")
    private BigDecimal cuadreFinal;

    private Instant endTime;

    @Digits(integer = 19, fraction = 2, message = "{shift.balanceFinal.digits}")
    private BigDecimal balanceFinal;
}
