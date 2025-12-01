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
        
      
        Cliente cliente = clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("El cliente con ID " + clienteId + " no existe."));

       
        ProductoFinanciero nuevaCuenta;
        if (tipoCuenta == TipoCuenta.AHORROS) {
            nuevaCuenta = new CuentaAhorros(cliente.getId());
        } else if (tipoCuenta == TipoCuenta.CORRIENTE) {
            nuevaCuenta = new CuentaCorriente(cliente.getId());
        } else {
            throw new IllegalArgumentException("Tipo de cuenta no soportado.");
        }
        
        
        return cuentaRepository.guardar(nuevaCuenta);
    }

    @Override
    public void depositar(String numeroCuenta, Dinero monto) {
        ProductoFinanciero cuenta = buscarCuentaActivaPorNumero(numeroCuenta);
        
        cuenta.depositar(monto);
        
        cuentaRepository.guardar(cuenta);
    }

    @Override
    public void retirar(String numeroCuenta, Dinero monto) {
        ProductoFinanciero cuenta = buscarCuentaActivaPorNumero(numeroCuenta);
        
        
        cuenta.retirar(monto);
        
        cuentaRepository.guardar(cuenta);
    }

    @Override
    public void cancelarCuenta(String numeroCuenta) {
        ProductoFinanciero cuenta = buscarCuentaActivaPorNumero(numeroCuenta);
        
        
        cuenta.cancelar();
        
       
        cuentaRepository.guardar(cuenta);
    }
    
    @Override
    public Optional<ProductoFinanciero> buscarCuentaPorNumero(String numeroCuenta) {
    	return cuentaRepository.buscarPorNumero(numeroCuenta);
    }
    
    
    private ProductoFinanciero buscarCuentaActivaPorNumero(String numeroCuenta) {
         ProductoFinanciero cuenta = cuentaRepository.buscarPorNumero(numeroCuenta)
            .orElseThrow(() -> new IllegalArgumentException("Cuenta " + numeroCuenta + " no encontrada."));

        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalStateException("La cuenta no está activa para realizar esta operación.");
        }
        return cuenta;
    }
    
    
    @Override
    public ProductoFinanciero actualizarEstadoCuenta(String numeroCuenta, String nuevoEstado) {

        
        ProductoFinanciero cuenta = cuentaRepository.buscarPorNumero(numeroCuenta)
            .orElseThrow(() -> new IllegalArgumentException("Cuenta " + numeroCuenta + " no encontrada."));

       
        if ("ACTIVA".equalsIgnoreCase(nuevoEstado)) {
            
            cuenta.activar();
        } else if ("INACTIVA".equalsIgnoreCase(nuevoEstado)) {
          
            cuenta.inactivar();
        } else {
            throw new IllegalArgumentException("El estado '" + nuevoEstado + "' no es válido.");
        }
        
     
        return cuentaRepository.guardar(cuenta);
    }
    
    
    
    
}