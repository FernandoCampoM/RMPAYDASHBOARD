package com.retailmanager.rmpaydashboard.services.services.AutomatedEmailService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.retailmanager.rmpaydashboard.services.DTO.ReportsDTO.BatchCloseReportDTO;


@Component
public class BatchCloseReportEmailTemplate {

    private static final Locale US_LOCALE = Locale.US;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.US);

    public String build(BatchCloseReportDTO report) {
        StringBuilder html = new StringBuilder();

        html.append("<!doctype html>");
        html.append("<html><body style=\"margin:0;background:#F8FAFC;font-family:Arial,Helvetica,sans-serif;color:#1E293B;\">");
        html.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#F8FAFC;padding:30px 0;\">");
        html.append("<tr><td align=\"center\">");

        html.append("<table width=\"760\" cellspacing=\"0\" cellpadding=\"0\" style=\"width:760px;max-width:760px;background:#ffffff;border-radius:18px;overflow:hidden;box-shadow:0 8px 28px rgba(15,23,42,.10);\">");

        html.append(buildHeader(report));
        html.append(buildPaymentCards(report));
        html.append(buildTaxes(report));
        html.append(buildDetails(report));
        html.append(buildFooter());

        html.append("</table>");
        html.append("</td></tr></table>");
        html.append("</body></html>");

        return html.toString();
    }

    private String buildHeader(BatchCloseReportDTO report) {

    return """
        <tr>
            <td style="background:linear-gradient(135deg,#17A673,#047857);padding:38px 36px;color:#ffffff;">
                <table width="100%">
                    <tr>

                        <td width="76" valign="top">
        """
        + circleIcon("📋", "rgba(255,255,255,.15)", "#ffffff", "#84CC16")
        + """
                        </td>

                        <td valign="top">
                            <div style="font-size:34px;font-weight:900;line-height:40px;">
                                Cierre de batch
                            </div>

                            <div style="font-size:18px;margin-top:10px;opacity:.95;">
        """
        + formatDate(report.reportDate())
        + """
                                &nbsp;•&nbsp;
        """
        + formatTime(report.reportDate())
        + """
                            </div>

                            <div style="font-size:17px;margin-top:34px;opacity:.95;">
                                TOTAL DEL BATCH
                            </div>

                            <div style="font-size:52px;font-weight:900;line-height:60px;margin-top:6px;">
        """
        + money(report.summary() == null ? null : report.summary().totalAmount())
        + """
                            </div>

                            <div style="font-size:18px;margin-top:8px;">
        """
        + safeTransactions(report)
        + """
                                Transacciones
                            </div>
                        </td>

                        <td align="right" valign="middle">
                            <div style="width:150px;height:150px;border-radius:50%;background:rgba(34,197,94,.13);border:1px solid rgba(132,204,22,.35);text-align:center;line-height:150px;">
                                <span style="display:inline-block;width:92px;height:92px;border-radius:50%;border:3px solid rgba(132,204,22,.85);line-height:92px;font-size:54px;font-weight:900;color:#A3E635;">
                                    $
                                </span>
                            </div>
                        </td>

                    </tr>
                </table>
            </td>
        </tr>
        """;
}

    private String buildPaymentCards(BatchCloseReportDTO report) {

    return """
        <tr>
            <td style="padding:28px 28px 0 28px;">
                <table width="100%" cellspacing="0" cellpadding="0">
                    <tr>
                        <td width="50%" style="padding-right:11px;">
        """
        + paymentCard(
                "📈",
                "VENTAS",
                "#17A673",
                "#22C55E",
                report.sales() == null ? null : report.sales().credit(),
                report.sales() == null ? null : report.sales().debit(),
                report.sales() == null ? null : report.sales().total(),
                "Total Ventas"
        )
        + """
                        </td>

                        <td width="50%" style="padding-left:11px;">
        """
        + paymentCard(
                "↩",
                "DEVOLUCIONES",
                "#7C3AED",
                "#7C3AED",
                report.refunds() == null ? null : report.refunds().credit(),
                report.refunds() == null ? null : report.refunds().debit(),
                report.refunds() == null ? null : report.refunds().total(),
                "Total Devoluciones"
        )
        + """
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        """;
}

    private String paymentCard(
        String icon,
        String title,
        String titleColor,
        String totalColor,
        BigDecimal credit,
        BigDecimal debit,
        BigDecimal total,
        String totalLabel
) {

    return """
        <table width="100%" cellspacing="0" cellpadding="0" style="border:1px solid #E5E7EB;border-radius:14px;padding:22px;background:#ffffff;">
            <tr>
                <td colspan="3">
                    <table>
                        <tr>
                            <td>
        """
        + circleIcon(icon, "#EEFDF6", titleColor, "#BBF7D0")
        + """
                            </td>

                            <td style="font-size:21px;font-weight:900;color:
        """
        + titleColor
        + """
        ;padding-left:16px;">
        """
        + title
        + """
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        """
        + paymentRow("💳", "Crédito", credit, "#2563EB")
        + separator()
        + paymentRow("💳", "Débito", debit, "#16A34A")
        + separator()
        + """
            <tr>
                <td colspan="2" style="font-size:17px;font-weight:900;color:#1E293B;padding-top:18px;">
        """
        + totalLabel
        + """
                </td>

                <td align="right" style="font-size:20px;font-weight:900;color:
        """
        + totalColor
        + """
        ;padding-top:18px;">
        """
        + money(total)
        + """
                </td>
            </tr>
        </table>
        """;
}

    private String buildTaxes(BatchCloseReportDTO report) {
    StringBuilder taxItems = new StringBuilder();

    if (report.taxes() != null && report.taxes().taxes() != null && !report.taxes().taxes().isEmpty()) {
        for (BatchCloseReportDTO.TaxItem tax : report.taxes().taxes()) {
            taxItems.append("""
                <td align="center" style="font-size:15px;color:#334155;padding:0 18px;border-right:1px solid #E5E7EB;">
                """)
                .append(escape(tax.name()))
                .append("""
                    <div style="font-size:22px;font-weight:900;color:#7C3AED;margin-top:10px;">
                    """)
                .append(money(tax.amount()))
                .append("""
                    </div>
                </td>
                """);
        }
    }

    return """
        <tr>
            <td style="padding:22px 28px 0 28px;">
                <table width="100%" cellspacing="0" cellpadding="0" style="border:1px solid #E5E7EB;border-radius:14px;padding:22px;background:#ffffff;">
                    <tr>
                        <td>
                            <table>
                                <tr>
                                    <td>
        """
        + circleIcon("🏛", "#F3E8FF", "#7C3AED", "#DDD6FE")
        + """
                                    </td>

                                    <td style="font-size:22px;font-weight:900;color:#7C3AED;padding-left:16px;">
                                        IMPUESTOS
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <td style="padding-top:22px;">
                            <table width="100%">
                                <tr>
        """
        + taxItems
        + """
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <td style="border-top:1px solid #E5E7EB;padding-top:18px;text-align:center;font-size:17px;color:#475569;">
                            Total Impuestos
                            <div style="font-size:26px;font-weight:900;color:#7C3AED;margin-top:8px;">
        """
        + money(report.taxes() == null ? null : report.taxes().totalTaxes())
        + """
                            </div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        """;
}

    private String buildDetails(BatchCloseReportDTO report) {
    BatchCloseReportDTO.BatchDetails details = report.details();

    return """
        <tr>
            <td style="padding:22px 28px 0 28px;">
                <table width="100%" cellspacing="0" cellpadding="0" style="border:1px solid #E5E7EB;border-radius:14px;padding:22px;background:#ffffff;">
                    <tr>
                        <td>
                            <table>
                                <tr>
                                    <td>
        """
        + circleIcon("ℹ", "#EFF6FF", "#2563EB", "#BFDBFE")
        + """
                                    </td>

                                    <td style="font-size:22px;font-weight:900;color:#1E293B;padding-left:16px;">
                                        DETALLES DEL CIERRE
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <td style="padding-top:22px;">
                            <table width="100%">
                                <tr>
        """
        + detailCell(
                "Último cierre",
                formatDateTime(details == null ? null : details.previousBatchDate()),
                "#2563EB"
        )
        + detailCell(
                "Cierre actual",
                formatDateTime(details == null ? null : details.currentBatchDate()),
                "#2563EB"
        )
        + detailCell(
                "Duración",
                "🕒 " + formatDuration(details == null ? null : details.duration()),
                "#16A34A"
        )
        + """
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        """;
}

    private String buildFooter() {
        return "<tr><td style=\"padding:24px 32px;border-top:1px solid #E5E7EB;margin-top:20px;\">"
                + "<table width=\"100%\"><tr>"
                + "<td style=\"font-size:14px;color:#64748B;line-height:22px;\">"
                + "Reporte generado automáticamente<br>Gracias por tu excelente trabajo"
                + "</td>"
                + "<td align=\"right\" style=\"font-size:24px;font-weight:900;color:#64748B;\">RMPay</td>"
                + "</tr></table>"
                + "</td></tr>";
    }

    private String paymentRow(String icon, String label, BigDecimal amount, String iconColor) {
        return "<tr>"
                + "<td width=\"42\" style=\"font-size:24px;color:" + iconColor + ";padding-top:22px;\">" + icon + "</td>"
                + "<td style=\"font-size:17px;color:#334155;padding-top:22px;\">" + label + "</td>"
                + "<td align=\"right\" style=\"font-size:17px;font-weight:700;color:#1E293B;padding-top:22px;\">" + money(amount) + "</td>"
                + "</tr>";
    }

    private String separator() {
        return "<tr><td colspan=\"3\" style=\"border-bottom:1px solid #E5E7EB;height:14px;\"></td></tr>";
    }

    private String detailCell(String label, String value, String color) {
        return "<td align=\"center\" style=\"font-size:15px;color:#334155;padding:0 18px;border-right:1px solid #E5E7EB;\">"
                + label
                + "<div style=\"font-size:18px;font-weight:900;color:" + color + ";margin-top:10px;line-height:25px;\">"
                + value
                + "</div>"
                + "</td>";
    }

    private String circleIcon(String icon, String background, String color, String border) {
        return "<div style=\"width:54px;height:54px;border-radius:50%;background:" + background + ";border:1px solid " + border + ";text-align:center;line-height:54px;font-size:27px;color:" + color + ";font-weight:900;\">"
                + icon
                + "</div>";
    }

    private String safeTransactions(BatchCloseReportDTO report) {
        if (report.summary() == null || report.summary().totalTransactions() == null) {
            return "0";
        }
        return String.valueOf(report.summary().totalTransactions());
    }

    private String money(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(US_LOCALE).format(value == null ? BigDecimal.ZERO : value);
    }

    private String formatDate(Instant value) {
        if (value == null) return "-";
        return DATE_FORMAT.format(value.atZone(ZoneId.systemDefault()));
    }

    private String formatTime(Instant value) {
        if (value == null) return "-";
        return TIME_FORMAT.format(value.atZone(ZoneId.systemDefault()));
    }

    private String formatDateTime(Instant value) {
        if (value == null) return "-";
        return DATE_TIME_FORMAT.format(value.atZone(ZoneId.systemDefault()));
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "0h 0m";
        }

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        return hours + "h " + minutes + "m";
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
