package com.example.demo.dominio.modelo;

import com.example.demo.dominio.modelo.VO.Dinero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
// Es crucial para que Lombok incluya los campos de ProductoFinanciero en equals/hashCode
@EqualsAndHashCode(callSuper = true) 
public class CuentaAhorros extends ProductoFinanciero {
    
    private static final String PREFIJO_CUENTA_AHORROS = "53";
    private static final int LONGITUD_CUERPO = 8;
    private static final int LIMITE_MAXIMO_CUERPO = 100000000; // 10^8

    
    
    public CuentaAhorros(
        Long clienteId, 
        Long id, 
        String numeroCuenta, 
        Dinero saldo, 
        EstadoCuenta estado, 
        LocalDateTime fechaCreacion, 
        LocalDateTime fechaModificacion, 
        boolean exentaGMF
    ) {
        super(clienteId); 
        
        
        this.id = id;
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
        this.estado = estado;
        this.tipoCuenta = TipoCuenta.AHORROS; 
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
        this.exentaGMF = exentaGMF;
    }

    
    public CuentaAhorros(Long clienteId) {
        super(clienteId); 
        
        this.tipoCuenta = TipoCuenta.AHORROS;
        this.estado = EstadoCuenta.ACTIVA; 
        this.numeroCuenta = generarNumeroCuenta(PREFIJO_CUENTA_AHORROS); 
    }
    
    @Override
    public void retirar(Dinero monto) {
        Dinero nuevoSaldo = this.saldo.restar(monto);
        if (nuevoSaldo.getMonto().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Saldo insuficiente. La cuenta de ahorros no puede quedar en negativo.");
        }
        this.saldo = this.saldo.restar(monto);
        this.fechaModificacion = LocalDateTime.now();
    }
    
    @Override
    public void cancelar() {
        if (this.saldo.getMonto().compareTo(BigDecimal.ZERO) != 0) {
             throw new IllegalStateException("Solo se pueden cancelar cuentas de ahorro con saldo en $0.");
        }
        this.estado = EstadoCuenta.CANCELADA;
        this.fechaModificacion = LocalDateTime.now();
    }
    
    private String generarNumeroCuenta(String prefijo) {
        String num = String.format("%0" + LONGITUD_CUERPO + "d", ThreadLocalRandom.current().nextInt(LIMITE_MAXIMO_CUERPO));
        return prefijo + num;
    }

	@Override
	public String getTipoProducto() {
		
		return "AHORROS";
	}
    
}