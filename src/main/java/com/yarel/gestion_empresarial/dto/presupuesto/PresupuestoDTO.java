package com.yarel.gestion_empresarial.dto.presupuesto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoDTO {
    private Long id;
    private String nombreCliente;
    private Long proyectoId;
    private String proyectoNombre;
    private Long creadorId;
    private String creadorNombre;
    private LocalDate fechaCreacion;
    private Double tarifaHora;
    private Double horasEstimadas;
    private Double costoTotal;
    private String descripcion;
    private String estado;
}