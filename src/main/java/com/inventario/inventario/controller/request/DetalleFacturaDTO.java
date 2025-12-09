package com.inventario.inventario.controller.request;



import lombok.Data;

@Data
public class DetalleFacturaDTO {
    private Long productoId;
    private Integer cantidad;
}
