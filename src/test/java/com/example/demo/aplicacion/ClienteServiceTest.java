package com.example.demo.aplicacion; 

import com.example.demo.dominio.modelo.Cliente;
import com.example.demo.dominio.modelo.VO.Email;
import com.example.demo.dominio.port.out.ClienteRepositoryPort;
import com.example.demo.infraestructura.api.dto.ClienteModificacionDTO; // Asume la ubicación del DTO

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

// Importaciones estáticas para Mockito y JUnit (para resolver 'never() is undefined')
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepositoryPort clienteRepository; // Puerto de salida
    
    @InjectMocks
    private ClienteService clienteService;

    // Se elimina @Mock dtoMock; ya que no se necesita simular el DTO, solo crearlo.
    private ClienteModificacionDTO dtoModificacion;

    private Cliente clienteActivo;
    private Cliente clienteActualizado;
    private final Long CLIENTE_ID = 1L;

    @BeforeEach
    void setUp() {
    	
    	Email emailPrueba = new Email("juan.perez@test.com");
        LocalDate fechaNac = LocalDate.of(1990, 1, 1);
        LocalDateTime ahora = LocalDateTime.now();
    	// Cliente ACTIVO (Original)
        clienteActivo = new Cliente(
            CLIENTE_ID,                 // id
            "CC",                       // tipoIdentificacion
            "12345678",                 // numeroIdentificacion
            "Juan",                     // nombres
            "Pérez",                    // apellido
            emailPrueba,                // correoElectronico
            fechaNac,                   // fechaNacimiento
            ahora,                      // fechaCreacion
            ahora                       // fechaModificacion
        );
        
        // Cliente ACTUALIZADO (Resultado esperado)
        clienteActualizado = new Cliente(
            CLIENTE_ID,
            "CC",
            "12345678",
            "Juan Pérez Actualizado",   // ⬅️ El campo que verificaremos
            "Pérez",
            emailPrueba,
            fechaNac,
            ahora,
            ahora
        );
        
        // También debes inicializar tu DTO de modificación para la prueba de actualización:
        dtoModificacion = new ClienteModificacionDTO(
                "Juan Pérez Actualizado",              // 1. nombres (String)
                "Pérez",                               // 2. apellido (String)
                new Email("juan.perez.nuevo@test.com") // 3. correoElectronico (Email VO)
            );
    }

    // --- PRUEBAS DE CREACIÓN ---
    
    @Test
    void testCrearCliente_debeGuardarClienteExitosamente() {
        // Arrange
        when(clienteRepository.guardar(any(Cliente.class))).thenReturn(clienteActivo);

        // Act: Asumiendo que crearCliente toma un Cliente o un DTO de creación
        Cliente resultado = clienteService.crearCliente(clienteActivo);

        // Assert
        assertNotNull(resultado);
        assertEquals(CLIENTE_ID, resultado.getId());
        verify(clienteRepository, times(1)).guardar(clienteActivo);
    }

    // --- PRUEBAS DE ELIMINACIÓN ---

    @Test
    void testEliminarCliente_debeLlamarAlRepositorioUnaVez() {
        // Act
        clienteService.eliminarCliente(CLIENTE_ID);

        // 🔑 CORRECCIÓN: Usar el nombre de método correcto del repositorio (asumo 'eliminar')
        verify(clienteRepository, times(1)).eliminar(CLIENTE_ID); 
        // Si tu método es 'eliminarPorId', entonces: verify(clienteRepository, times(1)).eliminarPorId(CLIENTE_ID);
    }
    
    // --- PRUEBAS DE ACTUALIZACIÓN ---

    @Test
    void testActualizarCliente_debeGuardarElClienteModificado() {
        // Arrange: 
        when(clienteRepository.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(clienteActivo));
        when(clienteRepository.guardar(any(Cliente.class))).thenReturn(clienteActualizado);
        
        // Act
        Cliente resultado = clienteService.actualizarCliente(CLIENTE_ID, dtoModificacion);

        // Assert
        assertNotNull(resultado);
        // 🔑 CORRECCIÓN: Usar getNombres()
        assertEquals("Juan Pérez Actualizado", resultado.getNombres()); 
        
        verify(clienteRepository, times(1)).buscarPorId(CLIENTE_ID);
        verify(clienteRepository, times(1)).guardar(any(Cliente.class));
    }
    
    


    // --- PRUEBAS DE BÚSQUEDA ---

    @Test
    void testBuscarClientePorId_cuandoExiste_debeRetornarCliente() {
        // Arrange: Simular que el repositorio encuentra el cliente
        when(clienteRepository.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(clienteActivo));

        // Act
        Optional<Cliente> resultado = clienteService.buscarClientePorId(CLIENTE_ID);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(CLIENTE_ID, resultado.get().getId());
        verify(clienteRepository, times(1)).buscarPorId(CLIENTE_ID);
    }

    @Test
    void testBuscarClientePorId_cuandoNoExiste_debeRetornarOptionalVacio() {
        // Arrange: Simular que el repositorio no encuentra nada
        when(clienteRepository.buscarPorId(CLIENTE_ID)).thenReturn(Optional.empty());

        // Act
        Optional<Cliente> resultado = clienteService.buscarClientePorId(CLIENTE_ID);

        // Assert
        assertFalse(resultado.isPresent());
        verify(clienteRepository, times(1)).buscarPorId(CLIENTE_ID);
    }
}