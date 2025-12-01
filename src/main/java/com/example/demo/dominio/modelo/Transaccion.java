package com.example.demo.dominio.modelo;


import java.time.LocalDateTime; 

import com.example.demo.dominio.modelo.VO.Dinero;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString

public class Transaccion {

    
    public enum TipoTransaccion {
        CONSIGNACION,
        RETIRO,
        TRANSFERENCIA_DEBITO,  
        TRANSFERENCIA_CREDITO   
    }

    protected Long id; 
    protected TipoTransaccion tipo;
    protected Dinero monto; 
    protected LocalDateTime fecha;
    protected String numeroCuentaOrigen;
    protected String numeroCuentaDestino;
    
    public Transaccion(
            Long id, 
            TipoTransaccion tipo, 
            Dinero monto, 
            LocalDateTime fecha, 
            String numeroCuentaOrigen, 
            String numeroCuentaDestino
        ) {
            this.id = id;
            this.tipo = tipo;
            this.monto = monto;
            this.fecha = fecha;
            this.numeroCuentaOrigen = numeroCuentaOrigen;
            this.numeroCuentaDestino = numeroCuentaDestino;
        }

       
        /**
         * Constructor para crear una transacción simple (consignación o retiro).
         * @param cuentaAfectada Es la cuenta de origen (retiro) o destino (consignación).
         */ 
        public Transaccion(TipoTransaccion tipo, Dinero monto, String cuentaAfectada) {
            this.id = null; 
            this.tipo = tipo;
            this.monto = monto;
            this.fecha = LocalDateTime.now();
            
            if (tipo == TipoTransaccion.RETIRO) {
                this.numeroCuentaOrigen = cuentaAfectada;
                this.numeroCuentaDestino = null;
            } else if (tipo == TipoTransaccion.CONSIGNACION) {
                this.numeroCuentaOrigen = null;
                this.numeroCuentaDestino = cuentaAfectada;
            } else {
               
                throw new IllegalArgumentException("Constructor inválido para transferencia. Use el constructor de dos cuentas.");
            }
        }

       
        /**
         * Constructor para crear una transacción de transferencia (débito o crédito).
         */
        public Transaccion(TipoTransaccion tipo, Dinero monto, String cuentaOrigen, String cuentaDestino) {
            
            if (!tipo.toString().startsWith("TRANSFERENCIA")) {
               
                throw new IllegalArgumentException("Constructor de transferencia solo para tipos TRANSFERENCIA_.");
            }
            
            this.id = null;
            this.tipo = tipo;
            this.monto = monto;
            this.fecha = LocalDateTime.now();
            this.numeroCuentaOrigen = cuentaOrigen;
            this.numeroCuentaDestino = cuentaDestino;
       }
    


}