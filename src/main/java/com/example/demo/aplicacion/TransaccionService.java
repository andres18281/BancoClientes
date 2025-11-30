package com.example.demo.aplicacion;



import com.example.demo.dominio.modelo.Transaccion;
import com.example.demo.dominio.modelo.Transaccion.TipoTransaccion;
import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.dominio.port.in.GestionCuentaPort;
import com.example.demo.dominio.port.in.GestionTransaccionPort;
import com.example.demo.dominio.port.out.TransaccionRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TransaccionService implements GestionTransaccionPort {

    private final GestionCuentaPort cuentaService;
    private final TransaccionRepositoryPort transaccionRepository; 

    public TransaccionService(GestionCuentaPort cuentaService, TransaccionRepositoryPort transaccionRepository) {
        this.cuentaService = cuentaService;
        this.transaccionRepository = transaccionRepository;
    }

    @Override
    public Transaccion consignar(String cuentaDestino, Dinero monto) {
        
      
        cuentaService.depositar(cuentaDestino, monto);
        
        
        Transaccion registro = new Transaccion(TipoTransaccion.CONSIGNACION, monto, cuentaDestino);
        
        
        return transaccionRepository.guardar(registro);
    }

    @Override
    public Transaccion retirar(String cuentaOrigen, Dinero monto) {
        
        
        cuentaService.retirar(cuentaOrigen, monto);
        
        
        Transaccion registro = new Transaccion(TipoTransaccion.RETIRO, monto, cuentaOrigen);
        
        
        return transaccionRepository.guardar(registro);
    }

    @Override
    public List<Transaccion> transferir(String cuentaOrigen, String cuentaDestino, Dinero monto) {
        
        
        cuentaService.retirar(cuentaOrigen, monto);
        cuentaService.depositar(cuentaDestino, monto);
        
        
        Transaccion debito = new Transaccion(TipoTransaccion.TRANSFERENCIA_DEBITO, monto, cuentaOrigen, cuentaDestino);
        Transaccion credito = new Transaccion(TipoTransaccion.TRANSFERENCIA_CREDITO, monto, cuentaOrigen, cuentaDestino);
        
       
        return transaccionRepository.guardarMultiples(Arrays.asList(debito, credito));
    }

    @Override
    public List<Transaccion> obtenerHistorial(String numeroCuenta) {
        return transaccionRepository.buscarPorCuenta(numeroCuenta);
    }
}