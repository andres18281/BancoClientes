package com.example.demo.dominio.port.in;

import java.util.Optional;

import com.example.demo.dominio.modelo.Cliente;

import com.example.demo.infraestructura.api.dto.ClienteModificacionDTO;

public interface GestionClientePort {
    Cliente crearCliente(Cliente cliente);
    void eliminarCliente(Long id);
    Cliente actualizarCliente(Long id, ClienteModificacionDTO dto);
    Optional<Cliente> buscarClientePorId(Long id);
   
}