package com.example.demo.infraestructura.api.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class ClienteCreacionDTO {
    
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String nombres;
    private String apellido;
    private String correoElectronico; // Se recibe como String, el Mapper lo convertirá a Email VO
    private LocalDate fechaNacimiento;
}