package com.example.demo.infraestructura.adaptadores;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.demo.dominio.modelo.Cliente;
import com.example.demo.dominio.modelo.ProductoFinanciero.EstadoCuenta;
import com.example.demo.dominio.modelo.VO.Email;
import com.example.demo.dominio.port.out.ClienteRepositoryPort;
import com.example.demo.infraestructura.datos.ClienteJPA;
import com.example.demo.infraestructura.datos.ClienteJPARepository;
import com.example.demo.infraestructura.datos.ProductoJPARepository;

@Component
public class ClienteJPAAdapter implements ClienteRepositoryPort {

    private final ClienteJPARepository jpaRepository;
    
    private final ProductoJPARepository productoRepository; 

    
    public ClienteJPAAdapter(ClienteJPARepository jpaRepository, ProductoJPARepository productoRepository) {
        this.jpaRepository = jpaRepository;
        this.productoRepository = productoRepository;
    }

    
    private ClienteJPA toJPA(Cliente dominio) {
        return new ClienteJPA(
            dominio.getId(),
            dominio.getTipoIdentificacion(),
            dominio.getNumeroIdentificacion(),
            dominio.getNombres(),
            dominio.getApellido(),
            dominio.getCorreoElectronico().getDireccion(), 
            dominio.getFechaNacimiento(),
            dominio.getFechaCreacion(),
            dominio.getFechaModificacion(),
            false 
        );
    }
    
  
    private Cliente toDominio(ClienteJPA jpa) {
       
        return new Cliente(
            jpa.getId(),
            jpa.getTipoIdentificacion(),
            jpa.getNumeroIdentificacion(),
            jpa.getNombres(),
            jpa.getApellido(),
            new Email(jpa.getCorreoElectronico()), 
            jpa.getFechaNacimiento(),
            jpa.getFechaCreacion(),
            jpa.getFechaModificacion()
        );
    }


    @Override
    public Cliente guardar(Cliente cliente) {
        ClienteJPA entity = toJPA(cliente);
        ClienteJPA savedEntity = jpaRepository.save(entity);
        return toDominio(savedEntity);
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDominio);
    }

    @Override
    public void eliminar(Long id) {
        // 🔑 REGLA TÉCNICA: Se recomienda el borrado lógico en banca.
        // Buscamos la entidad, la marcamos como eliminada y guardamos.
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setEliminado(true);
            jpaRepository.save(entity);
        });
    }

    // 🔑 IMPLEMENTACIÓN DE LA REGLA DE NEGOCIO EN EL ADAPTADOR
    @Override
    public boolean tieneProductosVinculados(Long clienteId) {
        // Lógica de DB: Contar cuántas cuentas están vinculadas a este cliente.
        // Aquí se usa el ProductoJPARepository (asumido)
    	return productoRepository.countByClienteIdAndEstadoNot(clienteId, EstadoCuenta.CANCELADA) > 0;
    }

    @Override
    public Optional<Cliente> buscarPorIdentificacion(String tipoId, String numeroId) {
        return jpaRepository
                .findByTipoIdentificacionAndNumeroIdentificacion(tipoId, numeroId)
                .map(this::toDominio);
    }
}
