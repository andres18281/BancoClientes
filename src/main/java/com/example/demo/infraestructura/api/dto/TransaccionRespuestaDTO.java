package com.example.demo.infraestructura.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransaccionRespuestaDTO {
    private Long id;
    private String tipo;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private String numeroCuentaOrigen;
    private String numeroCuentaDestino;
} 