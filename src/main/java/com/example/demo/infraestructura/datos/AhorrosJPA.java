package com.example.demo.infraestructura.datos;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("AHORROS")
public class AhorrosJPA extends ProductoJPA {
    // No se necesitan campos adicionales por ahora.
}	