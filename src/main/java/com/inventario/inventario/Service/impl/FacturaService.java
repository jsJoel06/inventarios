package com.inventario.inventario.Service.impl;

import com.inventario.inventario.Models.*;
import com.inventario.inventario.repository.ClienteRepository;
import com.inventario.inventario.repository.FacturaRepository;
import com.inventario.inventario.repository.MovimientoRepository;
import com.inventario.inventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final FacturaRepository facturaRepository;
    private final ClienteRepository clienteRepository;

    // Ahora recibe Cliente completo en lugar de String
    public Factura crearFactura(Cliente cliente, List<DetalleFactura> detalles) {

        // Guardar cliente si es nuevo (sin ID)
        if (cliente.getId() == null) {
            cliente = clienteRepository.save(cliente);
        }

        Factura factura = new Factura();
        factura.setCliente(cliente);

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleFactura det : detalles) {

            // Buscar producto real
            Producto producto = productoRepository.findById(det.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + det.getProducto().getId()));

            // Validar inventario
            if (producto.getCantidad() < det.getCantidad()) {
                throw new RuntimeException("No hay suficiente inventario de: " + producto.getNombre());
            }

            // Restar inventario
            producto.setCantidad(producto.getCantidad() - det.getCantidad());
            productoRepository.save(producto);

            // Registrar movimiento de salida
            Movimiento movimiento = new Movimiento();
            movimiento.setProducto(producto);
            movimiento.setCantidad(det.getCantidad());
            movimiento.setTipo(TipoMovimiento.SALIDA);
            movimiento.setDescripcion("Venta en factura");
            movimiento.setFecha(LocalDateTime.now());
            movimientoRepository.save(movimiento);

            // Configurar detalle
            det.setProducto(producto);
            det.setFactura(factura);
            det.setPrecioUnitario(producto.getPrecio());
            det.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(det.getCantidad())));

            total = total.add(det.getSubtotal());
        }

        factura.setDetalles(detalles);
        factura.setTotal(total);
        factura.setFecha(LocalDateTime.now());

        return facturaRepository.save(factura);
    }
}
