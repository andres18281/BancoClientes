package com.example.demo.infraestructura.api;

import com.example.demo.aplicacion.ClienteService;

import com.example.demo.dominio.modelo.Cliente;
import com.example.demo.dominio.port.in.GestionClientePort;
import com.example.demo.infraestructura.api.dto.ClienteCreacionDTO;
import com.example.demo.infraestructura.api.dto.ClienteModificacionDTO;
import com.example.demo.infraestructura.api.dto.ClienteRespuestaDTO;
import com.example.demo.infraestructura.mappers.ClienteMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final GestionClientePort gestionClientePort; 
    private final ClienteMapper mapper;

    // Constructor...
    public ClienteController(GestionClientePort gestionClientePort, ClienteMapper mapper) {
        this.gestionClientePort = gestionClientePort; 
        this.mapper = mapper;
    }

    // ----------------------------------------------------------------------
    // 1. POST /api/v1/clientes - Crear Cliente
    // ----------------------------------------------------------------------
    @Operation(
        summary = "Crea un nuevo cliente en el sistema.",
        description = "Registra un nuevo cliente con todos sus datos obligatorios."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Cliente creado exitosamente. Retorna el cliente registrado.",
            content = @Content(schema = @Schema(implementation = ClienteRespuestaDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos faltantes o DTO incorrecto)."),
        @ApiResponse(responseCode = "409", description = "Conflicto (ej. el cliente ya existe).")
    })
    @PostMapping
    public ResponseEntity<ClienteRespuestaDTO> crearCliente(
        @Validated 
        @RequestBody(description = "Datos del cliente a crear.")
        ClienteCreacionDTO dto) {
        
        Cliente cliente = mapper.toDominio(dto);
        Cliente clienteGuardado = gestionClientePort.crearCliente(cliente);
        ClienteRespuestaDTO respuesta = mapper.toRespuestaDTO(clienteGuardado);
        
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    // ----------------------------------------------------------------------
    // 2. GET /api/v1/clientes/{id} - Consultar Cliente por ID
    // ----------------------------------------------------------------------
    @Operation(
        summary = "Obtiene los detalles de un cliente por su ID.",
        description = "Busca un cliente específico y devuelve su detalle."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Consulta exitosa, devuelve el detalle del cliente.",
            content = @Content(schema = @Schema(implementation = ClienteRespuestaDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteRespuestaDTO> buscarClientePorId(
        @Parameter(description = "ID único del cliente.")
        @PathVariable Long id) {
        
        Optional<Cliente> cliente = gestionClientePort.buscarClientePorId(id);
        
        if (cliente.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ClienteRespuestaDTO respuesta = mapper.toRespuestaDTO(cliente.get());
        return ResponseEntity.ok(respuesta);
    }

    // ----------------------------------------------------------------------
    // 3. PUT /api/v1/clientes/{id} - Modificar Cliente (Actualización Total)
    // ----------------------------------------------------------------------
    @Operation(
        summary = "Actualiza todos los datos de un cliente existente.",
        description = "Reemplaza completamente la información de un cliente dado su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Cliente modificado exitosamente. Retorna la nueva información.",
            content = @Content(schema = @Schema(implementation = ClienteRespuestaDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida."),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteRespuestaDTO> modificarCliente(
        @Parameter(description = "ID del cliente a modificar.")
        @PathVariable Long id, 
        @RequestBody(description = "Nuevos datos completos del cliente.")
        ClienteModificacionDTO dto) {
        
        Cliente clienteModificado = gestionClientePort.actualizarCliente(id, dto);
        ClienteRespuestaDTO respuestaDTO = mapper.toRespuestaDTO(clienteModificado);
        
        return ResponseEntity.ok(respuestaDTO);
    }

    // ----------------------------------------------------------------------
    // 4. DELETE /api/v1/clientes/{id} - Eliminar Lógicamente
    // ----------------------------------------------------------------------
    @Operation(
        summary = "Elimina lógicamente un cliente por ID.",
        description = "Marca el cliente como eliminado o inactivo en el sistema. No se permite la eliminación física de registros."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente eliminado lógicamente con éxito."),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(
        @Parameter(description = "ID único del cliente a eliminar.")
        @PathVariable Long id) {
        
        gestionClientePort.eliminarCliente(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
