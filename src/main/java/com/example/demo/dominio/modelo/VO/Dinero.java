package com.example.demo.dominio.modelo.VO;



import java.math.BigDecimal;
import java.math.RoundingMode;

 

public final class Dinero {

    private final BigDecimal monto;
    private static final int DECIMAL_PLACES = 2; 

   
    private Dinero(BigDecimal monto) {
        
        this.monto = monto.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }
    
    
    public boolean esCero() {
        return this.monto.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public static Dinero of(BigDecimal monto) {
    	if (monto == null) {
            // Lanza la excepción si es null. Esto es la primera línea de defensa
            // si la validación del DTO (@NotNull) falla por alguna razón.
            throw new IllegalArgumentException("El monto base para Dinero no puede ser nulo."); 
        }
        return new Dinero(monto); // Llama al constructor privado
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