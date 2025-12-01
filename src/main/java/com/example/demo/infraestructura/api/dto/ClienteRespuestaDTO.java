package com.example.demo.infraestructura.api.dto;

import java.time.LocalDate; 
import java.time.LocalDateTime;
import lombok.Data;

@Data
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