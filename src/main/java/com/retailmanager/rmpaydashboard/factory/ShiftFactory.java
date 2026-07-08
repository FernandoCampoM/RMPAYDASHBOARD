package com.retailmanager.rmpaydashboard.factory;

import com.retailmanager.rmpaydashboard.models.SaleReport;
import com.retailmanager.rmpaydashboard.models.SaleReportProjection;
import com.retailmanager.rmpaydashboard.models.Shift;
import com.retailmanager.rmpaydashboard.models.enums.SyncStatus;
import com.retailmanager.rmpaydashboard.services.DTO.SaleReportDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ShiftDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShiftFactory {
    public ShiftDTO toDTO(Shift shift) {
        return Optional.ofNullable(shift)
                .map(entity -> new ShiftDTO(
                        entity.getShiftId(),
                        entity.getUserName(),
                        entity.getStartTime(),
                        entity.getEndTime(),
                        entity.getBalanceInicial(),
                        entity.getBalanceFinal(),
                        entity.getCuadreFinal(),
                        entity.isOpenShifBalance(),
                        toSaleReportDTO(entity.getSaleReport()),
                        entity.getUserBusiness() != null ? entity.getUserBusiness().getUserBusinessId() : null,
                        entity.getTerminal() != null ? entity.getTerminal().getSerial() : null,
                        entity.getLastSyncAt(),
                        entity.getSyncStatus().name()
                )).orElse(null);
    }

    public Shift toEntity(ShiftDTO shiftDTO) {
        return Optional.ofNullable(shiftDTO)
                .map(entity -> new Shift(
                        entity.shiftId(),
                        entity.userName(),
                        entity.startTime(),
                        entity.endTime(),
                        entity.balanceInicial(),
                        entity.balanceFinal(),
                        entity.cuadreFinal(),
                        entity.openShifBalance(),
                        SyncStatus.valueOf(entity.syncStatus()),
                        entity.lastSyncAt(),
                        null,
                        null,
                        null
                )).orElse(null);
    }

    public SaleReportDTO toSaleReportDTO(SaleReport saleReport) {
        return Optional.ofNullable(saleReport)
                .map(entity -> new SaleReportDTO(
                        entity.getSaleCash(),
                        entity.getSaleCredit(),
                        entity.getSaleDebit(),
                        entity.getSaleATH(),
                        entity.getRefundCash(),
                        entity.getRefundCredit(),
                        entity.getRefundDebit(),
                        entity.getRefundATH(),
                        entity.getStateTax(),
                        entity.getCityTax(),
                        entity.getReduceTax()
                )).orElse(null);
    }

    public SaleReportDTO toReportProjectionDTO(SaleReportProjection saleReport) {
        return Optional.ofNullable(saleReport)
                .map(entity -> new SaleReportDTO(
                        entity.getSaleCash(),
                        entity.getSaleCredit(),
                        entity.getSaleDebit(),
                        entity.getSaleATH(),
                        entity.getRefundCash(),
                        entity.getRefundCredit(),
                        entity.getRefundDebit(),
                        entity.getRefundATH(),
                        entity.getStateTax(),
                        entity.getCityTax(),
                        entity.getReduceTax()
                )).orElse(null);
    }

    public SaleReport toSaleReportEntity(SaleReportDTO saleReport, Shift shift) {
        return Optional.ofNullable(saleReport)
                .map(entity -> new SaleReport(
                        shift.getSaleReport()!=null ? shift.getSaleReport().getId() : null,
                        entity.getSaleCash(),
                        entity.getSaleCredit(),
                        entity.getSaleDebit(),
                        entity.getSaleATH(),
                        entity.getRefundCash(),
                        entity.getRefundCredit(),
                        entity.getRefundDebit(),
                        entity.getRefundATH(),
                        entity.getStateTax(),
                        entity.getCityTax(),
                        entity.getReduceTax(),
                        shift
                )).orElse(null);
    }
}
