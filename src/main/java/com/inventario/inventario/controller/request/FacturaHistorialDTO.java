package com.inventario.inventario.controller.request;

import lombok.Data;

import java.util.List;

@Data
public class FacturaHistorialDTO {

    private Long id;
    private String numero;
    private String fecha;
    private Double total;
    private String estado;
    private List<PagoDTO> pagos;
}
