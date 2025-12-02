package com.example.demo.infraestructura.api.security;



import com.example.demo.infraestructura.api.dto.security.AuthRequestDTO;
import com.example.demo.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Controlador para la gestión de autenticación (login y generación de JWT).
 * La ruta /api/v1/auth/login está configurada como pública en SecurityConfig.java.
 */
	@RestController
	@RequestMapping("/api/v1/auth")
	@Tag(name = "Autenticación", description = "Gestión de login y generación de JSON Web Tokens (JWT)")
	public class AuthController {
	
	    private final AuthenticationManager authenticationManager;
	    private final UserDetailsService userDetailsService;
	    private final JwtUtil jwtUtil;
	
	    public AuthController(
	        AuthenticationManager authenticationManager, 
	        UserDetailsService userDetailsService, 
	        JwtUtil jwtUtil) {
	        this.authenticationManager = authenticationManager;
	        this.userDetailsService = userDetailsService;
	        this.jwtUtil = jwtUtil;
	    }
	
	    /**
	     * Endpoint público para que el cliente genere un JWT.
	     * 1. Autentica las credenciales con el AuthenticationManager.
	     * 2. Genera el JWT usando el JwtUtil (con expiración de 10 minutos).
	     * 3. Devuelve el JWT.
	     * @param request Contiene el usuario y la contraseña.
	     * @return ResponseEntity con el JWT en el cuerpo si la autenticación es exitosa (HTTP 200).
	     */
	    @Operation(
	            summary = "Generar JWT (Flujo Normal de Login)",
	            description = "Autentica con usuario y contraseña, y devuelve un JWT con expiración de 10 minutos.",
	            responses = {
	                @ApiResponse(
	                    responseCode = "200", 
	                    description = "Autenticación exitosa. Retorna el JWT en el cuerpo de la respuesta.",
	                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
	                ),
	                @ApiResponse(
	                    responseCode = "401", 
	                    description = "Credenciales inválidas o no autorizadas.",
	                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
	                )
	            }
	        )
	    @PostMapping("/login")
	    public ResponseEntity<String> createAuthenticationToken(@RequestBody AuthRequestDTO request) {
	        
	        // El AuthenticationManager utiliza el UserDetailsService y el PasswordEncoder 
	        // para validar las credenciales. Si falla, lanza una excepción de autenticación.
	        authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
	        );
	
	        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
	        
	        // Generar el JWT
	        final String jwt = jwtUtil.generateToken(userDetails);
	
	        // Devolver el token (el cliente debe enviarlo como "Authorization: Bearer <token>")
	        return ResponseEntity.ok(jwt);
	    }
}