package com.example.demo.dominio.port.in;

import com.example.demo.dominio.modelo.Cliente;

public interface GestionClientePort {
    Cliente crearCliente(Cliente cliente);
    void eliminarCliente(Long id);
    Cliente actualizarCliente(Cliente cliente);
    Cliente buscarClientePorId(Long id);
}