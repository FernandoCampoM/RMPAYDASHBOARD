package com.retailmanager.rmpaydashboard.services.services.AutomatedEmailService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.retailmanager.rmpaydashboard.services.DTO.ShiftDTO;
import com.retailmanager.rmpaydashboard.services.DTO.SaleReportDTO;

@Component
public class ShiftClosingEmailTemplate {

    private static final Locale US_LOCALE = Locale.US;
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

    public String build(String businessName, Long businessId, ShiftDTO shift) {
        SaleReportDTO sale = shift.saleReport();

        StringBuilder html = new StringBuilder();

        html.append("<!doctype html>");
        html.append("<html><body style=\"margin:0;background:#edf4f8;font-family:Arial,Helvetica,sans-serif;color:#08142e;\">");
        html.append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#edf4f8;padding:36px 0;\">");
        html.append("<tr><td align=\"center\">");

        html.append("<table role=\"presentation\" width=\"760\" cellspacing=\"0\" cellpadding=\"0\" ")
                .append("style=\"width:760px;max-width:760px;background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 8px 24px rgba(0,0,0,.08);\">");

        html.append(buildHeader());
        html.append(buildShiftDetail(businessName, businessId, shift));
        html.append(
        buildSection(
                "🛒",
                "VENTAS",
                """
                """
                + row("Cash", sale == null ? null : sale.getSaleCash())
                + row("Credit", sale == null ? null : sale.getSaleCredit())
                + row("Debit", sale == null ? null : sale.getSaleDebit())
                + row("ATH Móvil", sale == null ? null : sale.getSaleATH())
        )
);

html.append(
        buildSection(
                "↩",
                "REEMBOLSOS",
                """
                """
                + row("Cash", sale == null ? null : sale.getRefundCash())
                + row("Credit", sale == null ? null : sale.getRefundCredit())
                + row("Debit", sale == null ? null : sale.getRefundDebit())
                + row("ATH Móvil", sale == null ? null : sale.getRefundATH())
        )
);

html.append(
        buildSection(
                "%",
                "IMPUESTOS",
                """
                """
                + row("Estatal", sale == null ? null : sale.getStateTax())
                + row("Municipal", sale == null ? null : sale.getCityTax())
                + row("Estatal reducido", sale == null ? null : sale.getReduceTax())
        )
);
        html.append(buildFinalBalance(shift.cuadreFinal()));
        html.append(buildFooter());

        html.append("</table>");
        html.append("</td></tr></table>");
        html.append("</body></html>");

        return html.toString();
    }

    private String buildHeader() {
        return "<tr><td style=\"background:#003f46;padding:34px 36px;color:#ffffff;\">"
                + "<table width=\"100%\"><tr>"
                + "<td style=\"font-size:34px;font-weight:800;\">REPORTE DE TURNO"
                + "<div style=\"font-size:16px;font-weight:400;margin-top:8px;\">Resumen de actividad del turno</div></td>"
                + "<td align=\"right\"><span style=\"background:#20b887;border:1px solid #c8f8e8;border-radius:28px;padding:12px 24px;font-size:18px;font-weight:800;\">CERRADO ✓</span></td>"
                + "</tr></table>"
                + "</td></tr>";
    }

    private String buildShiftDetail(String businessName, Long businessId, ShiftDTO shift) {
        return "<tr><td style=\"padding:34px 36px 24px 36px;border-bottom:1px solid #dfe7ef;\">"
                + "<table width=\"100%\"><tr>"
                + "<td width=\"48%\">"
                + "<div style=\"font-size:16px;color:#65728a;font-weight:800;\">DETALLE DEL TURNO</div>"
                + "<div style=\"font-size:34px;font-weight:900;margin-top:16px;\">" + escape(shift.userName()) + "</div>"
                + "<div style=\"font-size:13px;color:#65728a;margin-top:8px;\">Negocio: <b>" + escape(businessName) + "</b> | ID: <b>" + businessId + "</b></div>"
                + "</td>"
                + "<td style=\"border-left:1px solid #dfe7ef;padding-left:34px;font-size:15px;color:#20324d;\">"
                + detailRow("Inicio:", formatInstant(shift.startTime()))
                + detailRow("Finalización:", formatInstant(shift.endTime()))
                + detailRow("Terminal:", escape(shift.deviceId()))
                + "</td>"
                + "</tr></table>"
                + "</td></tr>";
    }

    private String buildSection(String icon, String title, String rows) {
        return "<tr><td style=\"padding:28px 36px 0 36px;\">"
                + "<table width=\"100%\" style=\"border-bottom:1px solid #dfe7ef;padding-bottom:26px;\">"
                + "<tr>"
                + "<td width=\"64\" valign=\"top\"><div style=\"width:46px;height:46px;border-radius:50%;background:#008467;color:#ffffff;text-align:center;line-height:46px;font-size:24px;font-weight:800;\">"
                + icon + "</div></td>"
                + "<td>"
                + "<div style=\"font-size:22px;color:#007153;font-weight:900;margin-bottom:18px;\">" + title + "</div>"
                + "<table width=\"100%\">" + rows + "</table>"
                + "</td>"
                + "</tr></table>"
                + "</td></tr>";
    }

    private String row(String label, BigDecimal value) {
        return "<tr>"
                + "<td style=\"font-size:16px;color:#4c596b;padding:8px 0;\">" + label + "</td>"
                + "<td style=\"border-bottom:1px dotted #b8c1cc;\"></td>"
                + "<td align=\"right\" style=\"font-size:18px;color:#007153;font-weight:900;padding:8px 0;width:140px;\">"
                + money(value) + "</td>"
                + "</tr>";
    }

    private String buildFinalBalance(BigDecimal value) {
        return "<tr><td style=\"padding:28px 36px;\">"
                + "<table width=\"100%\" style=\"border:1px solid #00a47b;background:#f2fffb;border-radius:12px;padding:20px;\">"
                + "<tr>"
                + "<td style=\"font-size:22px;font-weight:900;color:#003f46;\">CUADRE FINAL"
                + "<div style=\"font-size:14px;font-weight:400;color:#65728a;margin-top:6px;\">Resultado neto del turno</div></td>"
                + "<td align=\"right\" style=\"font-size:42px;font-weight:900;color:#007153;\">" + money(value) + "</td>"
                + "</tr>"
                + "</table>"
                + "</td></tr>";
    }

    private String buildFooter() {
        return "<tr><td style=\"background:#f7fafc;padding:24px 36px;font-size:14px;color:#20324d;\">"
                + "<b>Gracias por su dedicación</b><br>"
                + "Este resumen ha sido generado automáticamente.<br>"
                + "<b>Retail Manager POS</b>"
                + "</td></tr>";
    }

    private String detailRow(String label, String value) {
        return "<div style=\"margin-bottom:16px;\"><b style=\"color:#65728a;display:inline-block;width:110px;\">" 
                + label + "</b> " + value + "</div>";
    }

    private String formatInstant(Instant value) {
        if (value == null) {
            return "-";
        }
        return DATE_TIME_FORMAT.format(value.atZone(ZoneId.systemDefault()));
    }

    private String money(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(US_LOCALE).format(value == null ? BigDecimal.ZERO : value);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}