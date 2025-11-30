package com.example.demo.dominio.port.out;

import java.util.List;

import com.example.demo.dominio.modelo.Transaccion;


public interface TransaccionRepositoryPort {
 
 Transaccion guardar(Transaccion transaccion);
 

 List<Transaccion> guardarMultiples(List<Transaccion> transacciones); 
 
 List<Transaccion> buscarPorCuenta(String numeroCuenta);
}