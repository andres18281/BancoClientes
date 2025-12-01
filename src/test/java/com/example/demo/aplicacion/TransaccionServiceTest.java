package com.example.demo.aplicacion;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.example.demo.dominio.modelo.Transaccion;
import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.dominio.port.out.TransaccionRepositoryPort;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransaccionServiceTest {

    // Dependencias a simular
    @Mock
    private TransaccionRepositoryPort transaccionRepository;

    @Mock
    private CuentaService cuentaService; // Para las operaciones de saldo

    // Clase a probar
    @InjectMocks
    private TransaccionService transaccionService;

    // Constantes y VOs de prueba
    private final String CUENTA_ORIGEN = "1111111111";
    private final String CUENTA_DESTINO = "2222222222";
    private final Dinero MONTO = Dinero.of(BigDecimal.valueOf(100.00));
    private final Dinero SALDO_CERO = Dinero.of(BigDecimal.ZERO);


    @BeforeEach
    void setUp() {
    	when(transaccionRepository.guardar(any(Transaccion.class)))
        .thenAnswer(invocation -> {
            Transaccion transaccion = invocation.getArgument(0);
            // Simular que el repositorio asignó un ID (crucial para evitar NullPointer si el servicio lo usa)
            // Usamos un mock para simular el objeto guardado con ID.
            Transaccion guardada = mock(Transaccion.class); 
            when(guardada.getId()).thenReturn(999L); // ID Ficticio
            // Asume que también copia otros datos (Monto, Cuentas)
            return guardada; 
        });
    }

    // --- PRUEBAS DE CONSIGNACIÓN (Depósito) ---

    @Test
    void testConsignar_debeLlamarADepositarYGuardarTransaccion() {
        // Arrange: No se necesita simular el retorno del CuentaService (void method).
        
        // Act
        transaccionService.consignar(CUENTA_DESTINO, MONTO);

        // Assert:
        // 1. Verificar que el CuentaService recibió la orden de depositar
        verify(cuentaService, times(1)).depositar(CUENTA_DESTINO, MONTO);
        
        // 2. Verificar que el repositorio de transacciones guardó el registro
        // Usamos any() para indicar que guardó CUALQUIER instancia de Transaccion.
        verify(transaccionRepository, times(1)).guardar(any(Transaccion.class));
    }

    @Test
    void testConsignar_cuandoFallaCuentaService_noDebeGuardarTransaccion() {
        // Arrange: Simular que el depósito falla (ej. cuenta no encontrada o inactiva)
        doThrow(new IllegalArgumentException("Cuenta no encontrada."))
            .when(cuentaService).depositar(CUENTA_DESTINO, MONTO);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.consignar(CUENTA_DESTINO, MONTO);
        });

        // Verificar que el repositorio de transacciones NUNCA fue llamado
        verify(transaccionRepository, never()).guardar(any(Transaccion.class));
    }

    // --- PRUEBAS DE RETIRO ---

    @Test
    void testRetirar_debeLlamarARetirarYGuardarTransaccion() {
        // Arrange: No se necesita simular el retorno del CuentaService (void method).
        
        // Act
        transaccionService.retirar(CUENTA_ORIGEN, MONTO);

        // Assert:
        // 1. Verificar que el CuentaService recibió la orden de retirar
        verify(cuentaService, times(1)).retirar(CUENTA_ORIGEN, MONTO);
        
        // 2. Verificar que el repositorio de transacciones guardó el registro
        verify(transaccionRepository, times(1)).guardar(any(Transaccion.class));
    }

    @Test
    void testRetirar_cuandoFallaCuentaService_noDebeGuardarTransaccion() {
        // Arrange: Simular que el retiro falla (ej. saldo insuficiente)
        doThrow(new IllegalStateException("Saldo insuficiente."))
            .when(cuentaService).retirar(CUENTA_ORIGEN, MONTO);
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            transaccionService.retirar(CUENTA_ORIGEN, MONTO);
        });

        // Verificar que el repositorio de transacciones NUNCA fue llamado
        verify(transaccionRepository, never()).guardar(any(Transaccion.class));
    }

    // --- PRUEBAS DE TRANSFERENCIA ---
    
    @Test
    void testTransferir_debeLlamarARetirarYDepositarYGuardarTransaccion() {
        // Arrange: No se necesita stubbing.
        
        // Act
        transaccionService.transferir(CUENTA_ORIGEN, CUENTA_DESTINO, MONTO);

        // Assert:
        
        // 1. Verificar el retiro: Llamar a retirar con CUALQUIER String y CUALQUIER Dinero
        verify(cuentaService, times(1)).retirar(anyString(), any(Dinero.class)); 
        
        // 2. Verificar el depósito: Llamar a depositar con CUALQUIER String y CUALQUIER Dinero
        verify(cuentaService, times(1)).depositar(anyString(), any(Dinero.class));
        
        // 3. Verificar que se guardó la transacción:
        verify(transaccionRepository, times(1)).guardar(any(Transaccion.class)); 
    }

    @Test
    void testTransferir_cuandoFallaRetiro_noDebeIntentarDepositoNiGuardar() {
        // Arrange: Simular que el retiro de la cuenta origen falla (ej. saldo insuficiente)
        doThrow(new IllegalStateException("Saldo insuficiente en origen."))
            .when(cuentaService).retirar(CUENTA_ORIGEN, MONTO);
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            transaccionService.transferir(CUENTA_ORIGEN, CUENTA_DESTINO, MONTO);
        });

        // Verificar que el depósito NUNCA fue llamado
        verify(cuentaService, never()).depositar(anyString(), any(Dinero.class));
        // Verificar que la transacción NUNCA fue guardada
        verify(transaccionRepository, never()).guardar(any(Transaccion.class));
    }


    // --- PRUEBAS DE HISTORIAL ---

    @Test
    void testObtenerHistorial_debeLlamarAlRepositorioYRetornarLista() {
        // Arrange
        Transaccion t1 = mock(Transaccion.class);
        Transaccion t2 = mock(Transaccion.class);
        List<Transaccion> historialEsperado = Arrays.asList(t1, t2);

        // Simular que el repositorio devuelve la lista
        when(transaccionRepository.buscarPorCuenta(CUENTA_ORIGEN)).thenReturn(historialEsperado);

        // Act
        List<Transaccion> resultado = transaccionService.obtenerHistorial(CUENTA_ORIGEN);

        // Assert
        // 1. Verificar que el repositorio fue consultado
        verify(transaccionRepository, times(1)).buscarPorCuenta(CUENTA_ORIGEN);
        
        // 2. Verificar que se devolvió la lista esperada
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(historialEsperado, resultado);
    }
}
