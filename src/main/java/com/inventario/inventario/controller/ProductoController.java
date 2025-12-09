package com.inventario.inventario.controller;

import com.inventario.inventario.Models.Producto;
import com.inventario.inventario.Service.ServiceProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
@CrossOrigin( origins = "http://localhost:5174")
public class ProductoController {

    @Autowired
    private ServiceProduct serviceProduct;

    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public List<Producto> getAll() {
        return serviceProduct.findAll();
    }

    @GetMapping("/{id}")
    public Producto getById(@PathVariable Long id) {
        return serviceProduct.findById(id);
    }

    @PostMapping
    public Producto create(@RequestBody Producto producto) {
        return serviceProduct.save(producto);
    }

    @PutMapping("/{id}")
    public Producto update(@PathVariable Long id, @RequestBody Producto producto) {
        return serviceProduct.update(id, producto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        serviceProduct.delete(id);
    }
}
