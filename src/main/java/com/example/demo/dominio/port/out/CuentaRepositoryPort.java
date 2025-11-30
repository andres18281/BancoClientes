package com.example.demo.dominio.port.out;

import java.util.Optional;

import com.example.demo.dominio.modelo.ProductoFinanciero;

public interface CuentaRepositoryPort {
    
    ProductoFinanciero guardar(ProductoFinanciero producto);
    
    Optional<ProductoFinanciero> buscarPorNumero(String numeroCuenta);
    
    Optional<ProductoFinanciero> buscarPorId(Long id);
    
   
    boolean existeNumeroCuenta(String numeroCuenta);
}