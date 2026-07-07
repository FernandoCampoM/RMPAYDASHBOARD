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
import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;

// ── NEW: low inventory email template ──────────────────
// Purpose : Builds the HTML body for the Low Inventory automated report.
// Depends on : Business.name and ProductDTO low inventory fields.
// Does NOT modify : DailySummaryEmailTemplate, EmailService, ReportService, or ProductDTO.
@Component
public class LowInventoryEmailTemplate {

    private static final Locale US_LOCALE = Locale.US;

    public String build(Business business, LocalDate reportDate, List<ProductDTO> products) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(US_LOCALE);

        int totalProducts = products == null ? 0 : products.size();
        int unitsAtRisk = calculateUnitsAtRisk(products);
        BigDecimal suggestedPurchaseTotal = calculateSuggestedPurchaseTotal(products);
        ProductDTO priorityProduct = findPriorityProduct(products);

        String businessName = business != null && business.getName() != null
                ? business.getName()
                : "RMPay";

        String priorityName = priorityProduct != null ? priorityProduct.getName() : "Sin productos";
        String formattedDate = reportDate.format(DateTimeFormatter.ofPattern("M/d/yyyy"));

        StringBuilder html = new StringBuilder();

        html.append("""
                <!doctype html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Inventario bajo</title>
                </head>
                <body style="margin:0; padding:0; background:#eef5f8; font-family:Arial, Helvetica, sans-serif; color:#0b1730;">
                    <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#eef5f8; padding:40px 0;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="720" cellspacing="0" cellpadding="0" style="width:720px; max-width:720px; background:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 18px 38px rgba(15, 33, 58, 0.12);">
                                    <tr>
                                        <td style="background:#0fa58d; padding:46px 48px 36px 48px; color:#ffffff;">
                                            <table role="presentation" width="100%" cellspacing="0" cellpadding="0">
                                                <tr>
                                                    <td style="vertical-align:top;">
                                                        <table role="presentation" cellspacing="0" cellpadding="0">
                                                            <tr>
                                                                <td style="width:54px; vertical-align:top;">
                                                                    <div style="width:48px; height:48px; border-radius:50%; background:rgba(255,255,255,0.18); text-align:center; line-height:48px; font-size:25px;">▣</div>
                                                                </td>
                                                                <td style="padding-left:12px;">
                                                                    <div style="font-size:26px; font-weight:800; line-height:32px;">Inventario bajo</div>
                                                                    <div style="font-size:14px; line-height:22px; color:#dff8f3;">""")
                .append(escapeHtml(businessName))
                .append("""
                                                                    </div>
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                    <td align="right" style="vertical-align:top;">
                                                        <div style="display:inline-block; background:#ffffff; color:#0b1730; border-radius:10px; padding:14px 24px; min-width:150px; text-align:left;">
                                                            <div style="font-size:12px; color:#66728a; font-weight:800;">COMPRA SUGERIDA</div>
                                                            <div style="font-size:22px; line-height:26px; font-weight:900;">""")
                .append(currency.format(suggestedPurchaseTotal))
                .append("""
                                                            </div>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>

                                            <div style="font-size:19px; line-height:28px; margin-top:44px;">Productos que requieren reposición y compra sugerida.</div>
                                            <div style="font-size:13px; line-height:22px; margin-top:10px; color:#dff8f3;">Reporte generado: """)
                .append(formattedDate)
                .append("""
                                            </div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:46px 48px 28px 48px;">
                                            <table role="presentation" width="100%" cellspacing="0" cellpadding="0">
                                                <tr>
                                                    <td width="31%" style="border:1px solid #d9e8e6; border-radius:12px; padding:22px; background:#fbfefd;">
                                                        <div style="font-size:12px; color:#657187; font-weight:800;">UNIDADES EN RIESGO</div>
                                                        <div style="font-size:34px; line-height:40px; font-weight:900;">""")
                .append(unitsAtRisk)
                .append("""
                    <span style="font-size:13px; color:#657187; font-weight:400;"> faltantes</span></div>
                                                    </td>
                                                    <td width="3%"></td>
                                                    <td width="32%" style="border:1px solid #f2d7ca; border-radius:12px; padding:22px; background:#fffaf7;">
                                                        <div style="font-size:12px; color:#8b5529; font-weight:800;">MAYOR PRIORIDAD</div>
                                                        <div style="font-size:20px; line-height:26px; font-weight:900; margin-top:13px;">""")
                .append(escapeHtml(priorityName))
                .append("""
                                                        </div>
                                                    </td>
                                                    <td width="3%"></td>
                                                    <td width="31%" style="border:1px solid #d9e8e6; border-radius:12px; padding:22px; background:#fbfefd;">
                                                        <div style="font-size:12px; color:#657187; font-weight:800;">PRODUCTOS</div>
                                                        <div style="font-size:28px; line-height:38px; font-weight:900; margin-top:8px;">""")
                .append(totalProducts)
                .append("""
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>

                                            <div style="font-size:24px; line-height:30px; font-weight:900; margin-top:26px;">Detalle de productos</div>
                                            <div style="font-size:14px; line-height:22px; color:#66728a; margin-top:8px;">Listado completo de productos con inventario bajo incluidos en este reporte.</div>

                                            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="margin-top:26px; border-collapse:separate; border-spacing:0;">
                                                <tr>
                                                    <th align="left" style="background:#f4f7f6; color:#344258; font-size:12px; padding:18px 22px; border-radius:8px 0 0 8px;">PRODUCTO</th>
                                                    <th align="center" style="background:#f4f7f6; color:#344258; font-size:12px; padding:18px 12px;">INV.</th>
                                                    <th align="center" style="background:#f4f7f6; color:#344258; font-size:12px; padding:18px 12px;">COMPRA SUG.</th>
                                                    <th align="center" style="background:#f4f7f6; color:#344258; font-size:12px; padding:18px 22px; border-radius:0 8px 8px 0;">TOTAL COMPRA SUG</th>
                                                </tr>
                """);

        appendProductRows(html, products, currency);

        html.append("""
                                            </table>

                                            <div style="border-top:1px solid #dde7ea; margin-top:34px; padding-top:34px;">
                                                <table role="presentation" width="100%" cellspacing="0" cellpadding="0">
                                                    <tr>
                                                        <td>
                                                            <div style="font-size:13px; color:#66728a;">RMPay · Reporte automático de inventario bajo</div>
                                                            <div style="font-size:11px; color:#9aa5b8; margin-top:12px;">Recibiste este email porque tienes alertas activas para productos con reposición sugerida.</div>
                                                        </td>
                                                        <td align="right" style="font-size:24px; font-weight:900; color:#9aa5b8;">RMPay</td>
                                                    </tr>
                                                </table>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """);

        return html.toString();
    }

   private void appendProductRows(StringBuilder html, List<ProductDTO> products, NumberFormat currency) {
    if (products == null || products.isEmpty()) {
        html.append("""
                <tr>
                    <td colspan="4" style="padding:26px 22px; color:#66728a; font-size:14px; text-align:center;">
                        No hay productos con inventario bajo.
                    </td>
                </tr>
                """);
        return;
    }

    for (int i = 0; i < products.size(); i++) {
        ProductDTO product = products.get(i);
        String background = i % 2 == 1 ? "#fbfbfb" : "#ffffff";

        int suggestedPurchase = product.getSuggestedPurchase();
        BigDecimal cost = product.getCost() == null ? BigDecimal.ZERO : product.getCost();
        BigDecimal total = cost.multiply(BigDecimal.valueOf(suggestedPurchase));

        html.append("<tr>")
                .append("<td style=\"background:").append(background).append("; padding:18px 22px; border-bottom:1px solid #eef2f3;\">")
                .append("<div style=\"font-size:15px; line-height:20px; font-weight:800; color:#0b1730;\">")
                .append(escapeHtml(product.getName()))
                .append("</div>")
                .append("<div style=\"font-size:12px; line-height:18px; color:#66728a; margin-top:6px;\">UPC ")
                .append(escapeHtml(product.getBarcode()))
                .append(" · Costo producto ")
                .append(currency.format(cost))
                .append("</div>")
                .append("</td>")
                .append("<td align=\"center\" style=\"background:").append(background).append("; padding:18px 12px; border-bottom:1px solid #eef2f3; color:#d71919; font-size:15px; font-weight:900;\">")
                .append(product.getQuantity())
                .append("</td>")
                .append("<td align=\"center\" style=\"background:").append(background).append("; padding:18px 12px; border-bottom:1px solid #eef2f3; color:#0b1730; font-size:15px; font-weight:900;\">")
                .append(suggestedPurchase)
                .append("</td>")
                .append("<td align=\"center\" style=\"background:").append(background).append("; padding:18px 22px; border-bottom:1px solid #eef2f3; color:#0b1730; font-size:15px; font-weight:900;\">")
                .append(currency.format(total))
                .append("</td>")
                .append("</tr>");
    }
}

    private int calculateUnitsAtRisk(List<ProductDTO> products) {
        if (products == null) {
            return 0;
        }

        return products.stream()
                .mapToInt(ProductDTO::getQuantity)
                .filter(quantity -> quantity < 0)
                .map(Math::abs)
                .sum();
    }

   private BigDecimal calculateSuggestedPurchaseTotal(List<ProductDTO> products) {
    if (products == null) {
        return BigDecimal.ZERO;
    }

    return products.stream()
            .map(product -> {
                BigDecimal cost = product.getCost() == null ? BigDecimal.ZERO : product.getCost();
                return cost.multiply(BigDecimal.valueOf(product.getSuggestedPurchase()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}

    private ProductDTO findPriorityProduct(List<ProductDTO> products) {
        if (products == null || products.isEmpty()) {
            return null;
        }

        return products.stream()
                .min(Comparator.comparingInt(ProductDTO::getQuantity))
                .orElse(null);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}