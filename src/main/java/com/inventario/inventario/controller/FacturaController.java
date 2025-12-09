package com.inventario.inventario.controller;

import com.inventario.inventario.Models.*;
import com.inventario.inventario.controller.request.FacturaDTO;
import com.inventario.inventario.controller.request.DetalleFacturaDTO;
import com.inventario.inventario.controller.request.ClienteDTO;
import com.inventario.inventario.controller.request.PagoDTO;
import com.inventario.inventario.repository.ClienteRepository;
import com.inventario.inventario.repository.ProductoRepository;
import com.inventario.inventario.repository.FacturaRepository;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/facturas")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://inventario-7yzy.onrender.com")
public class FacturaController {

    private final FacturaRepository facturaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    @GetMapping
    public List<Factura> listar(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) BigDecimal totalMin,
            @RequestParam(required = false) BigDecimal totalMax
    ) {
        List<Factura> facturas = facturaRepository.findAll();

        if (cliente != null && !cliente.isEmpty()) {
            facturas.removeIf(f -> !f.getCliente().getNombre().toLowerCase().contains(cliente.toLowerCase()));
        }
        if (fecha != null && !fecha.isEmpty()) {
            facturas.removeIf(f -> !f.getFecha().toLocalDate().toString().equals(fecha));
        }
        if (totalMin != null) {
            facturas.removeIf(f -> f.getTotal().compareTo(totalMin) < 0);
        }
        if (totalMax != null) {
            facturas.removeIf(f -> f.getTotal().compareTo(totalMax) > 0);
        }

        return facturas;
    }

    @GetMapping("/cliente/{id}")
    public List<Factura> historialPorCliente(@PathVariable Long id) {
        return facturaRepository.findByClienteIdOrderByFechaDesc(id);
    }

    @GetMapping("/{id}/pagos")
    public List<Pago> obtenerPagos(@PathVariable Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        return factura.getPagos();
    }

    @GetMapping("/{id}")
    public Factura obtenerPorId(@PathVariable Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
    }

    @PostMapping("/{id}/pagos")
    public Factura agregarPago(@PathVariable Long id, @RequestBody PagoDTO pagoDTO) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        Pago pago = new Pago();
        pago.setMonto(pagoDTO.getMonto());
        pago.setMetodo(pagoDTO.getMetodo());
        pago.setFechaPago(pagoDTO.getFechaPago());
        pago.setFactura(factura);

        factura.getPagos().add(pago);

        BigDecimal totalPagado = factura.getPagos().stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPagado.compareTo(factura.getTotal()) >= 0) {
            factura.setEstado(EstadoFactura.PAGADA);
        } else if (totalPagado.compareTo(BigDecimal.ZERO) > 0) {
            factura.setEstado(EstadoFactura.PENDIENTE);
        }

        return facturaRepository.save(factura);
    }

    // ---------------------------
    // ✔ CREAR FACTURA (sin generar PDF automáticamente)
    // ---------------------------
    @PostMapping("/crear")
    public Factura crear(@RequestBody FacturaDTO facturaDTO) {

        ClienteDTO cDTO = facturaDTO.getCliente();

        Cliente cliente = clienteRepository.findByCorreo(cDTO.getCorreo())
                .orElseGet(() -> {
                    Cliente c = new Cliente();
                    c.setNombre(cDTO.getNombre());
                    c.setTelefono(cDTO.getTelefono());
                    c.setDireccion(cDTO.getDireccion());
                    c.setCorreo(cDTO.getCorreo());
                    return clienteRepository.save(c);
                });

        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setTotal(BigDecimal.ZERO);
        factura.setDetalles(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleFacturaDTO d : facturaDTO.getDetalles()) {

            Producto producto = productoRepository.findById(d.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + d.getProductoId()));

            if (producto.getCantidad() < d.getCantidad()) {
                throw new RuntimeException("No hay suficiente inventario para: " + producto.getNombre());
            }

            producto.setCantidad(producto.getCantidad() - d.getCantidad());
            productoRepository.save(producto);

            DetalleFactura det = new DetalleFactura();
            det.setProducto(producto);
            det.setCantidad(d.getCantidad());
            det.setPrecioUnitario(producto.getPrecio());
            det.calcularSubtotal();
            det.setFactura(factura);

            factura.getDetalles().add(det);
            total = total.add(det.getSubtotal());
        }

        factura.setTotal(total);

        Factura guardada = facturaRepository.save(factura);
        guardada.setNumero(guardada.generarNumeroFactura());
        facturaRepository.save(guardada);

        // ✔ NO se genera PDF automáticamente al crear la factura
        return guardada;
    }

    // ---------------------------
    // ✔ GENERAR PDF desde la lista o detalle
    // ---------------------------
    @GetMapping("/{id}/pdf")
    public void generarPDF(@PathVariable Long id, HttpServletResponse response) throws Exception {

        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=factura_" + id + ".pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Encabezado
        document.add(new Paragraph("🛒 Almacén Joel")
                .setBold().setFontSize(22)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Sistema de Inventario")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Tel: +1 809 123 4567")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        // Datos de factura y cliente
        document.add(new Paragraph("Factura #: " + factura.getNumero()).setBold());
        document.add(new Paragraph("Fecha: " + factura.getFecha().toLocalDate().toString()));

        // Estado con color
        Color estadoColor;
        switch (factura.getEstado()) {
            case PAGADA:
                estadoColor = new DeviceRgb(0, 128, 0); // verde
                break;
            case PENDIENTE:
                estadoColor = new DeviceRgb(255, 165, 0); // naranja
                break;
            case ANULADA:
                estadoColor = new DeviceRgb(255, 0, 0); // rojo
                break;
            default:
                estadoColor = ColorConstants.GRAY;
        }
        document.add(new Paragraph("Estado: " + factura.getEstado().name())
                .setFontColor(estadoColor)
                .setBold());

        document.add(new Paragraph("Cliente: " + factura.getCliente().getNombre()));
        document.add(new Paragraph("Teléfono: " + factura.getCliente().getTelefono()));
        document.add(new Paragraph("Correo: " + factura.getCliente().getCorreo()));
        document.add(new Paragraph("Dirección: " + factura.getCliente().getDireccion()));
        document.add(new Paragraph("\n"));

        // Tabla de productos
        float[] columnWidths = {4, 2, 2, 2};
        Table table = new Table(columnWidths);

        String[] headers = {"Producto", "Cantidad", "Precio", "Subtotal"};
        for (String h : headers) {
            table.addCell(new Cell().add(new Paragraph(h).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        for (DetalleFactura det : factura.getDetalles()) {
            table.addCell(new Paragraph(det.getProducto().getNombre()));
            table.addCell(new Paragraph(det.getCantidad().toString()).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Paragraph("$" + det.getPrecioUnitario()).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Paragraph("$" + det.getSubtotal()).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);

        // Total
        document.add(new Paragraph("\nTOTAL: $" + factura.getTotal())
                .setBold().setFontSize(14)
                .setTextAlignment(TextAlignment.RIGHT));

        document.add(new Paragraph("\nGracias por su compra, "
                + factura.getCliente().getNombre() + "! 🙏")
                .setItalic().setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER));

        document.close();
    }
}
