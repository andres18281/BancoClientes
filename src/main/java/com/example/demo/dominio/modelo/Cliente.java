package com.example.demo.dominio.modelo;

import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.dominio.modelo.VO.Email;

import java.time.LocalDateTime;

import java.time.LocalDate;
import java.time.Period;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
 
@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class Cliente {

    private Long id;
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String nombres;
    private String apellido;
    private Email correoElectronico; // Value Object
    private LocalDate fechaNacimiento;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    
    public Cliente(String tipoIdentificacion, String numeroIdentificacion, String nombres, String apellido, Email correoElectronico, LocalDate fechaNacimiento) {
        this.tipoIdentificacion = tipoIdentificacion;
        this.numeroIdentificacion = numeroIdentificacion;
        this.nombres = nombres;
        this.apellido = apellido;
        this.correoElectronico = correoElectronico;
        this.fechaNacimiento = fechaNacimiento;
        this.marcarComoCreado(); 
    }

    
    public boolean esMayorDeEdad() {
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears() >= 18;
    }

    
    public void validarDatos() {
        if (nombres.length() < 2 || apellido.length() < 2) {
            throw new IllegalArgumentException("Nombre y apellido deben tener al menos 2 caracteres.");
        }
    }
    
    
    public void marcarComoCreado() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }
    
    
    public void marcarComoModificado() {
        this.fechaModificacion = LocalDateTime.now();
    }
}