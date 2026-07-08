package com.retailmanager.rmpaydashboard.services.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.With;

import java.math.BigDecimal;
import java.time.Instant;

public record ShiftDTO(
        @NotBlank(message = "{shift.shiftId.notBlank}")
        String shiftId,

        //@NotBlank(message = "{shift.userName.notBlank}")
        //@Size(min = 2, max = 100, message = "{shift.userName.size}")
        String userName,

        @NotNull(message = "{shift.startTime.notNull}")
        @PastOrPresent(message = "{shift.startTime.pastOrPresent}")
        Instant startTime,

        Instant endTime,

        @NotNull(message = "{shift.balanceInicial.notNull}")
        @DecimalMin(value = "0.0", message = "{shift.balanceInicial.decimalMin}")
        @Digits(integer = 19, fraction = 2, message = "{shift.balanceInicial.digits}")
        BigDecimal balanceInicial,

        @Digits(integer = 19, fraction = 2, message = "{shift.balanceFinal.digits}")
        BigDecimal balanceFinal,

        @Digits(integer = 19, fraction = 2, message = "{shift.cuadreFinal.digits}")
        BigDecimal cuadreFinal,

        // Si quieres permitir null y que sea wrapper: Boolean
        @NotNull(message = "{shift.openShifBalance.notNull}")
        Boolean openShifBalance,

        @Valid
        @With
        SaleReportDTO saleReport,

        @NotNull(message = "{shift.userId.notNull}")
        @Positive(message = "{shift.userId.positive}")
        Long userId,

        @NotBlank(message = "{shift.deviceId.notBlank}")
        String deviceId,
        Instant lastSyncAt,
        String syncStatus
) {
}