package com.example.demo.infraestructura.datos;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.demo.dominio.modelo.Transaccion.TipoTransaccion;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "transacciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;
    
    private BigDecimal monto;
    private LocalDateTime fecha;
    private String numeroCuentaOrigen;
    private String numeroCuentaDestino;
}