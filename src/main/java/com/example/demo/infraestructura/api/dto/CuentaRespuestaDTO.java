package com.example.demo.infraestructura.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.demo.dominio.modelo.ProductoFinanciero.EstadoCuenta;
import com.example.demo.dominio.modelo.ProductoFinanciero.TipoCuenta;

import lombok.Data;

@Data
public class CuentaRespuestaDTO {
    private Long id;
    private Long clienteId;
    private TipoCuenta tipoCuenta;
    private String numeroCuenta;
    private EstadoCuenta estado;
    
    // El monto como String o BigDecimal para evitar problemas de precisión en la transferencia
    private BigDecimal saldo; 
    
    private LocalDateTime fechaCreacion;
}