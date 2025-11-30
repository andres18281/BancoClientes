package com.example.demo.aplicacion;

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
    private final ClienteRepositoryPort clienteRepository; // Se necesita para validar el cliente

    public CuentaService(CuentaRepositoryPort cuentaRepository, ClienteRepositoryPort clienteRepository) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public ProductoFinanciero crearCuenta(Long clienteId, TipoCuenta tipoCuenta) {
        
        // 1. Orquestación: Validar si el cliente existe (Regla de Negocio y Seguridad)
        Cliente cliente = clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("El cliente con ID " + clienteId + " no existe."));

        // 2. 🔑 Fábrica de Entidades (Dentro del Use Case o una Factory separada)
        ProductoFinanciero nuevaCuenta;
        if (tipoCuenta == TipoCuenta.AHORROS) {
            nuevaCuenta = new CuentaAhorros(cliente.getId());
        } else if (tipoCuenta == TipoCuenta.CORRIENTE) {
            nuevaCuenta = new CuentaCorriente(cliente.getId());
        } else {
            throw new IllegalArgumentException("Tipo de cuenta no soportado.");
        }
        
        // 3. Orquestación: Verificar unicidad del número de cuenta (aunque la BD lo garantiza, 
        // a veces se revisa antes) - Depende de cómo se implemente generarNumeroCuenta().
        // Por ahora, asumimos que el constructor de la Cuenta maneja la primera generación.
        
        // 4. Persistencia
        return cuentaRepository.guardar(nuevaCuenta);
    }

    @Override
    public void depositar(String numeroCuenta, Dinero monto) {
        ProductoFinanciero cuenta = buscarCuentaActivaPorNumero(numeroCuenta);
        
        // 1. Ejecutar Regla de Dominio: El método 'depositar' está en ProductoFinanciero
        cuenta.depositar(monto);
        
        // 2. Persistencia
        cuentaRepository.guardar(cuenta);
    }

    @Override
    public void retirar(String numeroCuenta, Dinero monto) {
        ProductoFinanciero cuenta = buscarCuentaActivaPorNumero(numeroCuenta);
        
        // 1. Ejecutar Regla de Dominio: El método 'retirar' está en CuentaAhorros/CuentaCorriente
        // La validación de saldo insuficiente ocurre dentro del método retirar específico.
        cuenta.retirar(monto);
        
        // 2. Persistencia
        cuentaRepository.guardar(cuenta);
    }

    @Override
    public void cancelarCuenta(String numeroCuenta) {
        ProductoFinanciero cuenta = buscarCuentaActivaPorNumero(numeroCuenta);
        
        // 1. Ejecutar Regla de Dominio: El método 'cancelar' está en CuentaAhorros/CuentaCorriente
        // La validación de Saldo Cero ocurre dentro del método cancelar específico.
        cuenta.cancelar();
        
        // 2. Persistencia
        cuentaRepository.guardar(cuenta);
    }
    
    @Override
    public ProductoFinanciero buscarCuentaPorNumero(String numeroCuenta) {
        return cuentaRepository.buscarPorNumero(numeroCuenta)
            .orElseThrow(() -> new IllegalArgumentException("Cuenta " + numeroCuenta + " no encontrada."));
    }
    
    // Método auxiliar común
    private ProductoFinanciero buscarCuentaActivaPorNumero(String numeroCuenta) {
         ProductoFinanciero cuenta = cuentaRepository.buscarPorNumero(numeroCuenta)
            .orElseThrow(() -> new IllegalArgumentException("Cuenta " + numeroCuenta + " no encontrada."));

        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalStateException("La cuenta no está activa para realizar esta operación.");
        }
        return cuenta;
    }
}