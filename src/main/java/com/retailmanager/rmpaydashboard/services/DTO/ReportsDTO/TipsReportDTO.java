package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TipsReportDTO {
    private BigDecimal totalSales=BigDecimal.ZERO;
    private BigDecimal subTotalSales = BigDecimal.ZERO;
    private BigDecimal totalTips = BigDecimal.ZERO;
    List<UserTipsReportProjection> userTips= new ArrayList<>();
}
