package com.example.demo.infraestructura.api;

import com.example.demo.aplicacion.CuentaService;

import com.example.demo.dominio.modelo.ProductoFinanciero;
import com.example.demo.dominio.modelo.ProductoFinanciero.TipoCuenta;
import com.example.demo.infraestructura.api.dto.CuentaCreacionDTO;
import com.example.demo.infraestructura.api.dto.CuentaRespuestaDTO;
import com.example.demo.infraestructura.mappers.CuentaMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoFinancieroController {

    private final CuentaService cuentaService;
    private final CuentaMapper mapper;

    public ProductoFinancieroController(CuentaService cuentaService, CuentaMapper mapper) {
        this.cuentaService = cuentaService;
        this.mapper = mapper;
    }

    /**
     * POST /api/v1/productos - Crear una nueva cuenta (Ahorros o Corriente).
     */
    @PostMapping
    public ResponseEntity<CuentaRespuestaDTO> crearCuenta(@Validated @RequestBody CuentaCreacionDTO dto) {
        
    	TipoCuenta tipo = TipoCuenta.valueOf(dto.getTipoCuenta().toUpperCase());	
        
        // 1. Ejecutar el Use Case para crear la cuenta
        ProductoFinanciero nuevaCuenta = cuentaService.crearCuenta(
            dto.getClienteId(), 
            tipo
        );
        
        // 2. Mapear a DTO de respuesta
        CuentaRespuestaDTO respuesta = mapper.toRespuestaDTO(nuevaCuenta);
        
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/productos/{numeroCuenta} - Consultar saldo y estado de la cuenta.
     */
    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaRespuestaDTO> consultarCuenta(@PathVariable String numeroCuenta) {
    	Optional<ProductoFinanciero> cuenta = cuentaService.buscarCuentaPorNumero(numeroCuenta);

        if (cuenta.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CuentaRespuestaDTO respuesta = mapper.toRespuestaDTO(cuenta.get());
        return ResponseEntity.ok(respuesta);
    }
    
    // Aquí irían endpoints para cambiar estado (inactivar/activar) y cancelar.
}