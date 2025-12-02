package com.example.demo.infraestructura.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.demo.dominio.modelo.Cliente;
import com.example.demo.dominio.modelo.VO.Email;
import com.example.demo.dominio.port.in.GestionClientePort;
import com.example.demo.infraestructura.api.dto.ClienteCreacionDTO;
import com.example.demo.infraestructura.api.dto.ClienteRespuestaDTO;
import com.example.demo.infraestructura.mappers.ClienteMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is; // Importar esto para usar is() en el content().json()
//1. Configuración de Spring Boot Test

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

 // Simula las peticiones HTTP
 @Autowired
 private MockMvc mockMvc;

 // 2. Mocks de las dependencias inyectadas en el Controller
 // El puerto (capa de aplicación) debe ser mockeado
 @MockBean
 private GestionClientePort gestionClientePort; 
 
 // El mapper también debe ser mockeado
 @MockBean
 private ClienteMapper mapper;

 // Utilidad para convertir objetos Java a JSON y viceversa
 @Autowired
 private ObjectMapper objectMapper;

 // --- Variables de Prueba (Mocks de Objetos) ---
 private final Long CLIENTE_ID = 10L;
 private final String API_BASE_PATH = "/api/v1/clientes";
 
 // Simulación del DTO de entrada
 private ClienteCreacionDTO crearClienteDTO() {
	 return new ClienteCreacionDTO(
		        "CC",                           // 1. tipoIdentificacion (String)
		        "1020304050",                   // 2. numeroIdentificacion (String)
		        "Juan",                         // 3. nombres (String)
		        "Pérez",                        // 4. apellido (String)
		        "juan.perez@example.com",       // 5. correoElectronico (String)
		        LocalDate.of(1990, 5, 15)       // 6. fechaNacimiento (LocalDate)
		    );
 }

 // Simulación del Modelo de Dominio
 private Cliente crearClienteDominio() {
	 return new Cliente(
		        CLIENTE_ID,                             // 1. Long id (Tu nuevo argumento)
		        "CC",                                   // 2. String tipoIdentificacion
		        "1020304050",                           // 3. String numeroIdentificacion
		        "Juan",                                 // 4. String nombres
		        "Pérez",                                // 5. String apellido
		        new Email("juan.perez@example.com"),    // 6. Email correoElectronico (CREAR EL VO)
		        LocalDate.of(1990, 5, 15)               // 7. LocalDate fechaNacimiento
		        , null, null
		    );
 }
 
 // Simulación del DTO de respuesta
 private ClienteRespuestaDTO crearClienteRespuestaDTO() {
	    // Esto dependerá de los campos de tu ClienteRespuestaDTO
	 LocalDateTime fechaFicticiaCreacion = LocalDateTime.of(2025, 1, 1, 10, 0); 

	 return new ClienteRespuestaDTO(
		        CLIENTE_ID,                             // 1. Long id
		        "CC",                                   // 2. String tipoIdentificacion
		        "1020304050",                           // 3. String numeroIdentificacion
		        "Juan",                                 // 4. String nombres
		        "Pérez",                                // 5. String apellido
		        "juan.perez@example.com",               // 6. String correoElectronico
		        LocalDate.of(1990, 5, 15),              // 7. LocalDate fechaNacimiento
		        fechaFicticiaCreacion                   // 8. LocalDateTime fechaCreacion ⬅️ ¡Añadido!
		    );
	}

//----------------------------------------------------------------------
//1. POST /api/v1/clientes - Crear Cliente
//----------------------------------------------------------------------

 @Test
 void testCrearCliente_debeRetornar201_yClienteCreado() throws Exception {
     // Arrange
     ClienteCreacionDTO inputDTO = crearClienteDTO();
     Cliente clienteDominio = crearClienteDominio();
     ClienteRespuestaDTO expectedOutputDTO = crearClienteRespuestaDTO();

     // Simular el comportamiento del Mapper
     when(mapper.toDominio(any(ClienteCreacionDTO.class))).thenReturn(clienteDominio);
     when(mapper.toRespuestaDTO(any(Cliente.class))).thenReturn(expectedOutputDTO);
     
     // Simular el comportamiento del Puerto de Aplicación
     when(gestionClientePort.crearCliente(any(Cliente.class))).thenReturn(clienteDominio);

     // Act & Assert
     mockMvc.perform(post(API_BASE_PATH)
             .contentType(MediaType.APPLICATION_JSON)
             .content(objectMapper.writeValueAsString(inputDTO)))
             
             // Assert: Verificar el status y el contenido
             .andExpect(status().isCreated()) // Espera HttpStatus.CREATED (201)
             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
             .andExpect(jsonPath("$.id", is(CLIENTE_ID.intValue())))
             .andExpect(jsonPath("$.nombre", is("Juan Pérez")));

     // Verificar interacciones con los Mocks
     verify(mapper, times(1)).toDominio(any(ClienteCreacionDTO.class));
     verify(gestionClientePort, times(1)).crearCliente(any(Cliente.class));
     verify(mapper, times(1)).toRespuestaDTO(any(Cliente.class));
 }

//----------------------------------------------------------------------
//2. GET /api/v1/clientes/{id} - Consultar Cliente por ID
//----------------------------------------------------------------------

 @Test
 void testBuscarClientePorId_cuandoClienteExiste_debeRetornar200_yCliente() throws Exception {
     // Arrange
     Cliente clienteDominio = crearClienteDominio();
     ClienteRespuestaDTO expectedOutputDTO = crearClienteRespuestaDTO();

     // Simular que el puerto encuentra el cliente
     when(gestionClientePort.buscarClientePorId(CLIENTE_ID)).thenReturn(Optional.of(clienteDominio));
     
     // Simular la conversión a DTO de respuesta
     when(mapper.toRespuestaDTO(any(Cliente.class))).thenReturn(expectedOutputDTO);

     // Act & Assert
     mockMvc.perform(get(API_BASE_PATH + "/{id}", CLIENTE_ID)
             .contentType(MediaType.APPLICATION_JSON))
             
             .andExpect(status().isOk()) // Espera HttpStatus.OK (200)
             .andExpect(jsonPath("$.id", is(CLIENTE_ID.intValue())));

     // Verificar interacciones
     verify(gestionClientePort, times(1)).buscarClientePorId(CLIENTE_ID);
     verify(mapper, times(1)).toRespuestaDTO(any(Cliente.class));
 }

 @Test
 void testBuscarClientePorId_cuandoClienteNoExiste_debeRetornar404() throws Exception {
     // Arrange
     // Simular que el puerto NO encuentra el cliente
     when(gestionClientePort.buscarClientePorId(CLIENTE_ID)).thenReturn(Optional.empty());

     // Act & Assert
     mockMvc.perform(get(API_BASE_PATH + "/{id}", CLIENTE_ID)
             .contentType(MediaType.APPLICATION_JSON))
             
             .andExpect(status().isNotFound()); // Espera HttpStatus.NOT_FOUND (404)

     // Verificar interacciones
     verify(gestionClientePort, times(1)).buscarClientePorId(CLIENTE_ID);
     verify(mapper, never()).toRespuestaDTO(any()); // No debe llamar al mapper si no lo encuentra
 }

//----------------------------------------------------------------------
//4. DELETE /api/v1/clientes/{id} - Eliminar Lógicamente
//----------------------------------------------------------------------

 @Test
 void testEliminarCliente_debeRetornar204() throws Exception {
     // Arrange
     // Simular que la operación de eliminación no arroja excepción
     doNothing().when(gestionClientePort).eliminarCliente(CLIENTE_ID);

     // Act & Assert
     mockMvc.perform(delete(API_BASE_PATH + "/{id}", CLIENTE_ID))
             
             .andExpect(status().isNoContent()); // Espera HttpStatus.NO_CONTENT (204)

     // Verificar interacciones
     verify(gestionClientePort, times(1)).eliminarCliente(CLIENTE_ID);
 }
}