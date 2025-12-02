package com.example.demo.infraestructura.api.dto;

import java.time.LocalDate; 
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRespuestaDTO {
	    private Long id;
	    private String tipoIdentificacion;
	    private String numeroIdentificacion;
	    private String nombres;
	    private String apellido;
	    private String correoElectronico;
	    private LocalDate fechaNacimiento;
	    private LocalDateTime fechaCreacion;
} 