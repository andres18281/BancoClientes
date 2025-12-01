package com.example.demo.aplicacion;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.dominio.modelo.Cliente;
import com.example.demo.dominio.modelo.CuentaAhorros;
import com.example.demo.dominio.modelo.CuentaCorriente;
import com.example.demo.dominio.modelo.ProductoFinanciero;
import com.example.demo.dominio.modelo.ProductoFinanciero.EstadoCuenta;
import com.example.demo.dominio.modelo.ProductoFinanciero.TipoCuenta;
import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.dominio.port.in.GestionCuentaPort;
import com.example.demo.dominio.port.out.ClienteRepositoryPort;
import com.example.demo.dominio.port.out.CuentaRepositoryPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service
public class CuentaService implements GestionCuentaPort {

    private final CuentaRepositoryPort cuentaRepository;
    private final ClienteRepositoryPort clienteRepository;

    public CuentaService(CuentaRepositoryPort cuentaRepository, ClienteRepositoryPort clienteRepository) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
    }

    
    @Override
    public ProductoFinanciero crearCuenta(Long clienteId, TipoCuenta tipoCuenta) {
        log.info("Iniciando creación de cuenta {} para Cliente ID {}.", tipoCuenta, clienteId);

        Cliente cliente = clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> {
                    log.error("CREACIÓN FALLIDA: El cliente con ID {} no existe.", clienteId); 
                    return new IllegalArgumentException("El cliente con ID " + clienteId + " no existe.");
                });

        ProductoFinanciero nuevaCuenta;
        if (tipoCuenta == TipoCuenta.AHORROS) {
            nuevaCuenta = new CuentaAhorros(cliente.getId());
        } else if (tipoCuenta == TipoCuenta.CORRIENTE) {
            nuevaCuenta = new CuentaCorriente(cliente.getId());
        } else {
            log.error("CREACIÓN FALLIDA: Tipo de cuenta no soportado: {}", tipoCuenta);
            throw new IllegalArgumentException("Tipo de cuenta no soportado.");
        }

        ProductoFinanciero cuentaGuardada = cuentaRepository.guardar(nuevaCuenta);
        log.info("CREACIÓN EXITOSA: Cuenta {} creada con número {} para Cliente ID {}.", 
                 tipoCuenta, cuentaGuardada.getNumeroCuenta(), clienteId); 
        return cuentaGuardada;
    }

    
    @Override
    public void depositar(String numeroCuenta, Dinero monto) {
        log.info("Iniciando depósito de {} a la cuenta {}.", monto, numeroCuenta); 
        
        ProductoFinanciero cuenta = buscarCuentaActivaPorNumero(numeroCuenta);
        Dinero saldoAnterior = cuenta.getSaldo();
        
        cuenta.depositar(monto);
        
        cuentaRepository.guardar(cuenta);
        log.info("DEPÓSITO EXITOSO: Cuenta {} recibió {}. Saldo final: {}.", 
                 numeroCuenta, monto, cuenta.getSaldo()); 
    }

    
    @Override
    public void retirar(String numeroCuenta, Dinero monto) {
        log.info("Iniciando retiro de {} de la cuenta {}.", monto, numeroCuenta); 
        
        ProductoFinanciero cuenta = buscarCuentaActivaPorNumero(numeroCuenta);
        Dinero saldoAnterior = cuenta.getSaldo(); 
        
        try {
            cuenta.retirar(monto);
        } catch (IllegalStateException e) {
            
            log.warn("RETIRO FALLIDO: Cuenta {} con saldo {}. Intento de retiro de {}. Causa: {}", 
                     numeroCuenta, saldoAnterior, monto, e.getMessage()); 
            throw e;
        }
        
        cuentaRepository.guardar(cuenta);
        log.info("RETIRO EXITOSO: Cuenta {} retiró {}. Saldo final: {}.", 
                 numeroCuenta, monto, cuenta.getSaldo()); 
    }

   
    @Override
    public void cancelarCuenta(String numeroCuenta) {
        log.warn("Iniciando cancelación de la cuenta {}.", numeroCuenta); 
        
        ProductoFinanciero cuenta = buscarCuentaActivaPorNumero(numeroCuenta);
        
        try {
            cuenta.cancelar(); 
        } catch (IllegalStateException e) {
            log.warn("CANCELACIÓN FALLIDA: Cuenta {} no pudo cancelarse. Causa: {}", numeroCuenta, e.getMessage()); 
            throw e;
        }
        
        cuentaRepository.guardar(cuenta);
        log.warn("CANCELACIÓN EXITOSA: Cuenta {} ha sido CANCELADA.", numeroCuenta);
    }

    
    @Override
    public Optional<ProductoFinanciero> buscarCuentaPorNumero(String numeroCuenta) {
        Optional<ProductoFinanciero> cuenta = cuentaRepository.buscarPorNumero(numeroCuenta);
        
        if (cuenta.isPresent()) {
            log.debug("CONSULTA: Cuenta {} encontrada.", numeroCuenta); 
        } else {
            log.debug("CONSULTA: Cuenta {} no encontrada.", numeroCuenta); 
        }
        return cuenta;
    }


    @Override
    public ProductoFinanciero actualizarEstadoCuenta(String numeroCuenta, String nuevoEstado) {
        log.info("Iniciando cambio de estado para Cuenta {} a {}.", numeroCuenta, nuevoEstado); 

        ProductoFinanciero cuenta = cuentaRepository.buscarPorNumero(numeroCuenta)
                .orElseThrow(() -> {
                    log.error("ACTUALIZACIÓN ESTADO FALLIDA: Cuenta {} no encontrada.", numeroCuenta); 
                    return new IllegalArgumentException("Cuenta " + numeroCuenta + " no encontrada.");
                });

        EstadoCuenta estadoAnterior = cuenta.getEstado();

        if ("ACTIVA".equalsIgnoreCase(nuevoEstado)) {
            cuenta.activar();
        } else if ("INACTIVA".equalsIgnoreCase(nuevoEstado)) {
            cuenta.inactivar();
        } else {
            log.error("ACTUALIZACIÓN ESTADO FALLIDA: Estado '{}' no es válido para cuenta {}.", nuevoEstado, numeroCuenta); 
            throw new IllegalArgumentException("El estado '" + nuevoEstado + "' no es válido.");
        }

        ProductoFinanciero cuentaActualizada = cuentaRepository.guardar(cuenta);
        log.info("ACTUALIZACIÓN ESTADO EXITOSA: Cuenta {} cambió de {} a {}.", 
                 numeroCuenta, estadoAnterior, cuentaActualizada.getEstado()); 
        return cuentaActualizada;
    }
    
   
    private ProductoFinanciero buscarCuentaActivaPorNumero(String numeroCuenta) {
        ProductoFinanciero cuenta = cuentaRepository.buscarPorNumero(numeroCuenta)
            .orElseThrow(() -> {
                log.warn("OPERACIÓN RECHAZADA: Cuenta {} no encontrada para la operación.", numeroCuenta);
                return new IllegalArgumentException("Cuenta " + numeroCuenta + " no encontrada.");
            });

        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            log.warn("OPERACIÓN RECHAZADA: Cuenta {} está en estado {}. Debe estar ACTIVA.", numeroCuenta, cuenta.getEstado()); 
            throw new IllegalStateException("La cuenta no está activa para realizar esta operación.");
        }
        log.debug("Cuenta {} verificada como ACTIVA.", numeroCuenta); 
        return cuenta;
    }
}