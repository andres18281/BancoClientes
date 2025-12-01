package com.example.demo.dominio.port.in;

import java.util.Optional;

import com.example.demo.dominio.modelo.ProductoFinanciero;
import com.example.demo.dominio.modelo.ProductoFinanciero.TipoCuenta;
import com.example.demo.dominio.modelo.VO.Dinero;

public interface GestionCuentaPort {
    
    ProductoFinanciero crearCuenta(Long clienteId, TipoCuenta tipoCuenta);
    
    void depositar(String numeroCuenta, Dinero monto);
    
    void retirar(String numeroCuenta, Dinero monto);
    
    void cancelarCuenta(String numeroCuenta);

    Optional<ProductoFinanciero> buscarCuentaPorNumero(String numeroCuenta);
}	