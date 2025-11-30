package com.example.demo.dominio.modelo;

import com.example.demo.dominio.modelo.VO.Dinero;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder; 

@Data 
@NoArgsConstructor 
public abstract class ProductoFinanciero {

    public enum TipoCuenta { AHORROS, CORRIENTE }
    public enum EstadoCuenta { ACTIVA, INACTIVA, CANCELADA }

    private Long id;
    protected TipoCuenta tipoCuenta;
    protected String numeroCuenta;
    protected EstadoCuenta estado;
    protected Dinero saldo; 
    private boolean exentaGMF;
    protected LocalDateTime fechaCreacion;
    protected LocalDateTime fechaModificacion;
    protected Long clienteId; 

    
    public ProductoFinanciero(Long clienteId) {
        this.clienteId = clienteId;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoCuenta.ACTIVA; 
        this.saldo = new Dinero(); 
        this.fechaModificacion = LocalDateTime.now();
    }
    
   

    public void depositar(Dinero monto) {
        this.saldo = this.saldo.sumar(monto);
        this.fechaModificacion = LocalDateTime.now();
    }
    
    public void cambiarEstado(EstadoCuenta nuevoEstado) {
        if (nuevoEstado == EstadoCuenta.CANCELADA) {
             throw new IllegalArgumentException("Use el método 'cancelar()' para cancelar la cuenta.");
        }
        this.estado = nuevoEstado;
        this.fechaModificacion = LocalDateTime.now();
    }

    
    public abstract void retirar(Dinero monto);
    public abstract void cancelar();
}