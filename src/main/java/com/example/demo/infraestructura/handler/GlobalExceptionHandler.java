package com.example.demo.infraestructura.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja las excepciones de tipo IllegalStateException e IllegalArgumentException.
     * Estas excepciones suelen ser lanzadas por las capas de Dominio/Servicio cuando
     * se violan reglas de negocio (ej. Saldo insuficiente, Cuenta inactiva, Menor de edad).
     * Mapeamos estas violaciones a 400 Bad Request (Solicitud Inválida).
     */
    @ExceptionHandler({
        IllegalStateException.class, 
        IllegalArgumentException.class
    })
    public ResponseEntity<String> handleBusinessValidationExceptions(
        Exception ex, 
        WebRequest request
    ) {
        // Loguea la advertencia o el error del negocio para trazabilidad
        log.warn("Solicitud de Negocio Inválida detectada. Mensaje: {}", ex.getMessage());

        // Retorna el estado 400 Bad Request y el mensaje de error del dominio
        return new ResponseEntity<>(
            ex.getMessage(), 
            HttpStatus.BAD_REQUEST // ⬅️ Esto es lo que produce el 400
        );
    }

    /**
     * Maneja excepciones genéricas no previstas (generalmente 500 Internal Server Error).
     * Descomentar si deseas un manejo explícito del 500, aunque Spring lo hace por defecto.
     * @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Error Interno del Servidor: {}", ex.getMessage(), ex);
        return new ResponseEntity<>("Ocurrió un error inesperado en el servidor.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    */
}