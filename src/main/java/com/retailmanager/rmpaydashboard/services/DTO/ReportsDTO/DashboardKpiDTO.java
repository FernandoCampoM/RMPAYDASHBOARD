package com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO;

import java.util.List;

import com.retailmanager.rmpaydashboard.services.DTO.HourlySalesDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * ============================
 * DASHBOARD KPI DTO
 * ============================
 *
 * DTO principal del dashboard analítico del POS.
 *
 * Contiene métricas financieras, operacionales,
 * tendencias, comparativas históricas y semáforos KPI.
 */
public class DashboardKpiDTO {

    /**
     * Ventas acumuladas del año actual (Year To Date).
     *
     * Desde el 1 de enero hasta hoy.
     */
    private Double salesYTD;

    /**
     * Ventas acumuladas del año pasado
     * hasta el mismo día del año actual.
     */
    private Double salesLY;

    /**
     * Ganancia acumulada del año actual.
     *
     * Profit = ingresos - costos.
     */
    private Double profitYTD;

    /**
     * Ganancia acumulada del año pasado
     * hasta el mismo día del año actual.
     */
    private Double profitLY;

    /**
     * Variación porcentual de ganancia
     * comparando año actual vs año anterior.
     *
     * Profit Year over Year.
     */
    private Double profitYoY;

    /**
     * Ticket promedio del año actual.
     *
     * Ticket Promedio = ventas / transacciones.
     */
    private Double avgTicketYTD;

    /**
     * Ticket promedio del año pasado
     * hasta el mismo periodo actual.
     */
    private Double avgTicketLY;

    /**
     * Variación porcentual del ticket promedio
     * frente al año pasado.
     */
    private Double avgTicketYoY;

    /**
     * Ventas realizadas hoy.
     */
    private Double todaySales;

    /**
     * Ventas realizadas ayer.
     */
    private Double yesterdaySales;

    /**
     * Ventas realizadas antes de ayer.
     */
    private Double twoDaysAgoSales;

    /**
     * Ventas acumuladas de esta semana.
     *
     * Desde domingo hasta hoy.
     */
    private Double thisWeekSales;

    /**
     * Ventas acumuladas de la semana pasada
     * hasta el mismo día equivalente de esta semana.
     */
    private Double lastWeekSalesUntilToday;

    /**
     * Comparación porcentual:
     * Ayer vs Antes de ayer.
     *
     * Day over Day (DoD).
     */
    private Double salesYesterdayVsPreviousDay;

    /**
     * Semáforo KPI de ventas diarias.
     *
     * GREEN / YELLOW / RED.
     */
    private String salesYesterdayVsPreviousDayStatus;

    /**
     * Variación porcentual semanal.
     *
     * Week over Week (WoW).
     */
    private Double salesWoW;

    /**
     * Semáforo KPI de crecimiento semanal.
     */
    private String salesWoWStatus;

    /**
     * Crecimiento porcentual diario general.
     */
    private Double dailyGrowthPercent;

    /**
     * Estado KPI del crecimiento diario.
     */
    private String dailyGrowthPercentStatus;

    /**
     * Impuestos generados hoy.
     */
    private Double todayTaxes;

    /**
     * Impuestos generados ayer.
     */
    private Double yesterdayTaxes;

    /**
     * Impuestos generados antes de ayer.
     */
    private Double twoDaysAgoTaxes;

    /**
     * Impuestos acumulados esta semana.
     */
    private Double thisWeekTaxes;

    /**
     * Impuestos de la semana pasada
     * hasta el mismo día equivalente.
     */
    private Double lastWeekTaxesUntilToday;

    /**
     * Variación porcentual:
     * impuestos ayer vs antes de ayer.
     */
    private Double taxesYesterdayVsPreviousDay;

    /**
     * Semáforo KPI impuestos diarios.
     */
    private String taxesYesterdayVsPreviousDayStatus;

    /**
     * Variación porcentual semanal de impuestos.
     */
    private Double taxesWoW;

    /**
     * Semáforo KPI impuestos WoW.
     */
    private String taxesWoWStatus;

    /**
     * Crecimiento porcentual general
     * de impuestos.
     */
    private Double taxesGrowthPercent;

    /**
     * Semáforo KPI crecimiento impuestos.
     */
    private String taxesGrowthPercentStatus;

    /**
     * Impuestos acumulados del año actual.
     */
    private Double taxesYTD;

    /**
     * Ganancia generada hoy.
     */
    private Double todayProfit;

    /**
     * Ganancia generada ayer.
     */
    private Double yesterdayProfit;

    /**
     * Ganancia generada antes de ayer.
     */
    private Double twoDaysAgoProfit;

    /**
     * Ganancia acumulada de esta semana.
     */
    private Double thisWeekProfit;

    /**
     * Ganancia acumulada de la semana pasada
     * hasta el mismo día equivalente.
     */
    private Double lastWeekProfitUntilToday;

    /**
     * Variación porcentual:
     * ganancia ayer vs antes de ayer.
     */
    private Double profitYesterdayVsPreviousDay;

    /**
     * Semáforo KPI profit diario.
     */
    private String profitYesterdayVsPreviousDayStatus;

    /**
     * Variación porcentual semanal
     * de ganancias.
     */
    private Double profitWoW;

    /**
     * Semáforo KPI profit semanal.
     */
    private String profitWoWStatus;

    /**
     * Crecimiento porcentual general
     * de ganancias.
     */
    private Double profitGrowthPercent;

    /**
     * Semáforo KPI crecimiento profit.
     */
    private String profitGrowthPercentStatus;

    /**
     * Cantidad de transacciones realizadas hoy.
     */
    private Integer todayTransactions;

    /**
     * Variación porcentual de ventas
     * año actual vs año anterior.
     *
     * Sales Year over Year.
     */
    private Double salesYoY;

    /**
     * Margen de ganancia acumulado
     * del año actual.
     *
     * Margin = profit / sales.
     */
    private Double marginPercentYTD;

    /**
     * Margen de ganancia acumulado
     * del año pasado.
     */
    private Double marginPercentLY;

    /**
     * Variación del margen
     * frente al año anterior.
     */
    private Double marginPercentYoY;

    /**
     * Promedio de ventas por hora.
     */
    private Double avgSalesPerHour;

    /**
     * Promedio de transacciones por hora.
     */
    private Double avgTransactionsPerHour;

    /**
     * Margen porcentual generado hoy.
     */
    private Double todayMarginPercent;

    /**
     * Estado KPI general de ventas.
     */
    private String salesStatus;

    /**
     * Estado KPI general del margen.
     */
    private String marginStatus;

    /**
     * Estado KPI de costos laborales.
     */
    private String laborStatus;

    /**
     * Estado KPI ticket promedio.
     */
    private String avgTicketStatus;

    /**
     * Total de transacciones acumuladas
     * del año actual.
     */
    private Integer transactionsYTD;

    /**
     * Costo laboral acumulado del año actual.
     */
    private Double laborCostYTD;

    /**
     * Horas laborales acumuladas del año actual.
     */
    private Double laborHoursYTD;

    /**
     * Costo laboral acumulado del año pasado.
     */
    private Double laborCostLY;

    /**
     * Horas laborales acumuladas del año pasado.
     */
    private Double laborHoursLY;

    /**
     * Variación del costo por hora
     * frente al año anterior.
     */
    private Double costPerHourYoY;

    /**
     * Ventas agrupadas por hora.
     *
     * Utilizado para gráficas intradía.
     */
    private List<HourlySalesDTO> hourlySales;

    /**
     * Ventas agrupadas por día
     * del mes actual.
     */
    private List<DailySalesDTO> dailySales;

    /**
     * Ventas agrupadas por día
     * del periodo anterior comparativo.
     */
    private List<DailySalesDTO> previousDailySales;

    /**
     * Indicador general de eficiencia operacional.
     *
     * Normalmente:
     * ventas / costo laboral.
     */
    private Double efficiency;
}