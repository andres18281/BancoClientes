package com.example.demo.infraestructura.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.demo.dominio.modelo.VO.Email;

import lombok.AllArgsConstructor;

/**
 * DTO para la modificación de la información básica de un cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteModificacionDTO {
    
 
    private String nombres;
    private String apellido;
    private Email correoElectronico;
    
    
}