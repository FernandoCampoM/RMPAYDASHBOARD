package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FindPaymentResponse {
    /**
     * **Status**: Confirma el estado de la respuesta del servicio.
     * 
     */
    private String status;
    private String message;
    private String errorcode;
    /**
     * **Data**: Contiene los detalles específicos de la transacción y el estado del comercio electrónico.
     */
    private Data data;
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class Data {
        /**
         * **Ecommerce Status**: Representa el estado de la transacción de comercio electrónico.
         * "OPEN":  Transaction Pending to be confirmed by the ATH Móvil customer
        * "CONFIRM": Transaction confirmed by the ATH Móvil customer but pending to be processed by the merchant
        * "COMPLETED": Completed transaction
        * "CANCEL": Transaction Expired or Canceled
         */
        private String ecommerceStatus;

        /**
         * **Ecommerce ID**: Este ID representa el ticket o la factura de la transacción.
         * Es un identificador único para la transacción en el sistema de comercio electrónico.
         */
        private String ecommerceId;

        /**
         * **Reference Number**: Identificador único de la transacción.
         */
        private String referenceNumber;

        /**
         * **Business Customer ID**: Identificador del cliente de negocio.
         */
        private String businessCustomerId;

        /**
         * **Transaction Date**: Fecha de autorización de la transacción.
         * Idealmente, este campo podría ser un tipo de dato de fecha/hora como {@code LocalDate} o {@code LocalDateTime}
         * para un mejor manejo, pero se mantiene como String para el mapeo directo con el JSON.
         */
        private String transactionDate;

        /**
         * **Daily Transaction ID**: Contador de ID para la transacción en el día.
         */
        private String dailyTransactionId;

        /**
         * **Business Name**: Nombre del negocio asociado a la cuenta de ATH Business.
         */
        private String businessName;

        /**
         * **Business Path**: Ruta o identificador del negocio en el sistema de ATH Business.
         */
        private String businessPath;

        /**
         * **Industry**: Industria a la que pertenece el negocio (ej. "COMPUTERS", "RETAIL").
         */
        private String industry;

        /**
         * **Subtotal**: Monto del subtotal de la transacción, antes de aplicar impuestos y tarifas.
         */
        private Double subTotal;

        /**
         * **Tax**: Impuesto a ser cargado en la transacción.
         */
        private Double tax;

        /**
         * **Total**: Monto total de la transacción (subtotal + impuestos + tarifas).
         */
        private Double total;

        /**
         * **Fee**: Tarifa o comisión a ser cargada en la transacción.
         */
        private Double fee;

        /**
         * **Net Amount**: Monto neto de la transacción, después de todas las deducciones o adiciones.
         */
        private Double netAmount;

        /**
         * **Total Refunded Amount**: Cantidad total reembolsada de la transacción original.
         */
        private Double totalRefundedAmount;

        /**
         * **Metadata 1**: Variable que puede llenarse con información adicional de la transacción,
         * como el ID de la tienda o la ubicación. Longitud máxima: 40 caracteres.
         */
        private String metadata1;

        /**
         * **Metadata 2**: Variable que puede llenarse con información adicional de la transacción,
         * como notas internas o referencias. Longitud máxima: 40 caracteres.
         */
        private String metadata2;

        /**
         * **Items**: Lista de los artículos pagados en la transacción.
         */
        private List<ItemData> items;

        /**
         * **Is Non-Profit**: Indica si el negocio es una organización sin fines de lucro.
         */
        private Boolean isNonProfit;
    }
    /**
     * Clase interna que representa un ítem individual dentro de la lista de ítems de la transacción.
     */
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class ItemData {
        /**
         * **Name**: El nombre del ítem.
         */
        private String name;

        /**
         * **Description**: La descripción del ítem.
         */
        private String description;

        /**
         * **Quantity**: La cantidad de este ítem.
         */
        private Integer quantity;

        /**
         * **Price**: El precio unitario del ítem.
         */
        private Double price;

        /**
         * **Tax**: El impuesto aplicado a este ítem.
         */
        private Double tax;

        /**
         * **Metadata**: Información adicional o metadatos específicos del ítem.
         */
        private String metadata;

        /**
         * **Formatted Price**: El precio del ítem formateado como cadena. Puede estar vacío.
         */
        private String formattedPrice;

        /**
         * **SKU**: El SKU (Stock Keeping Unit) del ítem. Puede estar vacío.
         */
        private String sku;
    }
}
