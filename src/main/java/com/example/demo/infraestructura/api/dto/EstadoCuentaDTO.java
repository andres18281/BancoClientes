package com.example.demo.infraestructura.api.dto;

import lombok.Data;

@Data
public class EstadoCuentaDTO {
	private String nuevoEstado; // Esperado: "ACTIVA" o "INACTIVA"
}
