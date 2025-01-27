package com.bettercloud.secret_santa.exceptions;

import com.bettercloud.secret_santa.dto.ApiResponseDTO;
import com.bettercloud.secret_santa.util.AppMessages;
import com.bettercloud.secret_santa.util.Meta;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Global exception handler using @ControllerAdvice.
 * <p>
 * Extends ResponseEntityExceptionHandler to manage specific custom exceptions
 * and any unhandled exceptions in a centralized manner. Returns standardized
 * responses with detailed information for easier debugging.
 */
@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles AppSecretSantaException by building a structured error response.
     * <p>
     * The Meta object is populated with the status, code, message, transaction ID,
     * and timestamp.
     *
     * @param ex the thrown AppSecretSantaException
     * @return a ResponseEntity containing the ApiResponseDTO with error details
     */
    @ExceptionHandler(value = {AppSecretSantaException.class})
    public ResponseEntity<Object> handleAppRolesException(AppSecretSantaException ex) {
        Meta meta = new Meta();
        meta.setDevMessage(null);
        meta.setStatus(ex.getStatus());
        meta.setStatusCode(ex.getCode());
        meta.setMessage(ex.getMessage());
        meta.setTransactionID(UUID.randomUUID().toString());
        meta.setTimestamp(LocalDateTime.now().toString());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ApiResponseDTO apiResponse = new ApiResponseDTO(meta, null);
        return new ResponseEntity<>(apiResponse, httpHeaders, ex.getCode());
    }

    /**
     * Fallback exception handler for any unhandled exception that does not match
     * other @ExceptionHandler methods.
     * @param runtimeException the unhandled exception
     * @param webRequest       the current web request
     * @return a ResponseEntity with an INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleException(RuntimeException runtimeException, WebRequest webRequest) {
        Meta meta = new Meta();
        meta.setStatus(AppMessages.ERROR);
        meta.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        meta.setDevMessage(runtimeException.getMessage() == null ? runtimeException.getClass().toString() : runtimeException.getMessage());
        meta.setTimestamp(LocalDateTime.now().toString());
        meta.setTransactionID(UUID.randomUUID().toString());
        ApiResponseDTO apiResponse = new ApiResponseDTO(meta, null);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return handleExceptionInternal(
                runtimeException,
                apiResponse,
                httpHeaders,
                HttpStatus.INTERNAL_SERVER_ERROR,
                webRequest
        );
    }



}
