package com.example.demo.dominio.port.in;

import java.util.List;

import com.example.demo.dominio.modelo.Transaccion;
import com.example.demo.dominio.modelo.VO.Dinero;

public interface GestionTransaccionPort {
    
    Transaccion consignar(String cuentaDestino, Dinero monto);
    
    Transaccion retirar(String cuentaOrigen, Dinero monto);
    
    // Una transferencia genera 2 registros: débito y crédito
    List<Transaccion> transferir(String cuentaOrigen, String cuentaDestino, Dinero monto);
    
    List<Transaccion> obtenerHistorial(String numeroCuenta);
}
