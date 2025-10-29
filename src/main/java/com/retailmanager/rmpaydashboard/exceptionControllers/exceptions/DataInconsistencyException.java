package com.retailmanager.rmpaydashboard.exceptionControllers.exceptions;

import lombok.Getter;

@Getter
public class DataInconsistencyException extends RuntimeException {
    private final String llaveMensaje;
  private final String codigo;

  public DataInconsistencyException(CodigoError code) {
    super(code.getCodigo());
    this.llaveMensaje = code.getLlaveMensaje();
    this.codigo = code.getCodigo();
  }

  public DataInconsistencyException(final String message) {
    super(message);
    this.llaveMensaje = CodigoError.DATA_INCONSISTENCY.getLlaveMensaje();
    this.codigo = CodigoError.DATA_INCONSISTENCY.getCodigo();
  }
}
