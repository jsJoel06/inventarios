package com.inventario.inventario.controller.request;


import lombok.Data;
import java.util.List;

@Data
public class FacturaDTO {
    private ClienteDTO cliente;
    private List<DetalleFacturaDTO> detalles;
}
