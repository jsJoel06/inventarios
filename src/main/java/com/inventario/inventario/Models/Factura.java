package com.inventario.inventario.Models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "factura")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private LocalDateTime fecha = LocalDateTime.now();

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<DetalleFactura> detalles = new ArrayList<>();


    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private EstadoFactura estado = EstadoFactura.PENDIENTE;


    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Pago> pagos = new ArrayList<>();

    private String numero; // <-- Nuevo campo para el número descriptivo

    // Método para generar número de factura (se puede llamar al guardar)
    public String generarNumeroFactura() {
        if (this.id == null || this.fecha == null) {
            return null;
        }
        return String.format("FAC-%d-%02d-%04d",
                this.fecha.getYear(),
                this.fecha.getMonthValue(),
                this.id);
    }
}
