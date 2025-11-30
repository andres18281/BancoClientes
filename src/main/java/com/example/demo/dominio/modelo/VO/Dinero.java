package com.example.demo.dominio.modelo.VO;



import java.math.BigDecimal;
import java.math.RoundingMode;



public final class Dinero {

    private final BigDecimal monto;
    private static final int DECIMAL_PLACES = 2; 

   
    private Dinero(BigDecimal monto) {
        
        this.monto = monto.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }
    
    
    public static Dinero of(BigDecimal monto) {
        return new Dinero(monto);
    }
    
    public static Dinero of(double monto) {
        return new Dinero(BigDecimal.valueOf(monto));
    }
    
   
    public Dinero() {
        this(BigDecimal.ZERO);
    }

    
    public Dinero sumar(Dinero otro) {
        return new Dinero(this.monto.add(otro.monto));
    }

    public Dinero restar(Dinero otro) {
        
        return new Dinero(this.monto.subtract(otro.monto));
    }

    public BigDecimal getMonto() {
        return monto;
    }
    
   
}