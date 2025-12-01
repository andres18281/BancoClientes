package com.example.demo.aplicacion;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.dominio.modelo.Cliente;
import com.example.demo.dominio.port.in.GestionClientePort;
import com.example.demo.dominio.port.out.ClienteRepositoryPort;
import com.example.demo.infraestructura.api.dto.ClienteModificacionDTO;

import lombok.extern.slf4j.Slf4j;


@Slf4j 
@Service
public class ClienteService implements GestionClientePort {

    

    private final ClienteRepositoryPort clienteRepository;

    public ClienteService(ClienteRepositoryPort clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

   
    @Override
    public Cliente crearCliente(Cliente cliente) {
        log.info("Iniciando creación de cliente con identificación: {}", cliente.getNumeroIdentificacion()); // 🔑 Log de inicio
        
        
        cliente.validarDatos(); 
        
        if (!cliente.esMayorDeEdad()) {
            log.warn("Intento de creación fallida. Cliente con ID {} no es mayor de edad.", cliente.getNumeroIdentificacion()); // 🔑 Log de fallo (WARN)
            throw new IllegalArgumentException("El cliente debe ser mayor de edad.");
        }

        cliente.marcarComoCreado();
        Cliente clienteGuardado = clienteRepository.guardar(cliente);
        
        log.info("CREACIÓN EXITOSA: Cliente {} creado con ID {}.", clienteGuardado.getNumeroIdentificacion(), clienteGuardado.getId()); // 🔑 Log de éxito
        return clienteGuardado;
    }


    @Override
    public void eliminarCliente(Long id) {
        log.info("Intentando eliminar lógicamente al Cliente con ID {}.", id); 
        
        if (clienteRepository.tieneProductosVinculados(id)) {
            log.warn("Intento de eliminación fallida. Cliente {} tiene productos vinculados.", id); 
            throw new IllegalStateException("No se puede eliminar un cliente con productos vinculados.");
        }
        
        clienteRepository.eliminar(id);
        log.info("ELIMINACIÓN EXITOSA: Cliente {} eliminado lógicamente.", id); 
    }

 
    @Override
    public Cliente actualizarCliente(Long id, ClienteModificacionDTO dto) {
        log.info("Iniciando actualización para el Cliente con ID {}.", id); 
        
        Cliente clienteExistente = clienteRepository.buscarPorId(id)
            .orElseGet(() -> {
                log.error("ACTUALIZACIÓN FALLIDA: Cliente con ID {} no encontrado.", id);
                throw new RuntimeException("Cliente con ID " + id + " no encontrado para modificar.");
            });

        
        clienteExistente.setNombres(dto.getNombres());
        clienteExistente.setApellido(dto.getApellido());
        clienteExistente.setCorreoElectronico(dto.getCorreoElectronico());

        
        clienteExistente.validarDatos(); 

        clienteExistente.marcarComoModificado();
        Cliente clienteActualizado = clienteRepository.guardar(clienteExistente);
        
        log.info("ACTUALIZACIÓN EXITOSA: Cliente {} modificado.", id); 
        return clienteActualizado;
    }


    @Override
    public Optional<Cliente> buscarClientePorId(Long id) {
        Optional<Cliente> cliente = clienteRepository.buscarPorId(id);
        
        if (cliente.isPresent()) {
            log.debug("CONSULTA EXITOSA: Cliente con ID {} encontrado.", id); 
        } else {
            log.debug("CONSULTA: Cliente con ID {} no encontrado.", id); 
        }
        
        return cliente;
    }
}