package com.retailmanager.rmpaydashboard.services.services.AutomatedEmailService;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.BestSellingItemProjection;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.CategoryNetSalesProjection;
import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.DailySummaryDTO;

// ── NEW: daily summary email template ──────────────────
// Purpose : Builds the HTML body for DailySummary automated emails.
// Depends on : DailySummaryDTO, Business.businessId, Business.name.
// Does NOT modify : EmailService templates, report endpoints, report DTO fields.
@Component
public class DailySummaryEmailTemplate {

    private static final Locale US_LOCALE = Locale.US;
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");

    public String build(Business business, LocalDate reportDate, DailySummaryDTO summary) {
        BigDecimal totalTaxes = safe(summary.getStateTax())
                .add(safe(summary.getMunicipalTax()))
                .add(safe(summary.getEstimatedRedTax()));

        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>");
        html.append("<html><body style=\"margin:0;background:#edf4f8;font-family:Arial,Helvetica,sans-serif;color:#08142e;\">");
        html.append("<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#edf4f8;padding:36px 0;\">");
        html.append("<tr><td align=\"center\">");
        html.append("<table role=\"presentation\" width=\"720\" cellspacing=\"0\" cellpadding=\"0\" style=\"width:720px;max-width:720px;background:#ffffff;border-radius:0 0 18px 18px;overflow:hidden;box-shadow:10px 10px 0 #dfeaf1;\">");
        html.append(buildHeader());
        html.append(buildHero(business, reportDate));
        html.append(buildMetrics(summary, totalTaxes));
        html.append(buildTaxes(summary, totalTaxes));
        html.append(buildCategoryChart(summary.getEarningsByCategory()));
        html.append(buildProducts(summary.getBestSellingProducts()));
        html.append(buildFooter());
        html.append("</table>");
        html.append("</td></tr></table>");
        html.append("</body></html>");
        return html.toString();
    }

    private String buildHeader() {
        return "<tr><td style=\"background:#f8fafc;padding:20px 30px;border-bottom:1px solid #edf1f5;\">"
                + "<table role=\"presentation\" width=\"100%\"><tr>"
                + "<td style=\"font-size:24px;font-weight:700;color:#687483;\"><span style=\"color:#00a991;\">R</span>MPay</td>"
                + "<td align=\"right\" style=\"font-size:14px;color:#65728a;\">Reporte diario</td>"
                + "</tr></table></td></tr>";
    }

    private String buildHero(Business business, LocalDate reportDate) {
        return "<tr><td style=\"padding:34px 30px 18px 30px;\">"
                + "<table role=\"presentation\" width=\"100%\"><tr>"
                + "<td width=\"70\" valign=\"top\"><div style=\"width:58px;height:58px;background:#d9f7f1;border-radius:14px;text-align:center;line-height:58px;color:#00a991;font-size:28px;font-weight:700;\">↗</div></td>"
                + "<td valign=\"top\">"
                + "<div style=\"font-size:32px;line-height:38px;font-weight:800;color:#08142e;\">Resumen diario</div>"
                + "<div style=\"font-size:15px;line-height:22px;color:#65728a;\">Ventas, beneficios e impuestos del "
                + REPORT_DATE_FORMAT.format(reportDate) + "</div>"
                + "<div style=\"font-size:13px;line-height:20px;color:#65728a;margin-top:6px;\">Negocio: <strong style=\"color:#08142e;\">"
                + escape(business.getName()) + "</strong> | ID: <strong style=\"color:#08142e;\">"
                + business.getBusinessId() + "</strong></div>"
                + "</td>"
                + "</tr></table>"
                + "</td></tr>";
    }

    private String buildMetrics(DailySummaryDTO summary, BigDecimal totalTaxes) {
        return "<tr><td style=\"padding:8px 30px 0 30px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">"
                + "<tr>"
                + metricCard("VENTAS", money(summary.getTotalSales()), true)
                + "<td width=\"22\"></td>"
                + metricCard("BENEFICIO", money(summary.getBenefit()), false)
                + "</tr>"
                + "<tr><td height=\"18\"></td></tr>"
                + "<tr>"
                + metricCard("REEMBOLSOS", money(summary.getTotalRefunds()), true)
                + "<td width=\"22\"></td>"
                + metricCard("PROPINAS", money(summary.getTotalTips()), false)
                + "</tr>"
                + "</table>"
                + "</td></tr>";
    }

    private String metricCard(String label, String value, boolean leftColumn) {
        String width = leftColumn ? "width:318px;" : "width:318px;";
        return "<td style=\"" + width + "border:1px solid #d7e3ee;border-left:5px solid #00a991;border-radius:8px;padding:20px 18px;height:86px;\">"
                + "<div style=\"font-size:13px;letter-spacing:.4px;color:#65728a;font-weight:700;\">" + label + "</div>"
                + "<div style=\"font-size:30px;line-height:38px;margin-top:8px;color:#08142e;font-weight:800;\">" + value + "</div>"
                + "</td>";
    }

    private String buildTaxes(DailySummaryDTO summary, BigDecimal totalTaxes) {
        return "<tr><td style=\"padding:22px 30px 0 30px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"border:1px solid #d7e3ee;border-radius:8px;padding:22px 24px;\">"
                + "<tr>"
                + "<td width=\"38%\"><div style=\"font-size:18px;color:#65728a;font-weight:700;\">Impuestos totales</div>"
                + "<div style=\"font-size:32px;line-height:40px;color:#08142e;font-weight:800;margin-top:6px;\">" + money(totalTaxes) + "</div></td>"
                + taxCell("Estatal", money(summary.getStateTax()))
                + taxCell("Municipal", money(summary.getMunicipalTax()))
                + taxCell("Estatal reducido", money(summary.getEstimatedRedTax()))
                + "</tr>"
                + "</table>"
                + "</td></tr>";
    }

    private String taxCell(String label, String value) {
        return "<td align=\"left\" style=\"font-size:13px;color:#65728a;\">"
                + "<div>" + label + "</div>"
                + "<div style=\"font-size:16px;color:#08142e;font-weight:800;margin-top:10px;\">" + value + "</div>"
                + "</td>";
    }

    private String buildCategoryChart(List<CategoryNetSalesProjection> categories) {
        StringBuilder rows = new StringBuilder();
        List<CategoryNetSalesProjection> data = categories == null ? List.of() : categories.stream()
                .sorted(Comparator.comparing(CategoryNetSalesProjection::getTotalAmount, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();
        double max = data.stream()
                .map(CategoryNetSalesProjection::getTotalAmount)
                .filter(value -> value != null && value > 0)
                .max(Double::compareTo)
                .orElse(1D);

        if (data.isEmpty()) {
            rows.append("<div style=\"padding:24px 0;color:#65728a;font-size:14px;\">No existen ganancias por categoria.</div>");
        } else {
            for (CategoryNetSalesProjection category : data) {
                double amount = category.getTotalAmount() == null ? 0D : category.getTotalAmount();
                int width = Math.max(8, (int) Math.round((amount / max) * 100));
                rows.append("<tr>")
                        .append("<td width=\"160\" style=\"font-size:13px;color:#20324d;padding:8px 10px 8px 0;\">")
                        .append(escape(category.getCategory()))
                        .append("</td>")
                        .append("<td style=\"padding:8px 0;\"><div style=\"background:#e8f2fb;border-radius:6px;height:26px;\">")
                        .append("<div style=\"width:").append(width).append("%;height:26px;background:#3499e5;border-radius:6px;text-align:right;color:#ffffff;font-weight:700;font-size:13px;line-height:26px;padding-right:8px;box-sizing:border-box;\">")
                        .append(money(BigDecimal.valueOf(amount)))
                        .append("</div></div></td>")
                        .append("</tr>");
            }
        }

        return "<tr><td style=\"padding:24px 30px 0 30px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"border:1px solid #d7e3ee;border-radius:8px;padding:22px 24px;\">"
                + "<tr><td style=\"font-size:20px;font-weight:800;color:#08142e;\">Ganancias por categoria</td>"
                + "<td align=\"right\" style=\"font-size:13px;color:#65728a;\">Distribucion del dia</td></tr>"
                + "<tr><td colspan=\"2\" style=\"border-top:1px solid #edf1f5;padding-top:18px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">" + rows + "</table>"
                + "</td></tr>"
                + "</table>"
                + "</td></tr>";
    }

    private String buildProducts(List<BestSellingItemProjection> products) {
        StringBuilder rows = new StringBuilder();
        List<BestSellingItemProjection> data = products == null ? List.of() : products.stream().limit(5).toList();
        if (data.isEmpty()) {
            rows.append("<tr><td colspan=\"4\" style=\"padding:24px 12px;color:#65728a;font-size:14px;\">No existen productos mas vendidos.</td></tr>");
        } else {
            for (BestSellingItemProjection product : data) {
                String benefitColor = product.getBenefit() != null && product.getBenefit() < 0 ? "#d71920" : "#20324d";
                rows.append("<tr>")
                        .append("<td style=\"padding:14px 12px;font-size:13px;color:#20324d;\">").append(escape(product.getName())).append("</td>")
                        .append("<td align=\"center\" style=\"padding:14px 12px;font-size:13px;color:#20324d;\">").append(product.getQuantity() == null ? 0 : product.getQuantity()).append("</td>")
                        .append("<td align=\"right\" style=\"padding:14px 12px;font-size:13px;color:#20324d;\">").append(money(product.getTotalAmount())).append("</td>")
                        .append("<td align=\"right\" style=\"padding:14px 12px;font-size:13px;color:").append(benefitColor).append(";\">").append(money(product.getBenefit())).append("</td>")
                        .append("</tr>");
            }
        }

        return "<tr><td style=\"padding:24px 30px 0 30px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"border:1px solid #d7e3ee;border-radius:8px;padding:22px 24px;\">"
                + "<tr><td style=\"font-size:20px;font-weight:800;color:#08142e;\">Productos mas vendidos</td>"
                + "<td align=\"right\" style=\"font-size:13px;color:#65728a;\">Top 5 del reporte</td></tr>"
                + "<tr><td colspan=\"2\" style=\"border-top:1px solid #edf1f5;padding-top:18px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">"
                + "<tr style=\"background:#f7f7f6;\">"
                + "<th align=\"left\" style=\"padding:14px 12px;border-radius:8px 0 0 8px;font-size:13px;color:#08142e;\">Nombre</th>"
                + "<th align=\"center\" style=\"padding:14px 12px;font-size:13px;color:#08142e;\">Vendidos</th>"
                + "<th align=\"right\" style=\"padding:14px 12px;font-size:13px;color:#08142e;\">Ventas</th>"
                + "<th align=\"right\" style=\"padding:14px 12px;border-radius:0 8px 8px 0;font-size:13px;color:#08142e;\">Beneficio</th>"
                + "</tr>"
                + rows
                + "</table>"
                + "</td></tr>"
                + "</table>"
                + "</td></tr>";
    }

    private String buildFooter() {
        return "<tr><td style=\"padding:28px 30px 32px 30px;\">"
                + "<table role=\"presentation\" width=\"100%\" style=\"border-top:1px solid #dfe7ef;padding-top:22px;\">"
                + "<tr><td style=\"font-size:13px;color:#65728a;\">RMPay | Reporte generado automaticamente</td>"
                + "<td align=\"right\" style=\"font-size:13px;color:#00a991;font-weight:700;\">Resumen diario</td></tr>"
                + "</table>"
                + "</td></tr>";
    }

    private String money(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(US_LOCALE).format(safe(value));
    }

    private String money(Double value) {
        return money(BigDecimal.valueOf(value == null ? 0D : value));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
