package com.example.demo.aplicacion;



import com.example.demo.dominio.modelo.Transaccion;
import com.example.demo.dominio.modelo.Transaccion.TipoTransaccion;
import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.dominio.port.in.GestionCuentaPort;
import com.example.demo.dominio.port.in.GestionTransaccionPort;
import com.example.demo.dominio.port.out.TransaccionRepositoryPort;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TransaccionService implements GestionTransaccionPort {

    private final GestionCuentaPort cuentaService;
    private final TransaccionRepositoryPort transaccionRepository;

    public TransaccionService(GestionCuentaPort cuentaService, TransaccionRepositoryPort transaccionRepository) {
        this.cuentaService = cuentaService;
        this.transaccionRepository = transaccionRepository;
    }

    // --- 1. Consignar ---
    @Override
    public Transaccion consignar(String cuentaDestino, Dinero monto) {
        log.info("Iniciando consignación de {} a la cuenta {}.", monto, cuentaDestino); // 🔑 Log de inicio
        
        try {
            // 1. Ejecutar movimiento en el servicio de cuenta
            cuentaService.depositar(cuentaDestino, monto);
            
            // 2. Registrar la transacción
            Transaccion registro = new Transaccion(TipoTransaccion.CONSIGNACION, monto, cuentaDestino);
            Transaccion registroGuardado = transaccionRepository.guardar(registro);

            log.info("CONSIGNACIÓN EXITOSA: Cuenta {} recibió {}. ID Transacción: {}", 
                     cuentaDestino, monto, registroGuardado.getId()); // 🔑 Log de éxito
            return registroGuardado;
            
        } catch (RuntimeException e) {
            log.error("CONSIGNACIÓN FALLIDA: Monto {} a cuenta {}. Causa: {}", 
                      monto, cuentaDestino, e.getMessage()); // 🔑 Log de fallo
            throw e; // Re-lanza la excepción para que la capa superior la maneje
        }
    }

  
    @Override
    public Transaccion retirar(String cuentaOrigen, Dinero monto) {
        log.info("Iniciando retiro de {} de la cuenta {}.", monto, cuentaOrigen); // 🔑 Log de inicio
        
        try {
            // 1. Ejecutar movimiento en el servicio de cuenta
            cuentaService.retirar(cuentaOrigen, monto);
            
            // 2. Registrar la transacción
            Transaccion registro = new Transaccion(TipoTransaccion.RETIRO, monto, cuentaOrigen);
            Transaccion registroGuardado = transaccionRepository.guardar(registro);

            log.info("RETIRO EXITOSO: Cuenta {} retiró {}. ID Transacción: {}", 
                     cuentaOrigen, monto, registroGuardado.getId()); // 🔑 Log de éxito
            return registroGuardado;
            
        } catch (RuntimeException e) {
            // Se usa WARN/ERROR ya que la causa más común es Saldo Insuficiente, un fallo de negocio.
            log.warn("RETIRO FALLIDO: Monto {} de cuenta {}. Causa: {}", 
                     monto, cuentaOrigen, e.getMessage()); // 🔑 Log de fallo
            throw e;
        }
    }

    // --- 3. Transferir ---
    @Override
    public List<Transaccion> transferir(String cuentaOrigen, String cuentaDestino, Dinero monto) {
        log.info("Iniciando transferencia de {} desde {} hacia {}.", monto, cuentaOrigen, cuentaDestino); 
        
        try {
            // 1. Ejecutar movimientos (Retiro y luego Depósito)
            // Si el retiro falla (ej. saldo insuficiente), la transacción se aborta (Atomicidad asumida)
            cuentaService.retirar(cuentaOrigen, monto);
            log.debug("Transferencia: Retiro exitoso de {} de la cuenta {}.", monto, cuentaOrigen);
            
            cuentaService.depositar(cuentaDestino, monto);
            log.debug("Transferencia: Depósito exitoso de {} a la cuenta {}.", monto, cuentaDestino);
            
            // 2. Registrar las transacciones (Débito y Crédito)
            Transaccion debito = new Transaccion(TipoTransaccion.TRANSFERENCIA_DEBITO, monto, cuentaOrigen, cuentaDestino);
            Transaccion credito = new Transaccion(TipoTransaccion.TRANSFERENCIA_CREDITO, monto, cuentaOrigen, cuentaDestino);
            
            List<Transaccion> registrosGuardados = transaccionRepository.guardarMultiples(Arrays.asList(debito, credito));

            log.info("TRANSFERENCIA EXITOSA: {} movido de {} a {}.", monto, cuentaOrigen, cuentaDestino);
            return registrosGuardados;
            
        } catch (RuntimeException e) {
            log.error("TRANSFERENCIA FALLIDA: Monto {} (Origen: {}, Destino: {}). Causa: {}", 
                      monto, cuentaOrigen, cuentaDestino, e.getMessage()); // 🔑 Log de fallo
            throw e;
        }
    }

   
    @Override
    public List<Transaccion> obtenerHistorial(String numeroCuenta) {
        log.debug("Buscando historial de transacciones para la cuenta {}.", numeroCuenta);
        
        List<Transaccion> historial = transaccionRepository.buscarPorCuenta(numeroCuenta);
        
        log.debug("Historial de cuenta {} encontrado. Total de {} registros.", numeroCuenta, historial.size());
        return historial;
    }
}