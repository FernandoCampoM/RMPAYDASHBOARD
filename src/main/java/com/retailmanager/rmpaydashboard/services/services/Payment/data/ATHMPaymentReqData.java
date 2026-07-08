package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ATHMPaymentReqData {
    /**
     * @param env Indica el entorno de la transacción (por ejemplo, "production" para el entorno de producción).
     */
    private String env;

    /**
     * @param publicToken **REQUERIDO**. String. Token público de la cuenta de negocio.
     * Determina la cuenta de negocio a la que se enviará el pago.
     */
    private String publicToken;

    /**
     * @param timeout Opcional. Number (entre 120 y 600). Límite de tiempo antes de que el servicio responda
     * con un error de timeout. El valor por defecto es 600 segundos (10 minutos).
     */
    private String timeout; // Se mantiene como String para coincidir con el JSON original, pero idealmente sería Number

    /**
     * @param total **REQUERIDO**. Number (de 1.00 a 1500.00). Monto total a pagar por el usuario final.
     */
    private String total; // Se mantiene como String para coincidir con el JSON original, pero idealmente sería Number

    /**
     * @param tax Opcional. Number. Variable opcional para mostrar el impuesto de pago (si aplica).
     */
    private String tax; // Se mantiene como String para coincidir con el JSON original, pero idealmente sería Number

    /**
     * @param subtotal Opcional. Number. Variable opcional para mostrar el subtotal del pago (si aplica).
     */
    private String subtotal; // Se mantiene como String para coincidir con el JSON original, pero idealmente sería Number

    /**
     * @param metadata1 **REQUERIDO**. String. Variable que se puede llenar con información adicional de la transacción.
     * Por ejemplo, ID de la tienda, ubicación, etc. Longitud máxima: 40 caracteres.
     */
    private String metadata1;

    /**
     * @param metadata2 **REQUERIDO**. String. Variable que se puede llenar con información adicional de la transacción.
     * Por ejemplo, ID de la tienda, ubicación, etc. Longitud máxima: 40 caracteres.
     */
    private String metadata2;

    /**
     * @param items **REQUERIDO**. Array de objetos ItemATHM. Variable opcional para mostrar los ítems que el usuario
     * está comprando en la pantalla de pago de ATH Móvil.
     * Los campos 'metadata' y 'tax' dentro de cada ítem son requeridos, pero pueden ser nulos.
     */
    private List<ItemATHM> items;

    /**
     * @param phoneNumber **REQUERIDO**. Number. El número de teléfono registrado en la cuenta de ATH Móvil
     * a la que se enviará el pago.
     */
    private String phoneNumber; // Se mantiene como String para coincidir con el JSON original, pero idealmente sería Number
}
