package com.example.demo.filters;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.security.JwtUtil;

import java.io.IOException;

/**
 * Filtro personalizado para procesar tokens JWT de autenticación Bearer.
 * Se encarga de validar el token en cada petición antes de que llegue al controlador.
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    
    private static final String BEARER_PREFIX = "Bearer ";

    public TokenAuthenticationFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain filterChain) throws ServletException, IOException {

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 1. Verificar si el encabezado existe o si no es un token Bearer
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = header.substring(BEARER_PREFIX.length());
        String username = null;

        try {
            // 2. Extraer el nombre de usuario del token
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // Un error aquí (ej. token expirado o firma inválida) 
            logger.warn("JWT inválido o expirado: " + e.getMessage());
        }

        // 3. Si se encuentra un nombre de usuario y el usuario aún no está autenticado
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 4. Validar el token contra los detalles del usuario y la expiración
            if (jwtUtil.validateToken(jwt, userDetails)) {
                
                // Si es válido, se crea el objeto de autenticación
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Establecer la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 6. Continuar con la cadena de filtros de Spring Security
        filterChain.doFilter(request, response);
    }
}