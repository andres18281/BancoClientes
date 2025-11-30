package com.example.demo.infraestructura.adaptadores;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.demo.dominio.modelo.Transaccion;
import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.dominio.port.out.TransaccionRepositoryPort;
import com.example.demo.infraestructura.datos.TransaccionJPA;
import com.example.demo.infraestructura.datos.TransaccionJPARepository;

@Component
public class TransaccionJPAAdapter implements TransaccionRepositoryPort {

    private final TransaccionJPARepository jpaRepository;

    public TransaccionJPAAdapter(TransaccionJPARepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // Mapeo de Dominio a JPA
    private TransaccionJPA toJPA(Transaccion dominio) {
        return new TransaccionJPA(
            dominio.getId(),
            dominio.getTipo(),
            dominio.getMonto().getMonto(), // VO Dinero a BigDecimal
            dominio.getFecha(),
            dominio.getNumeroCuentaOrigen(),
            dominio.getNumeroCuentaDestino()
        );
    }
    
    // Mapeo de JPA a Dominio
    private Transaccion toDominio(TransaccionJPA jpa) {
        // Asumiendo que Transaccion tiene un constructor de reconstrucción
        return new Transaccion(
            jpa.getId(),
            jpa.getTipo(),
            Dinero.of(jpa.getMonto()), // BigDecimal a VO Dinero
            jpa.getFecha(),
            jpa.getNumeroCuentaOrigen(),
            jpa.getNumeroCuentaDestino()
        );
    }

    @Override
    public Transaccion guardar(Transaccion transaccion) {
        TransaccionJPA entity = toJPA(transaccion);
        TransaccionJPA savedEntity = jpaRepository.save(entity);
        return toDominio(savedEntity);
    }

    @Override
    public List<Transaccion> guardarMultiples(List<Transaccion> transacciones) {
        List<TransaccionJPA> entities = transacciones.stream()
            .map(this::toJPA)
            .collect(Collectors.toList());
            
        List<TransaccionJPA> savedEntities = jpaRepository.saveAll(entities);
        
        return savedEntities.stream()
            .map(this::toDominio)
            .collect(Collectors.toList());
    }

    @Override
    public List<Transaccion> buscarPorCuenta(String numeroCuenta) {
        List<TransaccionJPA> jpaList = jpaRepository
            .findByNumeroCuentaOrigenOrNumeroCuentaDestinoOrderByFechaDesc(numeroCuenta, numeroCuenta);
            
        return jpaList.stream()
            .map(this::toDominio)
            .collect(Collectors.toList());
    }
}