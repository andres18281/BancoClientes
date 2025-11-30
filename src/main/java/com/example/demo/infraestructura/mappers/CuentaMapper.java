package com.example.demo.infraestructura.mappers;

import org.springframework.stereotype.Component;

import com.example.demo.dominio.modelo.ProductoFinanciero;
import com.example.demo.infraestructura.api.dto.CuentaRespuestaDTO;

@Component
public class CuentaMapper {

    /**
     * Convierte una entidad de Dominio (ProductoFinanciero) a DTO de respuesta.
     */
    public CuentaRespuestaDTO toRespuestaDTO(ProductoFinanciero dominio) {
        CuentaRespuestaDTO dto = new CuentaRespuestaDTO();
        dto.setId(dominio.getId());
        dto.setClienteId(dominio.getClienteId());
        dto.setTipoCuenta(dominio.getTipoCuenta());
        dto.setNumeroCuenta(dominio.getNumeroCuenta());
        dto.setEstado(dominio.getEstado());
        
        // 🔑 Conversión segura del Value Object Dinero a BigDecimal
        dto.setSaldo(dominio.getSaldo().getMonto()); 
        
        dto.setFechaCreacion(dominio.getFechaCreacion());
        return dto;
    }
}