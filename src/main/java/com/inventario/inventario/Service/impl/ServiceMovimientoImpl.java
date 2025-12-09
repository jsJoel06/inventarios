package com.inventario.inventario.Service.impl;

import com.inventario.inventario.Models.Movimiento;
import com.inventario.inventario.Models.Producto;
import com.inventario.inventario.Models.TipoMovimiento;
import com.inventario.inventario.Service.ServiceMovimiento;
import com.inventario.inventario.repository.MovimientoRepository;
import com.inventario.inventario.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceMovimientoImpl implements ServiceMovimiento {

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public List<Movimiento> findAll() {
        return movimientoRepository.findAll();
    }

    @Override
    public Movimiento findById(Long id) {
        return movimientoRepository.findById(id).orElse(null);
    }

    @Override
    public Movimiento save(Movimiento movimiento) {
        Producto producto = productoRepository.findById(movimiento.getProducto().getId()).orElse(null);
        if (producto == null) {
            throw new RuntimeException("Producto no encontrado");
        }

        if (movimiento.getTipo() == TipoMovimiento.ENTRADA) {
            producto.setCantidad(producto.getCantidad() + movimiento.getCantidad());
        } else if (movimiento.getTipo() == TipoMovimiento.SALIDA) {
            if (producto.getCantidad() < movimiento.getCantidad()) {
                throw new RuntimeException("Stock insuficiente");
            }
            producto.setCantidad(producto.getCantidad() - movimiento.getCantidad());
        } else {
            throw new RuntimeException("Tipo de movimiento no válido");
        }

        // Guardar cambios en el producto
        productoRepository.save(producto);

        // Guardar el movimiento
        return movimientoRepository.save(movimiento);
    }

    @Override
    public Movimiento update(Long id, Movimiento movimiento) {
        Movimiento movimientoUpdate = movimientoRepository.findById(id).orElse(null);
        if (movimientoUpdate != null) {
            movimientoUpdate.setProducto(movimiento.getProducto());
            movimientoUpdate.setTipo(movimiento.getTipo());
            movimientoUpdate.setCantidad(movimiento.getCantidad());
            movimientoUpdate.setFecha(movimiento.getFecha());

            return save(movimientoUpdate);
        }
        return save(movimiento);
    }

    @Override
    public void delete(Long id) {
        movimientoRepository.deleteById(id);
    }
}
