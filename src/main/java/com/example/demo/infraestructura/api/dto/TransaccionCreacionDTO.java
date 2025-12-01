package com.example.demo.infraestructura.api.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionCreacionDTO {
    
    // Tipo de movimiento (Consignación, Retiro, Transferencia)
    private String tipoMovimiento; 
     
    // Cuenta de la que sale el dinero (requerido para Retiro y Transferencia)
    private String cuentaOrigen;
    
    // Cuenta a la que entra el dinero (requerido para Consignación y Transferencia)
    private String cuentaDestino;
    
    // El monto como BigDecimal para asegurar precisión
    @NotNull(message = "El monto es obligatorio y no puede ser nulo.")
    @DecimalMin(value = "0.01", message = "El monto debe ser un valor positivo.")
    @JsonProperty("monto")
    private BigDecimal monto;
}