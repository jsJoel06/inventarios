package com.inventario.inventario.Service;

import com.inventario.inventario.Models.Producto;

import java.util.List;

public interface ServiceProduct {

     List<Producto> findAll();

     Producto findById(Long id);

     Producto save(Producto producto);

     Producto update(Long id, Producto producto);

     void delete(Long id);

}
