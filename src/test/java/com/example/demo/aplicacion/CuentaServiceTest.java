package com.example.demo.aplicacion;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.example.demo.dominio.modelo.Cliente;
import com.example.demo.dominio.modelo.CuentaAhorros;
import com.example.demo.dominio.modelo.CuentaCorriente;
import com.example.demo.dominio.modelo.ProductoFinanciero;
import com.example.demo.dominio.modelo.ProductoFinanciero.EstadoCuenta;
import com.example.demo.dominio.modelo.ProductoFinanciero.TipoCuenta;
import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.dominio.port.out.ClienteRepositoryPort;
import com.example.demo.dominio.port.out.CuentaRepositoryPort;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
@MockitoSettings(strictness = Strictness.LENIENT)
class CuentaServiceTest {

    // Puertos y Repositorios (Mocks)
    @Mock
    private CuentaRepositoryPort cuentaRepository;

    @Mock
    private ClienteRepositoryPort clienteRepository;

    // Clase a probar (Inyección de Mocks)
    @InjectMocks
    private CuentaService cuentaService; // Asumiendo que el import es correcto

    // Constantes de prueba
    private final String NUMERO_CUENTA = "5300000001";
    private final Long CLIENTE_ID = 100L;
    private final Dinero SALDO_INICIAL = Dinero.of(BigDecimal.valueOf(1000.00));
    private final Dinero MONTO_RETIRO = Dinero.of(BigDecimal.valueOf(500.00));
    private final Dinero SALDO_CERO = Dinero.of(BigDecimal.ZERO);

    // Instancias de dominio reales (para pruebas de mutación de estado/saldo)
    private CuentaAhorros cuentaConSaldo;
    private CuentaAhorros cuentaVacia;

    @BeforeEach
    void setUp() {
        // 1. Reinicializar las cuentas como NUEVAS INSTANCIAS en cada test.
        // ESTO SOLUCIONA LOS PROBLEMAS DE MUTABILIDAD ENTRE TESTS.
        cuentaConSaldo = new CuentaAhorros(
                CLIENTE_ID, 1L, NUMERO_CUENTA, SALDO_INICIAL, EstadoCuenta.ACTIVA,
                LocalDateTime.now(), LocalDateTime.now(), false
        );

        cuentaVacia = new CuentaAhorros(
                CLIENTE_ID, 2L, "5300000002", SALDO_CERO, EstadoCuenta.ACTIVA,
                LocalDateTime.now(), LocalDateTime.now(), false
        );

        // 2. Comportamiento común del Repositorio (Guardar): devuelve el mismo objeto que recibe
        when(cuentaRepository.guardar(any(ProductoFinanciero.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 3. Comportamiento por defecto: cuando buscan el NUMERO_CUENTA devuelvo la instancia NUEVA
       
    }

    // ----------------------------------------------------------------------------------
    // PRUEBAS DE CREACIÓN
    // ----------------------------------------------------------------------------------

    @Test
    void testCrearCuenta_debeGuardarNuevaCuenta_siClienteExiste_CON_CAPTURER() {
        // Arrange
        final TipoCuenta tipoAhorros = TipoCuenta.AHORROS;
        
        // 1. Simular la búsqueda del Cliente
        Cliente clienteMock = mock(Cliente.class);
        when(clienteMock.getId()).thenReturn(CLIENTE_ID);
        when(clienteRepository.buscarPorId(CLIENTE_ID))
                .thenReturn(Optional.of(clienteMock));

        // 2. Definir el captor para la Cuenta
        ArgumentCaptor<ProductoFinanciero> captor =
                ArgumentCaptor.forClass(ProductoFinanciero.class);

        // NOTA: La simulación del 'guardar' ya está en el setUp y devuelve el mismo objeto.

        // Act
        ProductoFinanciero result =
                cuentaService.crearCuenta(CLIENTE_ID, tipoAhorros);

        // Assert
        // 1. Verificar la llamada a los repositorios
        verify(clienteRepository, times(1)).buscarPorId(CLIENTE_ID);
        // 2. Verificar la llamada a guardar y CAPTURAR el objeto que se intentó guardar
        verify(cuentaRepository, times(1)).guardar(captor.capture());

        ProductoFinanciero guardada = captor.getValue();

        // 3. Aserciones sobre el objeto capturado (Reglas de Negocio)
        assertNotNull(guardada, "El objeto guardado no debe ser nulo.");
        assertTrue(guardada instanceof CuentaAhorros, "Debe ser una instancia de CuentaAhorros.");
        assertEquals(TipoCuenta.AHORROS, guardada.getTipoCuenta(), "El tipo de cuenta debe ser AHORROS.");
        
        // 🔑 CORRECCIÓN CLAVE: Verificar que el objeto retornado por el servicio (result) 
        // es la MISMA INSTANCIA que fue capturada por el repositorio (guardada).
        assertSame(guardada, result, "El resultado del servicio debe ser la misma instancia que se guardó.");
    }

    @Test
    void testCrearCuenta_cuandoClienteNoExiste_debeLanzarExcepcion() {
        // Arrange
        when(clienteRepository.buscarPorId(CLIENTE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        // Si falla, el servicio lanza otra excepción que no es IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            cuentaService.crearCuenta(CLIENTE_ID, TipoCuenta.AHORROS);
        });

        verify(cuentaRepository, never()).guardar(any());
    }
    
    // ----------------------------------------------------------------------------------
    // PRUEBAS DE DEPÓSITO
    // ----------------------------------------------------------------------------------

    @Test
    void testDepositar_debeLlamarAlMetodoDepositarYGuardar() {
        // Arrange
        // 🔑 AÑADIR LA SIMULACIÓN DE BÚSQUEDA AQUÍ:
        // Le decimos a Mockito que al buscar por NUMERO_CUENTA, devuelva nuestra instancia mutable.
        when(cuentaRepository.buscarPorNumero(NUMERO_CUENTA))
                .thenReturn(Optional.of(cuentaConSaldo));
        
        Dinero montoDeposito = Dinero.of(BigDecimal.valueOf(200.00));

        // Act
        cuentaService.depositar(NUMERO_CUENTA, montoDeposito);

        // Assert
        // Se verifica que se llamó a guardar (después de modificar el saldo de cuentaConSaldo)
        verify(cuentaRepository, times(1)).guardar(cuentaConSaldo);
        
        // Se verifica el nuevo saldo: 1000 + 200 = 1200
        assertEquals(BigDecimal.valueOf(1200.00).setScale(2),
                cuentaConSaldo.getSaldo().getMonto().setScale(2));
    }

    // ----------------------------------------------------------------------------------
    // PRUEBAS DE RETIRO
    // ----------------------------------------------------------------------------------

    @Test
    void testRetirar_cuandoSaldoSuficiente_debeDisminuirSaldoYGuardar() {
        // Arrange
        // 🔑 AÑADIR LA SIMULACIÓN DE BÚSQUEDA AQUÍ:
        // Indicamos que al buscar por NUMERO_CUENTA, devuelva nuestra instancia mutable.
        when(cuentaRepository.buscarPorNumero(NUMERO_CUENTA))
                .thenReturn(Optional.of(cuentaConSaldo));
                
        // cuentaConSaldo tiene 1000.00 al inicio de esta prueba, gracias al @BeforeEach
        
        // Act
        cuentaService.retirar(NUMERO_CUENTA, MONTO_RETIRO);

        // Assert
        // 1. Verificar la interacción con el repositorio
        verify(cuentaRepository, times(1)).guardar(cuentaConSaldo);
        
        // 2. Verificar el nuevo saldo: 1000 - 500 = 500
        assertEquals(BigDecimal.valueOf(500.00).setScale(2),
                cuentaConSaldo.getSaldo().getMonto().setScale(2), 
                "El saldo final debe ser 500.00 después del retiro.");
    }

    @Test
    void testRetirar_cuandoSaldoInsuficiente_debeLanzarIllegalStateException() {
        // Arrange: cuentaConSaldo con 1000.00
        
        // 🔑 CLAVE: Añadir la simulación de la búsqueda
        when(cuentaRepository.buscarPorNumero(NUMERO_CUENTA))
                .thenReturn(Optional.of(cuentaConSaldo));
                
        Dinero montoExceso = Dinero.of(BigDecimal.valueOf(1001.00));

        // Act & Assert
        // Se espera que la lógica de dominio (CuentaAhorros.retirar) lance la excepción.
        assertThrows(IllegalStateException.class, () -> {
            cuentaService.retirar(NUMERO_CUENTA, montoExceso);
        }, "La operación debe lanzar IllegalStateException por saldo insuficiente.");

        // Verificar que NO se guardó (porque falló)
        verify(cuentaRepository, never()).guardar(any());
    }

    // ----------------------------------------------------------------------------------
    // PRUEBAS DE CANCELACIÓN
    // ----------------------------------------------------------------------------------

    @Test
    void testCancelarCuenta_cuandoSaldoEsCero_debeCambiarEstadoACanceladaYGuardar() {
        // Arrange: cuentaVacia tiene saldo 0. Sobrescribimos la simulación de búsqueda.
        when(cuentaRepository.buscarPorNumero(cuentaVacia.getNumeroCuenta()))
                .thenReturn(Optional.of(cuentaVacia));
        
        // Act
        cuentaService.cancelarCuenta(cuentaVacia.getNumeroCuenta());

        // Assert
        verify(cuentaRepository, times(1)).guardar(cuentaVacia);
        assertEquals(EstadoCuenta.CANCELADA, cuentaVacia.getEstado());
    }

    @Test
    void testCancelarCuentaAhorro_conSaldoNoCero_debeLanzarIllegalStateException() {
        // Arrange: Usamos cuentaConSaldo (1000.00)
        
        // 🔑 CLAVE: Simular la búsqueda para que el servicio encuentre la cuenta.
        when(cuentaRepository.buscarPorNumero(NUMERO_CUENTA))
                .thenReturn(Optional.of(cuentaConSaldo));
                
        // Act & Assert
        // El servicio encontrará la cuenta, pero la lógica de dominio debe lanzar la excepción
        assertThrows(IllegalStateException.class, () -> {
            cuentaService.cancelarCuenta(NUMERO_CUENTA); 
        }, "Debe lanzar IllegalStateException porque el saldo es distinto de cero.");
        
        // Verificar que NO se intentó guardar la cuenta cancelada
        verify(cuentaRepository, never()).guardar(any());
    }
    
    @Test
    void testCancelarCuentaCorriente_conSaldoNoCero_debeLanzarIllegalStateException() {
        // Arrange: Crear CuentaCorriente con saldo
        CuentaCorriente cuentaCorriente = new CuentaCorriente(
                CLIENTE_ID, 3L, "5300000003", SALDO_INICIAL, EstadoCuenta.ACTIVA,
                LocalDateTime.now(), LocalDateTime.now(), false
        );

        when(cuentaRepository.buscarPorNumero(cuentaCorriente.getNumeroCuenta()))
                .thenReturn(Optional.of(cuentaCorriente));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            cuentaService.cancelarCuenta(cuentaCorriente.getNumeroCuenta())
        );
        verify(cuentaRepository, never()).guardar(any());
    }

    // ----------------------------------------------------------------------------------
    // PRUEBAS DE BÚSQUEDA
    // ----------------------------------------------------------------------------------

    @Test
    void testBuscarCuentaPorNumero_cuandoExiste_debeRetornarCuenta() {
        // Arrange
        // 🔑 CLAVE: Simular la búsqueda. Le decimos a Mockito que al buscar el NUMERO_CUENTA,
        // devuelva nuestra instancia real 'cuentaConSaldo'.
        when(cuentaRepository.buscarPorNumero(NUMERO_CUENTA))
                .thenReturn(Optional.of(cuentaConSaldo));
                
        // Act
        Optional<ProductoFinanciero> resultado = cuentaService.buscarCuentaPorNumero(NUMERO_CUENTA);

        // Assert
        assertTrue(resultado.isPresent(), "La cuenta debe ser encontrada.");
        assertEquals(NUMERO_CUENTA, resultado.get().getNumeroCuenta(), "El número de cuenta debe coincidir.");
        
        // Verificar que la instancia devuelta es la misma que mockeamos.
        // Esto asegura que Mockito ha hecho su trabajo correctamente.
        assertEquals(cuentaConSaldo, resultado.get(), "La instancia devuelta debe ser la cuentaConSaldo.");
        
        // Verificar la interacción
        verify(cuentaRepository, times(1)).buscarPorNumero(NUMERO_CUENTA);
    }

    // ----------------------------------------------------------------------------------
    // PRUEBAS DE ACTUALIZACIÓN DE ESTADO
    // ----------------------------------------------------------------------------------

    @Test
    void testActualizarEstadoCuenta_aInactiva_debeCambiarEstadoYGuardar() {
        // Arrange
        // 🔑 CLAVE: Simular la búsqueda de la cuenta (que ahora está fuera del setUp)
        when(cuentaRepository.buscarPorNumero(NUMERO_CUENTA))
                .thenReturn(Optional.of(cuentaConSaldo));
                
        final String NUEVO_ESTADO_STRING = "INACTIVA";
        final EstadoCuenta ESTADO_ESPERADO = EstadoCuenta.INACTIVA;

        // Act
        ProductoFinanciero resultado = cuentaService.actualizarEstadoCuenta(NUMERO_CUENTA, NUEVO_ESTADO_STRING);
        // ...
    }
}