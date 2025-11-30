package com.example.demo.infraestructura.datos;
import com.example.demo.infraestructura.datos.ClienteJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio de Spring Data JPA. Provee métodos CRUD básicos.
 */
@Repository
public interface ClienteJPARepository extends JpaRepository<ClienteJPA, Long> {

    
	Optional<ClienteJPA> findByNumeroIdentificacion(String numeroIdentificacion);
    Optional<ClienteJPA> findByCorreoElectronico(String correo);
    Optional<ClienteJPA> findByTipoIdentificacionAndNumeroIdentificacion(
            String tipoIdentificacion, 
            String numeroIdentificacion
        );
    
}