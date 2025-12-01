package com.example.demo.infraestructura.api;

import com.example.demo.aplicacion.ClienteService;

import com.example.demo.dominio.modelo.Cliente;
import com.example.demo.infraestructura.api.dto.ClienteCreacionDTO;
import com.example.demo.infraestructura.api.dto.ClienteModificacionDTO;
import com.example.demo.infraestructura.api.dto.ClienteRespuestaDTO;
import com.example.demo.infraestructura.mappers.ClienteMapper;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

	private final ClienteService clienteService; 
    private final ClienteMapper mapper;

    public ClienteController(ClienteService clienteService, ClienteMapper mapper) {
        this.clienteService = clienteService;
        this.mapper = mapper;
    }

    /**
     * POST /api/v1/clientes - Crear un nuevo cliente.
     */
    @PostMapping
    public ResponseEntity<ClienteRespuestaDTO> crearCliente(@Validated @RequestBody ClienteCreacionDTO dto) {
        
        // 1. Mapear DTO de entrada a la Entidad de Dominio (o Use Case DTO si existiera)
        Cliente cliente = mapper.toDominio(dto); // Asumiendo que existe este mapeo en ClienteMapper
        
        // 2. Ejecutar el Use Case
        Cliente clienteGuardado = clienteService.crearCliente(cliente);
        
        // 3. Mapear la Entidad de Dominio a DTO de respuesta
        ClienteRespuestaDTO respuesta = mapper.toRespuestaDTO(clienteGuardado);
        
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/clientes/{id} - Obtener un cliente por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteRespuestaDTO> buscarClientePorId(@PathVariable Long id) {
    	Optional<Cliente> cliente = clienteService.buscarClientePorId(id); 
        if (cliente.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ClienteRespuestaDTO respuesta = mapper.toRespuestaDTO(cliente.get());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * DELETE /api/v1/clientes/{id} - Eliminar lógicamente un cliente.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @PutMapping("/{id}")
   
    public ResponseEntity<ClienteRespuestaDTO> modificarCliente(
            @PathVariable Long id, 
            @RequestBody ClienteModificacionDTO dto) {
        
        Cliente clienteModificado = clienteService.actualizarCliente(id, dto);
        ClienteRespuestaDTO respuestaDTO = mapper.toRespuestaDTO(clienteModificado);
       
        return ResponseEntity.ok(respuestaDTO);
    }
}
