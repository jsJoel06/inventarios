package com.inventario.inventario.repository;

import com.inventario.inventario.Models.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByClienteIdOrderByFechaDesc(Long clienteId);
}
