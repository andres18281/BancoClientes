package com.example.demo.infraestructura.datos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio de Spring Data JPA. Provee métodos CRUD básicos.
 */
@Repository
public interface ClienteJPARepository extends JpaRepository<ClienteJPA, Long> {

    
    Optional<ClienteJPA> findByTipoIdentificacionAndNumeroIdentificacion(String tipoId, String numeroId);
    
    
}