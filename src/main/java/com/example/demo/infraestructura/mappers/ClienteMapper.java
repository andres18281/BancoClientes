package com.example.demo.infraestructura.mappers;

import org.springframework.stereotype.Component;

import com.example.demo.dominio.modelo.Cliente;
import com.example.demo.dominio.modelo.VO.Email;
import com.example.demo.infraestructura.api.dto.ClienteCreacionDTO;
import com.example.demo.infraestructura.api.dto.ClienteRespuestaDTO;





@Component
public class ClienteMapper {

    /**
     * Convierte DTO de creación a Entidad de Dominio (Cliente).
     */
    public Cliente toDominio(ClienteCreacionDTO dto) {
        
        return new Cliente(
            dto.getTipoIdentificacion(),
            dto.getNumeroIdentificacion(),
            dto.getNombres(),
            dto.getApellido(),
            new Email(dto.getCorreoElectronico()), 
            dto.getFechaNacimiento()
        );
    }

    /**
     * Convierte Entidad de Dominio (Cliente) a DTO de respuesta.
     */
    public ClienteRespuestaDTO toRespuestaDTO(Cliente dominio) {
        ClienteRespuestaDTO dto = new ClienteRespuestaDTO();
        dto.setId(dominio.getId());
        dto.setTipoIdentificacion(dominio.getTipoIdentificacion());
        dto.setNumeroIdentificacion(dominio.getNumeroIdentificacion());
        dto.setNombres(dominio.getNombres());
        dto.setApellido(dominio.getApellido());
        dto.setCorreoElectronico(dominio.getCorreoElectronico().getDireccion()); // 🔑 Extrae la String del VO
        dto.setFechaNacimiento(dominio.getFechaNacimiento());
        dto.setFechaCreacion(dominio.getFechaCreacion());
        return dto;
    }
}