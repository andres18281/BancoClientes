package com.example.demo.infraestructura.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dominio.modelo.CuentaAhorros;
import com.example.demo.dominio.modelo.ProductoFinanciero;
import com.example.demo.dominio.modelo.ProductoFinanciero.EstadoCuenta;
import com.example.demo.dominio.modelo.ProductoFinanciero.TipoCuenta;
import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.dominio.port.in.GestionCuentaPort;
import com.example.demo.infraestructura.api.dto.CuentaCreacionDTO;
import com.example.demo.infraestructura.api.dto.CuentaRespuestaDTO;
import com.example.demo.infraestructura.api.dto.DepositoDTO;
import com.example.demo.infraestructura.api.dto.EstadoCuentaDTO;
import com.example.demo.infraestructura.mappers.CuentaMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(ProductoFinancieroController.class)
class ProductoFinancieroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GestionCuentaPort gestionCuentaPort;

    @MockBean
    private CuentaMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------
    // Test: POST /api/v1/productos (crearCuenta)
    // -------------------------------------------------------------
    @Test
    void testCrearCuenta() throws Exception {

        CuentaCreacionDTO dto = new CuentaCreacionDTO();
        dto.setClienteId(1L);
        dto.setTipoCuenta("AHORROS");

        // Instanciar una cuenta REAL
        ProductoFinanciero cuenta = new CuentaAhorros(
                1L,                          // clienteId
                10L,                         // id
                "5300000001",                // numero
                Dinero.of(BigDecimal.ZERO),  // <-- AQUÍ está el cambio correcto
                ProductoFinanciero.EstadoCuenta.ACTIVA,
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
        );

        CuentaRespuestaDTO respuestaDTO = new CuentaRespuestaDTO(
        	    null, null,
        	    TipoCuenta.valueOf("AHORROS"),
        	    "5300000001",
        	    EstadoCuenta.valueOf("ACTIVA"),
        	    BigDecimal.ZERO,
        	    LocalDateTime.now()
        	);

        when(gestionCuentaPort.crearCuenta(any(), any())).thenReturn(cuenta);
        when(mapper.toRespuestaDTO(any())).thenReturn(respuestaDTO);

        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCuenta").value("5300000001"));
    }

    // -------------------------------------------------------------
    // Test: GET /api/v1/productos/{numeroCuenta}
    // -------------------------------------------------------------
    @Test
    void testConsultarCuenta() throws Exception {

        ProductoFinanciero cuenta = new CuentaAhorros(1L);
        CuentaRespuestaDTO respuesta = new CuentaRespuestaDTO(1L, 10L, TipoCuenta.AHORROS, "123", EstadoCuenta.ACTIVA, BigDecimal.TEN, LocalDateTime.now());


        when(gestionCuentaPort.buscarCuentaPorNumero("5300000001"))
                .thenReturn(Optional.of(cuenta));

        when(mapper.toRespuestaDTO(any())).thenReturn(respuesta);

        mockMvc.perform(get("/api/v1/productos/5300000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoCuenta").value("AHORROS"));
    }

    // -------------------------------------------------------------
    // Test: PATCH /api/v1/productos/{numeroCuenta}/estado
    // -------------------------------------------------------------
    @Test
    void testActualizarEstado() throws Exception {

        EstadoCuentaDTO dto = new EstadoCuentaDTO();
        dto.setNuevoEstado("INACTIVA");

        ProductoFinanciero cuenta = new CuentaAhorros(1L);
        cuenta.setEstado(ProductoFinanciero.EstadoCuenta.INACTIVA);

        CuentaRespuestaDTO respuesta = new CuentaRespuestaDTO();
        respuesta.setNumeroCuenta("5300000001");
        respuesta.setTipoCuenta(TipoCuenta.AHORROS);
        respuesta.setSaldo(BigDecimal.ZERO);
        respuesta.setEstado(EstadoCuenta.INACTIVA);
        respuesta.setId(1L);
        respuesta.setClienteId(10L);
        respuesta.setFechaCreacion(LocalDateTime.now());

        when(gestionCuentaPort.actualizarEstadoCuenta(any(), any()))
                .thenReturn(cuenta);
        when(mapper.toRespuestaDTO(any())).thenReturn(respuesta);

        mockMvc.perform(patch("/api/v1/productos/" + cuenta.getNumeroCuenta() + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVA"));
    }

    // -------------------------------------------------------------
    // Test: DELETE /api/v1/productos/{numeroCuenta}/cancelar
    // -------------------------------------------------------------
    @Test
    void testCancelarCuenta() throws Exception {

        Mockito.doNothing().when(gestionCuentaPort).cancelarCuenta("5300000001");

        mockMvc.perform(delete("/api/v1/productos/5300000001/cancelar"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------
    // Test: POST /api/v1/productos/depositar
    // -------------------------------------------------------------
    @Test
    void testConsignar() throws Exception {

        DepositoDTO dto = new DepositoDTO();
        dto.setNumeroCuenta("5300000001");
        dto.setMonto(new BigDecimal("100000"));

        Mockito.doNothing().when(gestionCuentaPort).depositar(any(), any());

        mockMvc.perform(post("/api/v1/productos/depositar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }
}