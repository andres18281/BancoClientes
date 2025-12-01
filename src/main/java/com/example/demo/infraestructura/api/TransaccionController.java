package com.example.demo.infraestructura.api;



import com.example.demo.dominio.modelo.Transaccion;
import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.dominio.port.in.GestionTransaccionPort;
import com.example.demo.infraestructura.api.dto.TransaccionRespuestaDTO;
import com.example.demo.infraestructura.mappers.TransaccionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.demo.infraestructura.api.dto.TransaccionCreacionDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/v1/transacciones")
public class TransaccionController {

    
    private final GestionTransaccionPort gestionTransaccionPort;
    private final TransaccionMapper mapper;

    
    public TransaccionController(GestionTransaccionPort gestionTransaccionPort, TransaccionMapper mapper) {
        this.gestionTransaccionPort = gestionTransaccionPort;
        this.mapper = mapper;
    }

    // ----------------------------------------------------------------------
    // 3. POST /api/v1/transacciones - Realizar Transacción (Consignación, Retiro, Transferencia)
    // ----------------------------------------------------------------------
    @Operation(
        summary = "Realiza una transacción financiera (Consignación, Retiro o Transferencia).",
        description = "Procesa el tipo de movimiento indicado en el DTO. Requiere cuentaOrigen y cuentaDestino solo para TRANSFERENCIA."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Transacción(es) registrada(s) con éxito.",
            content = @Content(schema = @Schema(oneOf = {TransaccionRespuestaDTO.class, List.class})) // Usa oneOf para List o Simple DTO
        ),
        @ApiResponse(responseCode = "400", description = "Tipo de movimiento o datos inválidos."),
        @ApiResponse(responseCode = "404", description = "Cuenta de origen o destino no encontrada."),
        @ApiResponse(responseCode = "409", description = "Conflicto (ej. saldo insuficiente para retiro o cuenta inactiva).")
    })
    @PostMapping
    public ResponseEntity<?> realizarTransaccion(
        @Validated 
        @RequestBody(description = "Detalles de la transacción: tipo, monto, cuentas.")
        TransaccionCreacionDTO dto) {
        
        Dinero monto = Dinero.of(dto.getMonto());
        
        switch (dto.getTipoMovimiento().toUpperCase()) {
            case "CONSIGNACION":
              
                Transaccion consig = gestionTransaccionPort.consignar(dto.getCuentaDestino(), monto);
                return new ResponseEntity<>(mapper.toRespuestaDTO(consig), HttpStatus.CREATED);

            case "RETIRO":
     
                Transaccion retiro = gestionTransaccionPort.retirar(dto.getCuentaOrigen(), monto);
                return new ResponseEntity<>(mapper.toRespuestaDTO(retiro), HttpStatus.CREATED);

            case "TRANSFERENCIA":
            
                List<Transaccion> transferencias = gestionTransaccionPort.transferir(
                    dto.getCuentaOrigen(), 
                    dto.getCuentaDestino(), 
                    monto
                );
                
                List<TransaccionRespuestaDTO> respuesta = transferencias.stream()
                    .map(mapper::toRespuestaDTO)
                    .collect(Collectors.toList());
                return new ResponseEntity<>(respuesta, HttpStatus.CREATED);

            default:
                return new ResponseEntity<>("Tipo de movimiento inválido.", HttpStatus.BAD_REQUEST);
        }
    }
    
    // ----------------------------------------------------------------------
    // 4. GET /api/v1/transacciones/historial/{numeroCuenta} - Consultar Historial
    // ----------------------------------------------------------------------
    @Operation(
        summary = "Consulta el historial de transacciones de una cuenta.",
        description = "Devuelve la lista completa de movimientos (débitos y créditos) asociados a un número de cuenta."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Consulta exitosa, devuelve la lista de transacciones.",
            content = @Content(schema = @Schema(implementation = TransaccionRespuestaDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada.")
    })
    @GetMapping("/historial/{numeroCuenta}")
    public ResponseEntity<List<TransaccionRespuestaDTO>> obtenerHistorial(
        @Parameter(description = "Número de cuenta para consultar su historial.")
        @PathVariable String numeroCuenta) {
        
        
        List<Transaccion> historial = gestionTransaccionPort.obtenerHistorial(numeroCuenta);
        
        List<TransaccionRespuestaDTO> respuesta = historial.stream()
            .map(mapper::toRespuestaDTO)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(respuesta);
    }
}