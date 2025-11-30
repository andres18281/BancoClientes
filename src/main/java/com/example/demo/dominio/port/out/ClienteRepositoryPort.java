package com.example.demo.dominio.port.out;

import java.util.Optional;

import com.example.demo.dominio.modelo.Cliente;

public interface ClienteRepositoryPort {
    
    
    Cliente guardar(Cliente cliente);
    
    
    Optional<Cliente> buscarPorId(Long id);
    
    
    void eliminar(Long id);
    
    
    boolean tieneProductosVinculados(Long clienteId);
    
   
    Optional<Cliente> buscarPorIdentificacion(String tipoId, String numeroId);
}
