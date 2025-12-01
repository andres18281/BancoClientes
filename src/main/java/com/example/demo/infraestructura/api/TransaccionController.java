package com.example.demo.infraestructura.api;


import com.example.demo.aplicacion.TransaccionService;
import com.example.demo.dominio.modelo.Transaccion;
import com.example.demo.dominio.modelo.VO.Dinero;

import com.example.demo.infraestructura.api.dto.TransaccionRespuestaDTO;
import com.example.demo.infraestructura.mappers.TransaccionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.demo.infraestructura.api.dto.TransaccionCreacionDTO;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final TransaccionMapper mapper;

    public TransaccionController(TransaccionService transaccionService, TransaccionMapper mapper) {
        this.transaccionService = transaccionService;
        this.mapper = mapper;
    } 

    /**
     * POST /api/v1/transacciones - Maneja Consignación, Retiro y Transferencia.
     */
    @PostMapping
    public ResponseEntity<?> realizarTransaccion(@Validated @RequestBody TransaccionCreacionDTO dto) {
        
        Dinero monto = Dinero.of(dto.getMonto());
        
        switch (dto.getTipoMovimiento().toUpperCase()) {
            case "CONSIGNACION":
                Transaccion consig = transaccionService.consignar(dto.getCuentaDestino(), monto);
                return new ResponseEntity<>(mapper.toRespuestaDTO(consig), HttpStatus.CREATED);

            case "RETIRO":
                Transaccion retiro = transaccionService.retirar(dto.getCuentaOrigen(), monto);
                return new ResponseEntity<>(mapper.toRespuestaDTO(retiro), HttpStatus.CREATED);

            case "TRANSFERENCIA":
                List<Transaccion> transferencias = transaccionService.transferir(
                    dto.getCuentaOrigen(), 
                    dto.getCuentaDestino(), 
                    monto
                );
                // Retorna la lista de los dos registros (débito y crédito)
                List<TransaccionRespuestaDTO> respuesta = transferencias.stream()
                    .map(mapper::toRespuestaDTO)
                    .collect(Collectors.toList());
                return new ResponseEntity<>(respuesta, HttpStatus.CREATED);

            default:
                return new ResponseEntity<>("Tipo de movimiento inválido.", HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * GET /api/v1/transacciones/historial/{numeroCuenta} - Consulta el historial.
     */
    @GetMapping("/historial/{numeroCuenta}")
    public ResponseEntity<List<TransaccionRespuestaDTO>> obtenerHistorial(@PathVariable String numeroCuenta) {
        List<Transaccion> historial = transaccionService.obtenerHistorial(numeroCuenta);
        
        List<TransaccionRespuestaDTO> respuesta = historial.stream()
            .map(mapper::toRespuestaDTO)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(respuesta);
    }
}