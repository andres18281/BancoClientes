package com.example.demo.dominio.port.in;

import java.util.Optional;

import com.example.demo.dominio.modelo.Cliente;

public interface GestionClientePort {
    Cliente crearCliente(Cliente cliente);
    void eliminarCliente(Long id);
    Cliente actualizarCliente(Cliente cliente);
    Optional<Cliente> buscarClientePorId(Long id);
}