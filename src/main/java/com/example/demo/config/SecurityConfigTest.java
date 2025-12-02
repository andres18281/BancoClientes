package com.example.demo.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security exclusiva para entornos de test.
 * Esta clase se carga SOLAMENTE cuando el perfil 'test' está activo.
 * Su propósito es deshabilitar la autenticación JWT para que los tests
 * puedan acceder a los endpoints protegidos sin proporcionar tokens.
 */
@Configuration
@Profile("test") // <-- Solo se activa con @ActiveProfiles("test")
@Primary // <-- Asegura que esta configuración anule la principal
public class SecurityConfigTest {

    /**
     * Define una cadena de filtros de seguridad que permite el acceso a cualquier URL.
     * Esta anulación evita la necesidad de tokens JWT en los tests.
     */
    @Bean
    public SecurityFilterChain securityFilterChainTest(HttpSecurity http) throws Exception {

        http
            // Deshabilitar CSRF (estándar para REST)
            .csrf(AbstractHttpConfigurer::disable)
            
            // LÍNEA CLAVE: Permitir todas las peticiones sin autenticación
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll() 
            );

        return http.build();
    }
}