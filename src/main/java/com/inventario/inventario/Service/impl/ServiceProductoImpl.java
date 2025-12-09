package com.inventario.inventario.Service.impl;

import com.inventario.inventario.Models.Producto;
import com.inventario.inventario.Service.ServiceProduct;
import com.inventario.inventario.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceProductoImpl implements ServiceProduct {

    @Autowired
    private ProductoRepository repository; // ✅ Usar el repositorio

    @Override
    public List<Producto> findAll() {
        return repository.findAll();
    }

    @Override
    public Producto findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Producto save(Producto producto) {
        return repository.save(producto);
    }

    @Override
    public Producto update(Long id, Producto producto) {
        Producto productoUpdate = repository.findById(id).orElse(null);
        if(productoUpdate != null){
            productoUpdate.setNombre(producto.getNombre());
            productoUpdate.setDescripcion(producto.getDescripcion());
            productoUpdate.setPrecio(producto.getPrecio());
            productoUpdate.setCantidad(producto.getCantidad());
            return repository.save(productoUpdate);
        }
        return repository.save(producto);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
