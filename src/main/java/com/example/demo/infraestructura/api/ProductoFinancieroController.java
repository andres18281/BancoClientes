package com.example.demo.infraestructura.api;


import com.example.demo.dominio.port.in.GestionCuentaPort; // ⬅️ ¡IMPORTANTE! Importar el Puerto
import com.example.demo.infraestructura.api.dto.CuentaCreacionDTO;
import com.example.demo.infraestructura.api.dto.CuentaRespuestaDTO;
import com.example.demo.infraestructura.api.dto.DepositoDTO;
import com.example.demo.infraestructura.api.dto.EstadoCuentaDTO;
import com.example.demo.dominio.modelo.ProductoFinanciero;
import com.example.demo.dominio.modelo.ProductoFinanciero.TipoCuenta;
import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.infraestructura.mappers.CuentaMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;


import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoFinancieroController {

    // 1. 🔑 Depender de la Interfaz/Puerto, no de la implementación concreta (CuentaService)
    private final GestionCuentaPort gestionCuentaPort; 
    private final CuentaMapper mapper;

    // 2. 🔑 Constructor: Inyectar el Puerto
    public ProductoFinancieroController(GestionCuentaPort gestionCuentaPort, CuentaMapper mapper) {
        this.gestionCuentaPort = gestionCuentaPort;
        this.mapper = mapper;
    }

    // ----------------------------------------------------------------------
    // POST /api/v1/productos - Crear una nueva cuenta
    // ----------------------------------------------------------------------
    @Operation(
    	    summary = "Crea un nuevo producto financiero (Cuenta de Ahorros o Corriente).",
    	    description = "Registra una nueva cuenta a nombre de un cliente existente, inicializándola con estado ACTIVA y saldo cero."
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(
    	        responseCode = "201", 
    	        description = "Cuenta creada exitosamente. Retorna los detalles del producto financiero.",
    	        content = @Content(schema = @Schema(implementation = CuentaRespuestaDTO.class))
    	    ),
    	    @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. tipo de cuenta no soportado o DTO inválido)."),
    	    @ApiResponse(responseCode = "404", description = "El Cliente ID proporcionado no existe.")
    	})
    @PostMapping
    public ResponseEntity<CuentaRespuestaDTO> crearCuenta(@Validated @RequestBody CuentaCreacionDTO dto) {
        

    	TipoCuenta tipo = TipoCuenta.valueOf(dto.getTipoCuenta().toUpperCase());	
        
        ProductoFinanciero nuevaCuenta = gestionCuentaPort.crearCuenta( 
            dto.getClienteId(),	
            tipo
        );
        
        CuentaRespuestaDTO respuesta = mapper.toRespuestaDTO(nuevaCuenta);
        
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    // ----------------------------------------------------------------------
    // GET /api/v1/productos/{numeroCuenta} - Consultar cuenta
    // ----------------------------------------------------------------------
    @Operation(
    	    summary = "Consulta el estado y saldo de una cuenta por su número.",
    	    description = "Busca un producto financiero (cuenta) específico y devuelve su detalle. Si la cuenta no existe, retorna 404."
    	)
    @ApiResponses(value = {
     @ApiResponse(responseCode = "200", description = "Consulta exitosa, devuelve el detalle de la cuenta."),
     @ApiResponse(responseCode = "404", description = "Cuenta no encontrada.")
    })
    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaRespuestaDTO> consultarCuenta(@PathVariable String numeroCuenta) {
    	
    	Optional<ProductoFinanciero> cuenta = gestionCuentaPort.buscarCuentaPorNumero(numeroCuenta); 

        if (cuenta.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CuentaRespuestaDTO respuesta = mapper.toRespuestaDTO(cuenta.get());
        return ResponseEntity.ok(respuesta);
    }
    
    // ----------------------------------------------------------------------
    // PATCH /api/v1/productos/{numeroCuenta}/estado - Actualizar estado
    // ----------------------------------------------------------------------
    @Operation(
    	    summary = "Actualiza el estado operativo de una cuenta (ACTIVA o INACTIVA).",
    	    description = "Cambia el estado de una cuenta existente. Una cuenta CANCELADA no puede ser reactivada. Se usa PATCH porque es una modificación parcial del recurso."
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(
    	        responseCode = "200", 
    	        description = "Estado de la cuenta actualizado exitosamente. Retorna la cuenta modificada.",
    	        content = @Content(schema = @Schema(implementation = CuentaRespuestaDTO.class))
    	    ),
    	    @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. el nuevo estado proporcionado no es 'ACTIVA' o 'INACTIVA')."),
    	    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada."),
    	    @ApiResponse(responseCode = "409", description = "Conflicto de estado (ej. intentar activar una cuenta CANCELADA).")
    	})
    @PatchMapping("/{numeroCuenta}/estado")
    public ResponseEntity<CuentaRespuestaDTO> actualizarEstado(
            @PathVariable String numeroCuenta,
            @RequestBody EstadoCuentaDTO dto) {
        

        ProductoFinanciero cuentaModificada = gestionCuentaPort.actualizarEstadoCuenta( 
            numeroCuenta, 
            dto.getNuevoEstado()
        );
        
        CuentaRespuestaDTO respuestaDTO = mapper.toRespuestaDTO(cuentaModificada); 
        
        return ResponseEntity.ok(respuestaDTO);
    }
    
    @Operation(
    	    summary = "Cancela un producto financiero.",
    	    description = "Pone el estado de la cuenta a CANCELADA. Esta acción solo es posible si el **saldo actual de la cuenta es igual a $0**. Si la cancelación es exitosa, se devuelve 204 No Content."
    	    
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(responseCode = "204", description = "Cancelación exitosa. La cuenta está marcada como CANCELADA."),
    	    @ApiResponse(responseCode = "400", description = "Solicitud inválida."),
    	    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada."),
    	    @ApiResponse(responseCode = "409", description = "Conflicto de estado (ej. la cuenta tiene saldo distinto a $0 o ya está cancelada).")
    	})
    @DeleteMapping("/{numeroCuenta}/cancelar") 
    public ResponseEntity<Void> cancelarCuenta(@PathVariable String numeroCuenta) {
        
        gestionCuentaPort.cancelarCuenta(numeroCuenta);
        
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
    	    summary = "Registra una consignación (depósito) a una cuenta.",
    	    description = "Añade un monto al saldo de una cuenta. La cuenta debe estar en estado **ACTIVA** para recibir fondos. Si la operación es exitosa, devuelve 204."
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(responseCode = "204", description = "Consignación exitosa. El saldo de la cuenta ha sido actualizado."),
    	    @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. monto negativo, DTO incorrecto)."),
    	    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada."),
    	    @ApiResponse(responseCode = "409", description = "Conflicto de estado (ej. la cuenta existe, pero no está ACTIVA y no puede recibir depósitos).")
    	})
    @PostMapping("/depositar")
    public ResponseEntity<Void> consignar(@Validated @RequestBody DepositoDTO dto) {
        

        Dinero montoVO = Dinero.of(dto.getMonto());
        gestionCuentaPort.depositar(dto.getNumeroCuenta(), montoVO);

        return ResponseEntity.noContent().build();
    }
}