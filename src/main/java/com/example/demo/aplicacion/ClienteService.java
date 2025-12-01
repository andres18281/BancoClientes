package com.example.demo.aplicacion;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.dominio.modelo.Cliente;
import com.example.demo.dominio.port.in.GestionClientePort;
import com.example.demo.dominio.port.out.ClienteRepositoryPort;
import com.example.demo.infraestructura.api.dto.ClienteModificacionDTO;


// Implementa el puerto impulsor
@Service
public class ClienteService implements GestionClientePort {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

   
    private final ClienteRepositoryPort clienteRepository;

    public ClienteService(ClienteRepositoryPort clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Cliente crearCliente(Cliente cliente) {
        
       
        cliente.validarDatos(); 
        
        
        if (!cliente.esMayorDeEdad()) {
            log.warn("Intento de creación de cliente menor de edad: {}", cliente.getNumeroIdentificacion());
            throw new IllegalArgumentException("El cliente debe ser mayor de edad.");
        }
        
        
        cliente.marcarComoCreado(); 
        
        
        return clienteRepository.guardar(cliente);
    }
    
    @Override
    public void eliminarCliente(Long id) {
        
        if (clienteRepository.tieneProductosVinculados(id)) {
            log.warn("Intento de eliminación fallida. Cliente {} tiene productos vinculados.", id);
            throw new IllegalStateException("No se puede eliminar un cliente con productos vinculados.");
        }
        clienteRepository.eliminar(id);
        log.info("Cliente {} eliminado exitosamente.", id);
    }

    
    @Override
    public Cliente actualizarCliente(Long id, ClienteModificacionDTO dto) {
        
        // 1. Obtener el Cliente existente del repositorio
        // NOTA: Usamos el 'id' recibido como parámetro, no una variable 'id' indefinida.
        Cliente clienteExistente = clienteRepository.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Cliente con ID " + id + " no encontrado para modificar."));

        // 2. Actualizar los campos de la entidad con la información del DTO
        clienteExistente.setNombres(dto.getNombres());
        clienteExistente.setApellido(dto.getApellido());
        clienteExistente.setCorreoElectronico(dto.getCorreoElectronico());

        clienteExistente.validarDatos(); 

        clienteExistente.marcarComoModificado(); 
        return clienteRepository.guardar(clienteExistente);
    }

    @Override
    public Optional<Cliente> buscarClientePorId(Long id) {
    	return clienteRepository.buscarPorId(id);
        
    }
}