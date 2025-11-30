package com.example.demo.infraestructura.datos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionJPARepository extends JpaRepository<TransaccionJPA, Long> {

    
    List<TransaccionJPA> findByNumeroCuentaOrigenOrNumeroCuentaDestinoOrderByFechaDesc(String cuentaOrigen, String cuentaDestino);
}