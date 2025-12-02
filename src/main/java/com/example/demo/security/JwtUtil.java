package com.example.demo.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Utilidad para generar, validar y extraer información de los JSON Web Tokens (JWT).
 */
@Component
public class JwtUtil {

    // Clave secreta (debe ser larga y compleja)
    private static final String SECRET = "f44b20a3b8c6f1d2e5a7c4d8b9e0f3c2a1e6d4b9c8a7e0d3f2b1a4c5e6d8f7a9"; 

    // Tiempo de expiración: 10 minutos (en milisegundos)
    private static final long EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(10); 

    /**
     * Genera el token JWT para un usuario.
     * @param userDetails Detalles del usuario.
     * @return El token JWT generado.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject) // Nombre de usuario (o cuenta)
            .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de emisión
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Fecha de expiración (10 minutos)
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Valida si el token es válido (no expirado y coincide con el usuario).
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Métodos de extracción de Claims (extraerUsername, isTokenExpired, etc.)
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}