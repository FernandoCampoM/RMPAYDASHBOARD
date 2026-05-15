package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesDTO {
    Integer dayOfMonth;

    Integer transactions;

    Double sales;
}
