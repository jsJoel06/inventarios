package com.inventario.inventario.controller.request;

import lombok.Data;

@Data
public class ClienteDTO {
    private String nombre;
    private String telefono;
    private String direccion;
    private String correo;
}
