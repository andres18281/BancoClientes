package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.filters.TokenAuthenticationFilter;
import com.example.demo.security.JwtUtil;

/**
 * Clase de configuración de Spring Security.
 * Define las reglas de acceso, el mecanismo de autenticación y el cifrado.
 */
@Profile("!test")
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	
	@Value("${app.security.test-users.admin-username}")
	private String adminUsername;

	@Value("${app.security.test-users.admin-password}")
	private String adminPassword;

    /**
     * Define el TokenAuthenticationFilter como un Bean.
     */
    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter(
        UserDetailsService userDetailsService, 
        JwtUtil jwtUtil
    ) {
        return new TokenAuthenticationFilter(userDetailsService, jwtUtil);
    }
    
    /**
     * Define la cadena de filtros de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http, 
        TokenAuthenticationFilter tokenAuthenticationFilter 
    ) throws Exception {
        
        http
            // 1. Deshabilitar CSRF (Crucial para REST APIs)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. LÍNEAS CLAVE PARA EL 403: Deshabilitar autenticación por formulario y HTTP Basic
            .formLogin(AbstractHttpConfigurer::disable) 
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // 3. Configurar la política de sesión como SIN ESTADO
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 4. Configurar las reglas de autorización
            .authorizeHttpRequests(authorize -> authorize
                // Permitir acceso a la ruta de login y Swagger sin autenticación
                .requestMatchers("/api/v1/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() 
                // Requerir autenticación para todas las demás peticiones
                .anyRequest().authenticated() 
            )
            
            // 5. Agregar el filtro JWT personalizado ANTES del filtro de autenticación estándar
            .addFilterBefore(
                tokenAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
    
    /**
     * Define el DaoAuthenticationProvider para vincular UserDetailsService y PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Expone el AuthenticationManager como un Bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    /**
     * Bean para el cifrado de contraseñas.
     * Se usa el codificador delegante para que el prefijo {noop} funcione correctamente.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); 
    }

    /**
     * Simula la carga de usuarios en memoria (implementación de UserDetailsService).
     */
    @Bean
    public UserDetailsService userDetailsService() {
        

        UserDetails admin = User.withUsername(adminUsername)
            .password("{noop}" +adminPassword)
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager( admin);
    }
}