package com.inventario.inventario.controller;

import com.inventario.inventario.Models.Movimiento;
import com.inventario.inventario.Service.ServiceMovimiento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movimientos")
@CrossOrigin( origins = "http://localhost:5173")
public class MovimientoController {

    @Autowired
    private ServiceMovimiento serviceMovimiento;

    @GetMapping
    public List<Movimiento> getAll() {
        return serviceMovimiento.findAll();
    }

    @GetMapping("/{id}")
    public Movimiento getById(@PathVariable Long id) {
        return serviceMovimiento.findById(id);
    }

    @PostMapping
    public Movimiento create(@RequestBody Movimiento movimiento) {
        return serviceMovimiento.save(movimiento);
    }

    @PutMapping("/{id}")
    public Movimiento update(@PathVariable Long id, @RequestBody Movimiento movimiento) {
        return serviceMovimiento.update(id, movimiento);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        serviceMovimiento.delete(id);
    }
}
