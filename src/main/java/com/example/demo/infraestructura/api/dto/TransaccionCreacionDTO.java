package com.example.demo.infraestructura.api.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor
public class TransaccionCreacionDTO {
    
    // Tipo de movimiento (Consignación, Retiro, Transferencia)
    private String tipoMovimiento; 
     
    // Cuenta de la que sale el dinero (requerido para Retiro y Transferencia)
    private String cuentaOrigen;
    
    // Cuenta a la que entra el dinero (requerido para Consignación y Transferencia)
    private String cuentaDestino;
    
    // El monto como BigDecimal para asegurar precisión
    private BigDecimal monto;
}