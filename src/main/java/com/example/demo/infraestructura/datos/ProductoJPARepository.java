package com.example.demo.infraestructura.datos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.dominio.modelo.ProductoFinanciero.EstadoCuenta;

import java.util.Optional;

@Repository
public interface ProductoJPARepository extends JpaRepository<ProductoJPA, Long> {

    // Método requerido para buscar por el número de cuenta
    Optional<ProductoJPA> findByNumeroCuenta(String numeroCuenta);
    
    // Método auxiliar usado en ClienteJPAAdapter para validar la regla de eliminación del cliente
    long countByClienteIdAndEstadoNot(Long clienteId, EstadoCuenta estado);
}
