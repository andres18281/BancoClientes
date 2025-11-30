package com.example.demo.infraestructura.datos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.demo.dominio.modelo.ProductoFinanciero.EstadoCuenta;
import com.example.demo.dominio.modelo.ProductoFinanciero.TipoCuenta;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "productos_financieros")
// 🔑 ESTRATEGIA DE HERENCIA: Single Table (Todos los tipos en una sola tabla)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) 
@DiscriminatorColumn(name = "tipo_producto", discriminatorType = DiscriminatorType.STRING)
@Data
public abstract class ProductoJPA { 
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long clienteId;
    
    @Column(unique = true)
    private String numeroCuenta;
    
    // Almacenamos el Dinero VO como un BigDecimal
    private BigDecimal saldo; 
    
    @Enumerated(EnumType.STRING)
    private EstadoCuenta estado;
    
    @Enumerated(EnumType.STRING)
    private TipoCuenta tipoCuenta; // Se usa en el Dominio, pero JPA lo almacena
    
    private boolean exentaGMF;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}