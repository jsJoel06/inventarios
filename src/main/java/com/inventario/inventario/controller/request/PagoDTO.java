package com.inventario.inventario.controller.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PagoDTO {
    private BigDecimal monto;
    private String metodo;
    private LocalDate fechaPago;
}
