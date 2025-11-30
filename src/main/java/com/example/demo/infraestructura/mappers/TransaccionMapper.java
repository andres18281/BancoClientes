package com.example.demo.infraestructura.mappers;

import org.springframework.stereotype.Component;

import com.example.demo.dominio.modelo.Transaccion;
import com.example.demo.infraestructura.api.dto.TransaccionRespuestaDTO;

@Component
public class TransaccionMapper {

    /**
     * Convierte la Entidad de Dominio a DTO de respuesta.
     */
    public TransaccionRespuestaDTO toRespuestaDTO(Transaccion dominio) {
        TransaccionRespuestaDTO dto = new TransaccionRespuestaDTO();
        dto.setId(dominio.getId());
        dto.setTipo(dominio.getTipo().toString());
        dto.setMonto(dominio.getMonto().getMonto()); // Extrae BigDecimal del VO Dinero
        dto.setFecha(dominio.getFecha());
        dto.setNumeroCuentaOrigen(dominio.getNumeroCuentaOrigen());
        dto.setNumeroCuentaDestino(dominio.getNumeroCuentaDestino());
        return dto;
    }
    
   
}