package com.example.demo.infraestructura.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;




import com.example.demo.dominio.modelo.Transaccion;
import com.example.demo.dominio.modelo.Transaccion.TipoTransaccion;
import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.dominio.port.in.GestionCuentaPort;
import com.example.demo.dominio.port.in.GestionTransaccionPort;
import com.example.demo.infraestructura.api.dto.TransaccionCreacionDTO;
import com.example.demo.infraestructura.api.dto.TransaccionRespuestaDTO;
import com.example.demo.infraestructura.mappers.TransaccionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransaccionController.class)
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GestionTransaccionPort gestionTransaccionPort;

    @MockBean
    private TransaccionMapper mapper;
    
    

    // ----------------------------------------------------------
    // TEST: CONSIGNACIÓN
    // ----------------------------------------------------------
    @Test
    void testRealizarTransaccion_consignacion() throws Exception {
        // Preparación de datos
        BigDecimal montoValor = BigDecimal.valueOf(100);
        
        TransaccionCreacionDTO dto = new TransaccionCreacionDTO();
        dto.setTipoMovimiento("CONSIGNACION");
        dto.setCuentaDestino("540000005");
        dto.setMonto(montoValor);

        // Objeto de dominio que se espera que devuelva el puerto
        Transaccion trans = new Transaccion(
            TipoTransaccion.CONSIGNACION, 
            Dinero.of(montoValor), 
            "540000005"
        );
        trans.setId(2L);
        trans.setFecha(LocalDateTime.now());

        // DTO de respuesta que se espera del mapper
        TransaccionRespuestaDTO respuesta = new TransaccionRespuestaDTO();
        respuesta.setNumeroCuentaDestino("540000005");
        respuesta.setTipo("CREDITO"); 
        respuesta.setMonto(montoValor);
        
        // Simulación del comportamiento (Stubbing)
        when(gestionTransaccionPort.consignar(eq("540000005"), any(Dinero.class)))
            .thenReturn(trans);
        when(mapper.toRespuestaDTO(any(Transaccion.class))).thenReturn(respuesta);

        // Ejecución y Verificación
        mockMvc.perform(post("/api/v1/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated()) // Espera HTTP 201
                .andExpect(jsonPath("$.tipo").value("CREDITO"))
                .andExpect(jsonPath("$.numeroCuentaDestino").value("540000005"))
                .andExpect(jsonPath("$.monto").value(100));
    }

    // ----------------------------------------------------------
    // TEST: RETIRO
    // ----------------------------------------------------------

   
    @Test
    void testRealizarTransaccion_retiro() throws Exception {
        // Preparación de datos
        BigDecimal montoValor = BigDecimal.valueOf(50);
        Dinero monto = Dinero.of(montoValor);

        TransaccionCreacionDTO dto = new TransaccionCreacionDTO();
        dto.setTipoMovimiento("RETIRO");
        dto.setCuentaOrigen("530000001");
        dto.setMonto(montoValor);

        // Objeto de dominio que se espera que devuelva el puerto
        Transaccion transRetiro = new Transaccion(
            TipoTransaccion.RETIRO,
            monto,
            "530000001"
        );
        // Forzar ID y fecha para que el DTO mapee bien en la respuesta
        transRetiro.setId(1L);
        transRetiro.setFecha(LocalDateTime.now());

        // DTO de respuesta que se espera del mapper
        TransaccionRespuestaDTO respuesta = new TransaccionRespuestaDTO();
        respuesta.setNumeroCuentaOrigen("530000001"); // La cuenta afectada es la de origen
        respuesta.setTipo("DEBITO");
        respuesta.setMonto(montoValor);

        // Simulación del comportamiento (Stubbing)
        when(gestionTransaccionPort.retirar(eq("530000001"), any(Dinero.class)))
            .thenReturn(transRetiro);
        when(mapper.toRespuestaDTO(any(Transaccion.class))).thenReturn(respuesta);

        // Ejecución y Verificación
        mockMvc.perform(post("/api/v1/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated()) // Espera HTTP 201
                .andExpect(jsonPath("$.tipo").value("DEBITO"))
                .andExpect(jsonPath("$.numeroCuentaOrigen").value("530000001"))
                .andExpect(jsonPath("$.monto").value(50));
    }
    // ----------------------------------------------------------
    // TEST: TRANSFERENCIA (retorno es una lista)
    // ----------------------------------------------------------
    @Test
    void testRealizarTransaccion_transferencia() throws Exception {
        // --- PREPARACIÓN DE DATOS DE ENTRADA ---
        BigDecimal montoValor = BigDecimal.valueOf(200);
        Dinero monto = Dinero.of(montoValor);
        String cuentaOrigen = "530000001";
        String cuentaDestino = "540000005";

        TransaccionCreacionDTO dto = new TransaccionCreacionDTO();
        dto.setTipoMovimiento("TRANSFERENCIA");
        dto.setCuentaOrigen(cuentaOrigen);
        dto.setCuentaDestino(cuentaDestino);
        dto.setMonto(montoValor);

        // --- OBJETOS DE DOMINIO QUE DEVUELVE EL PUERTO (Transferir devuelve una lista) ---
        // Transacción 1: Débito de la cuenta de origen
        Transaccion trans1Debito = new Transaccion(
                TipoTransaccion.TRANSFERENCIA_DEBITO, 
                monto, 
                cuentaOrigen, 
                cuentaDestino // Constructor de transferencia debe manejar ambas cuentas
        );
        trans1Debito.setId(3L);
        trans1Debito.setFecha(LocalDateTime.now());

        // Transacción 2: Crédito a la cuenta de destino
        Transaccion trans2Credito = new Transaccion(
                TipoTransaccion.TRANSFERENCIA_CREDITO, 
                monto, 
                cuentaDestino, 
                cuentaOrigen // Constructor de transferencia debe manejar ambas cuentas
        );
        trans2Credito.setId(4L);
        trans2Credito.setFecha(LocalDateTime.now());

        List<Transaccion> movimientos = Arrays.asList(trans1Debito, trans2Credito);

        // --- DTOS DE RESPUESTA QUE DEVUELVE EL MAPPER ---
        // Respuesta 1: Débito (Cuenta Origen Afectada)
        TransaccionRespuestaDTO resp1 = new TransaccionRespuestaDTO();
        resp1.setNumeroCuentaOrigen(cuentaOrigen); // Cuenta de origen del débito
        resp1.setNumeroCuentaDestino(cuentaDestino); // Cuenta destino (se envía también)
        resp1.setTipo("DEBITO");
        resp1.setMonto(montoValor);

        // Respuesta 2: Crédito (Cuenta Destino Afectada)
        TransaccionRespuestaDTO resp2 = new TransaccionRespuestaDTO();
        resp2.setNumeroCuentaOrigen(cuentaOrigen); // Cuenta origen (se envía también)
        resp2.setNumeroCuentaDestino(cuentaDestino); // Cuenta destino del crédito
        resp2.setTipo("CREDITO");
        resp2.setMonto(montoValor);

        // La lista de DTOS que el controlador devolverá es: [resp1, resp2]
        List<TransaccionRespuestaDTO> listaRespuestas = Arrays.asList(resp1, resp2);


        // --- SIMULACIÓN DEL COMPORTAMIENTO (Stubbing) ---
        // Simula la llamada al puerto
        when(gestionTransaccionPort.transferir(
                eq(cuentaOrigen), eq(cuentaDestino), any(Dinero.class)))
                .thenReturn(movimientos);

        // Simula el mapeo de cada transacción a su DTO correspondiente
        when(mapper.toRespuestaDTO(trans1Debito)).thenReturn(resp1);
        when(mapper.toRespuestaDTO(trans2Credito)).thenReturn(resp2);


        // --- EJECUCIÓN Y VERIFICACIÓN ---
        mockMvc.perform(post("/api/v1/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated()) 
                
                // Verifica que la respuesta es una LISTA (por eso empieza con $)
                
                // Verifica el primer elemento (Débito)
                .andExpect(jsonPath("$[0].tipo").value("DEBITO"))
                .andExpect(jsonPath("$[0].monto").value(200))
                .andExpect(jsonPath("$[0].numeroCuentaOrigen").value(cuentaOrigen))
                .andExpect(jsonPath("$[0].numeroCuentaDestino").value(cuentaDestino))

                // Verifica el segundo elemento (Crédito)
                .andExpect(jsonPath("$[1].tipo").value("CREDITO"))
                .andExpect(jsonPath("$[1].monto").value(200))
                .andExpect(jsonPath("$[1].numeroCuentaOrigen").value(cuentaOrigen))
                .andExpect(jsonPath("$[1].numeroCuentaDestino").value(cuentaDestino));
    }

    // ----------------------------------------------------------
    // TEST: HISTORIAL
    // ----------------------------------------------------------
    @Test
    void testObtenerHistorial() throws Exception {
        String numeroCuenta = "530000001";
        BigDecimal montoValor = BigDecimal.valueOf(20);

        // Objeto de dominio simulado
        // Asumiendo un constructor que permite inicializar todos los campos
        Transaccion trans = new Transaccion(
             TipoTransaccion.RETIRO, Dinero.of(montoValor), numeroCuenta 
        );
        trans.setId(99L);
        trans.setFecha(LocalDateTime.now());
        
        // DTO de respuesta esperado
        TransaccionRespuestaDTO respuesta = new TransaccionRespuestaDTO();
        respuesta.setNumeroCuentaOrigen(numeroCuenta); // Usamos el campo correcto
        respuesta.setTipo("DEBITO");
        respuesta.setMonto(montoValor);

        // Simulación del comportamiento
        when(gestionTransaccionPort.obtenerHistorial(numeroCuenta))
                .thenReturn(List.of(trans));

        when(mapper.toRespuestaDTO(trans)).thenReturn(respuesta);

        // Ejecución y Verificación
        mockMvc.perform(get("/api/v1/transacciones/historial/" + numeroCuenta))
                .andExpect(status().isOk())
                // El endpoint devuelve una lista, por eso usamos $[0]
                .andExpect(jsonPath("$[0].numeroCuentaOrigen").value(numeroCuenta)) // <-- Corrección
                .andExpect(jsonPath("$[0].monto").value(20))
                .andExpect(jsonPath("$[0].tipo").value("DEBITO")); // Se verifica el tipo también
    }
}
