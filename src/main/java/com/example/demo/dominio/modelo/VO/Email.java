package com.example.demo.dominio.modelo.VO;



import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class Email {

    private final String direccion;
    
    
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    
    public Email(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección de correo electrónico no puede ser nula o vacía.");
        }
        if (!isValid(direccion)) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido: " + direccion);
        }
        this.direccion = direccion.toLowerCase(); 
    }
    
    
    private boolean isValid(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.matches();
    }

    
    public String getDireccion() {
        return direccion;
    }

    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        
        return Objects.equals(direccion, email.direccion); 
    }

    @Override
    public int hashCode() {
        return Objects.hash(direccion);
    }

    @Override
    public String toString() {
        return direccion;
    }
}