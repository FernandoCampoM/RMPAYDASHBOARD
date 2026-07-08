package com.retailmanager.rmpaydashboard.exceptionControllers;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.CodigoError;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConfigurationNotFoundException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.DataInconsistencyException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ErrorUtils;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.InvalidDateOrTime;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.InvalidToken;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.MaxTerminalsReached;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.TerminalDisabled;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.UserDisabled;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.Error;
import jakarta.servlet.http.HttpServletRequest;


/** Controlador de Excepciones */
@ControllerAdvice
public class RestApiExceptionHandler {
        /**
         * Atiende las Exepciones genericas y crea una rspuesta 
         * @param req
         * @param ex
         * @param locale
         * @return ResponseEntity<Error> con información del error
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Error> handleGenericException(final HttpServletRequest req,final Exception ex, final Locale locale) {
                final Error error = ErrorUtils.crearError(CodigoError.ERROR_GENERICO.getCodigo(),CodigoError.ERROR_GENERICO.getLlaveMensaje() + " "+ex.getMessage(),
                                                HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        /**
         * Atiende las Exepciones cusadas por falta de privilegios en una petición y crea una respuesta
         * @param req
         * @param ex
         * @param locale
         * @return ResponseEntity<Error> con información del error
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Error> handleGenericExceptionn(final HttpServletRequest req,final Exception ex, final Locale locale) {
                final Error error = ErrorUtils.crearError(CodigoError.UNAUTHORISED.getCodigo(),CodigoError.UNAUTHORISED.getLlaveMensaje(),
                                                HttpStatus.UNAUTHORIZED.value())
                                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
        @ExceptionHandler(DataInconsistencyException.class)
        public ResponseEntity<Error> handleDataInconsistencyException(final HttpServletRequest req,final Exception ex, final Locale locale) {
                final Error error = ErrorUtils.crearError(CodigoError.DATA_INCONSISTENCY.getCodigo(),CodigoError.DATA_INCONSISTENCY.getLlaveMensaje(),
                                                HttpStatus.CONFLICT.value())
                                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
            return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        }
        /**
         * Atiende las Exepciones EntidadYaExisteException causadas al registrar una entidad ya existente y crea una respuesta
         * @param req 
         * @param ex
         * @param locale
         * @return ResponseEntity<Error> con información del error
         */
        @ExceptionHandler(EntidadYaExisteException.class)
        public ResponseEntity<Error> handleGenericException(final HttpServletRequest req,
                        final EntidadYaExisteException ex, final Locale locale) {
                final Error error = ErrorUtils
                                .crearError(CodigoError.ENTIDAD_YA_EXISTE.getCodigo(),
                                                String.format("%s, %s", CodigoError.ENTIDAD_YA_EXISTE.getLlaveMensaje(),
                                                                ex.getMessage()),
                                                HttpStatus.NOT_ACCEPTABLE.value())
                                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
                return new ResponseEntity<>(error, HttpStatus.NOT_ACCEPTABLE);
        }
        
         /**
          * Atiende las Exepciones EntidadNoExisteException al realizar una petición sobre una entidad que no existe y crea una rspuesta
          * @param req
          * @param ex
          * @param locale
          * @return ResponseEntity<Error> con información del error
          */
        @ExceptionHandler(EntidadNoExisteException.class)
        public ResponseEntity<Error> handleGenericException(final HttpServletRequest req,
                        final EntidadNoExisteException ex, final Locale locale) {
                final Error error = ErrorUtils
                                .crearError(CodigoError.ENTIDAD_NO_ENCONTRADA.getCodigo(),
                                                String.format("%s, %s",
                                                                CodigoError.ENTIDAD_NO_ENCONTRADA.getLlaveMensaje(),
                                                                ex.getMessage()),
                                                HttpStatus.NOT_FOUND.value())
                                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        @ExceptionHandler(ConfigurationNotFoundException.class)
        public ResponseEntity<Error> handleConfigurationNotFoundException(final HttpServletRequest req,
                        final ConfigurationNotFoundException ex, final Locale locale) {
                final Error error = ErrorUtils
                                .crearError(CodigoError.CONFIGURATION_NOT_FOUND.getCodigo(),
                                                String.format("%s, %s",
                                                                CodigoError.CONFIGURATION_NOT_FOUND.getLlaveMensaje(),
                                                                ex.getMessage()),
                                                HttpStatus.NOT_FOUND.value())
                                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        @ExceptionHandler(MaxTerminalsReached.class)
        public ResponseEntity<Error> handleMaxTerminalsReachedException(final HttpServletRequest req,
                        final MaxTerminalsReached ex, final Locale locale) {
                final Error error = ErrorUtils
                                .crearError(CodigoError.MAX_TERMINALS_REACHED.getCodigo(),
                                                String.format("%s, %s",
                                                                CodigoError.MAX_TERMINALS_REACHED.getLlaveMensaje(),
                                                                ex.getMessage()),
                                                HttpStatus.PAYMENT_REQUIRED.value())
                                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
                return new ResponseEntity<>(error, HttpStatus.PAYMENT_REQUIRED);
        }
        @ExceptionHandler(TerminalDisabled.class)
        public ResponseEntity<Error> handleTerminalDisabledException(final HttpServletRequest req,
                        final TerminalDisabled ex, final Locale locale) {
                final Error error = ErrorUtils
                                .crearError(CodigoError.MAX_TERMINALS_REACHED.getCodigo(),
                                                String.format("%s, %s",
                                                                CodigoError.TERMINAL_DISABLED.getLlaveMensaje(),
                                                                ex.getMessage()),
                                                HttpStatus.FORBIDDEN.value())
                                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }
        @ExceptionHandler(UserDisabled.class)
        public ResponseEntity<Error> handleUserDisabledException(final HttpServletRequest req,
                        final UserDisabled ex, final Locale locale) {
                final Error error = ErrorUtils
                                .crearError(CodigoError.USUARIO_DESACTIVADO.getCodigo(),
                                                String.format("%s, %s",
                                                                CodigoError.USUARIO_DESACTIVADO.getLlaveMensaje(),
                                                                ex.getMessage()),
                                                HttpStatus.UNAUTHORIZED.value())
                                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
        @ExceptionHandler(InvalidToken.class)
        public ResponseEntity<Error> handleUserDisabledException(final HttpServletRequest req,
                        final InvalidToken ex, final Locale locale) {
                final Error error = ErrorUtils
                                .crearError(CodigoError.INVALID_TOKEN.getCodigo(),
                                                String.format("%s, %s",
                                                                CodigoError.INVALID_TOKEN.getLlaveMensaje(),
                                                                ex.getMessage()),
                                                HttpStatus.BAD_REQUEST.value())
                                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        @ExceptionHandler(InvalidDateOrTime.class)
        public ResponseEntity<Error> handleInvalidDateOrTime(final HttpServletRequest req,
                        final InvalidToken ex, final Locale locale) {
                final Error error = ErrorUtils
                                .crearError(CodigoError.INVALID_DATEORTIME.getCodigo(),
                                                String.format("%s, %s",
                                                                CodigoError.INVALID_DATEORTIME.getLlaveMensaje(),
                                                                ex.getMessage()),
                                                HttpStatus.UNPROCESSABLE_ENTITY.value())
                                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
                return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        /**
         * Atiende las Exepciones MethodArgumentNotValidException y crea una rspuesta
         * @param ex
         * @return ResponseEntity<Error> con información del error
         */
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });
                System.out.println("Error de validación: " );
                return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        /**
         * Atiende las Exepciones ConstraintViolationException y crea una rspuesta
         * @param e
         * @return ResponseEntity<Error> con información del error
         */
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler(ConstraintViolationException.class)
        ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
                System.out.println("Error de validación: " + e.getMessage());
                return new ResponseEntity<>("nombre del método y parametros erroneos: " + e.getMessage(),
                                HttpStatus.BAD_REQUEST);
        }
}