package com.example.demo.infraestructura.adaptadores;

import org.springframework.stereotype.Component;

import com.example.demo.dominio.modelo.CuentaAhorros;
import com.example.demo.dominio.modelo.CuentaCorriente;
import com.example.demo.dominio.modelo.ProductoFinanciero;
import com.example.demo.dominio.modelo.ProductoFinanciero.TipoCuenta;
import com.example.demo.dominio.modelo.VO.Dinero;
import com.example.demo.dominio.port.out.CuentaRepositoryPort;
import com.example.demo.infraestructura.datos.AhorrosJPA;
import com.example.demo.infraestructura.datos.CorrienteJPA;
import com.example.demo.infraestructura.datos.ProductoJPA;
import com.example.demo.infraestructura.datos.ProductoJPARepository;
import java.util.Optional;

@Component
public class ProductoJPAAdapter implements CuentaRepositoryPort {

    private final ProductoJPARepository jpaRepository;

    public ProductoJPAAdapter(ProductoJPARepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // 🔑 Mapeo de Dominio a JPA
    private ProductoJPA toJPA(ProductoFinanciero dominio) {
        ProductoJPA jpa;
        if (dominio.getTipoCuenta() == TipoCuenta.AHORROS) {
            jpa = new AhorrosJPA();
        } else if (dominio.getTipoCuenta() == TipoCuenta.CORRIENTE) {
            jpa = new CorrienteJPA();
        } else {
            throw new IllegalArgumentException("Tipo de cuenta no mapeable.");
        }
        
        // Copia de campos comunes
        jpa.setId(dominio.getId());
        jpa.setClienteId(dominio.getClienteId());
        jpa.setNumeroCuenta(dominio.getNumeroCuenta());
        jpa.setSaldo(dominio.getSaldo().getMonto()); // 🔑 VO a BigDecimal
        jpa.setEstado(dominio.getEstado());
        jpa.setTipoCuenta(dominio.getTipoCuenta());
        jpa.setExentaGMF(dominio.isExentaGMF());
        jpa.setFechaCreacion(dominio.getFechaCreacion());
        jpa.setFechaModificacion(dominio.getFechaModificacion());
        return jpa;
    }

    // 🔑 Mapeo de JPA a Dominio (Reconstruye la Entidad y el VO Dinero)
    private ProductoFinanciero toDominio(ProductoJPA jpa) {
        // Usa el constructor de copia para reconstruir la entidad de Dominio.
        if (jpa.getTipoCuenta() == TipoCuenta.AHORROS) {
            return new CuentaAhorros(jpa.getClienteId(), jpa.getId(), jpa.getNumeroCuenta(), 
                                     Dinero.of(jpa.getSaldo()), jpa.getEstado(), jpa.getFechaCreacion(), 
                                     jpa.getFechaModificacion(), jpa.isExentaGMF());
        } else if (jpa.getTipoCuenta() == TipoCuenta.CORRIENTE) {
             return new CuentaCorriente(jpa.getClienteId(), jpa.getId(), jpa.getNumeroCuenta(), 
                                        Dinero.of(jpa.getSaldo()), jpa.getEstado(), jpa.getFechaCreacion(), 
                                        jpa.getFechaModificacion(), jpa.isExentaGMF());
        }
        throw new IllegalArgumentException("Tipo de producto no mapeable.");
    }

    @Override
    public ProductoFinanciero guardar(ProductoFinanciero producto) {
        ProductoJPA entity = toJPA(producto);
        ProductoJPA savedEntity = jpaRepository.save(entity);
        return toDominio(savedEntity);
    }

    @Override
    public Optional<ProductoFinanciero> buscarPorNumero(String numeroCuenta) {
        return jpaRepository.findByNumeroCuenta(numeroCuenta).map(this::toDominio);
    }

    @Override
    public Optional<ProductoFinanciero> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDominio);
    }

    @Override
    public boolean existeNumeroCuenta(String numeroCuenta) {
        return jpaRepository.findByNumeroCuenta(numeroCuenta).isPresent();
    }
}