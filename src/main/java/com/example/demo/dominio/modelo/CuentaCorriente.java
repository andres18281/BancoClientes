package com.example.demo.dominio.modelo;

import com.example.demo.dominio.modelo.VO.Dinero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CuentaCorriente extends ProductoFinanciero {
    
    private static final String PREFIJO_CUENTA_CORRIENTE = "33";
    private static final int LONGITUD_CUERPO = 8;
    private static final int LIMITE_MAXIMO_CUERPO = 100000000;

    public CuentaCorriente(
            Long clienteId, 
            Long id, 
            String numeroCuenta, 
            Dinero saldo, 
            EstadoCuenta estado, 
            LocalDateTime fechaCreacion, 
            LocalDateTime fechaModificacion, 
            boolean exentaGMF
        ) {
            super(clienteId); // Llama al constructor base para inicializar clienteId
            
            // Asignación de todos los campos persistidos (si son 'protected' en ProductoFinanciero)
            this.id = id;
            this.numeroCuenta = numeroCuenta;
            this.saldo = saldo;
            this.estado = estado;
            this.tipoCuenta = TipoCuenta.CORRIENTE; // Aseguramos el tipo
            this.fechaCreacion = fechaCreacion;
            this.fechaModificacion = fechaModificacion;
            this.exentaGMF = exentaGMF;
        }

        // Constructor de creación (el que ya tenías)
        public CuentaCorriente(Long clienteId) {
            super(clienteId);
            this.tipoCuenta = TipoCuenta.CORRIENTE;
            this.estado = EstadoCuenta.ACTIVA; 
            this.numeroCuenta = generarNumeroCuenta(PREFIJO_CUENTA_CORRIENTE); 
        }

 
    @Override
    public void retirar(Dinero monto) {
        
        this.saldo = this.saldo.restar(monto); 
        this.fechaModificacion = LocalDateTime.now();
    }
    
    
    @Override
    public void cancelar() {
        if (this.saldo.getMonto().compareTo(BigDecimal.ZERO) != 0) {
             throw new IllegalStateException("Solo se pueden cancelar cuentas corrientes con saldo en $0.");
        }
        this.estado = EstadoCuenta.CANCELADA;
        this.fechaModificacion = LocalDateTime.now();
    }

    
    private String generarNumeroCuenta(String prefijo) {
        String num = String.format("%0" + LONGITUD_CUERPO + "d", ThreadLocalRandom.current().nextInt(LIMITE_MAXIMO_CUERPO));
        return prefijo + num;
    }
}
