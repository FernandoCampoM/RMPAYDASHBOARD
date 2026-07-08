package com.retailmanager.rmpaydashboard.services.services.Payment;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConsumeAPIException;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMCancelPaymentResponse;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMPaymentReqData;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ATHMPaymentResponse;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.FindPaymentReqData;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.FindPaymentResponse;

public interface IATHMovilService {
     /**
     * Realiza un pago a través de la API de ATH Móvil.
     * @param paymentData Los datos requeridos para el pago.
     * @return La respuesta de la API de ATH Móvil.
     * @throws ConsumeAPIException Si ocurre un error al consumir la API.
     */
    ATHMPaymentResponse doPayment(ATHMPaymentReqData paymentData) throws ConsumeAPIException;

    /**
     * Busca un pago existente en la API de ATH Móvil.
     * @param paymentData Los datos para buscar el pago.
     * @return La respuesta de la API con los detalles del pago.
     * @throws ConsumeAPIException Si ocurre un error al consumir la API.
     */
    FindPaymentResponse findPayment(FindPaymentReqData paymentData) throws ConsumeAPIException;

    /**
     * Confirma una autorización de pago en la API de ATH Móvil.
     * @param authorization El número de autorización a confirmar.
     * @return La respuesta de la API con la confirmación.
     * @throws ConsumeAPIException Si ocurre un error al consumir la API.
     */
    FindPaymentResponse confirmAuthorization(String authorization) throws ConsumeAPIException;

    /**
     * Cancela un pago en la API de ATH Móvil.
     * @param paymentData Los datos del pago a cancelar.
     * @return La respuesta de la API con el resultado de la cancelación.
     * @throws ConsumeAPIException Si ocurre un error al consumir la API.
     */
    ATHMCancelPaymentResponse cancel(FindPaymentReqData paymentData) throws ConsumeAPIException;
}
