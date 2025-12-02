package com.example.demo.infraestructura.api.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaCreacionDTO {
    // ID del cliente al que se le abrirá la cuenta
    private Long clienteId; 
    
    // Tipo de cuenta a crear: AHORROS o CORRIENTE
    private String tipoCuenta; 
} 