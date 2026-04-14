package com.jardel.LogiDash.handler;

import com.jardel.LogiDash.exception.ApiIndisponivelException;
import com.jardel.LogiDash.exception.RateLimitException;
import com.jardel.LogiDash.exception.SleepInterrompidoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Map<String, String> handleRateLimit(RateLimitException ex) {
        return Map.of("erro", ex.getMessage());
    }

    @ExceptionHandler(ApiIndisponivelException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleApiIndisponivel(ApiIndisponivelException ex) {
        return Map.of("erro", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGenerico(Exception ex) {
        return Map.of("erro", "Erro interno: " + ex.getMessage());
    }

    @ExceptionHandler(SleepInterrompidoException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleSleepInterrompido(SleepInterrompidoException ex) {
        return Map.of("erro", ex.getMessage());
    }
}
