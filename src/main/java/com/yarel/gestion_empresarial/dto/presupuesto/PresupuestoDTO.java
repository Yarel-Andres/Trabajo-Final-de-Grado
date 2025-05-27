package com.yarel.gestion_empresarial.dto.presupuesto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoDTO {
    // Datos básicos del presupuesto
    private Long id;
    private String nombreCliente;
    private String descripcion;
    private String estado;

    // Fechas
    private LocalDate fechaCreacion;

    // Relaciones - Proyecto
    private Long proyectoId;
    private String proyectoNombre;

    // Relaciones - Creador (RRHH)
    private Long creadorId;
    private String creadorNombre;

    // Información financiera
    private Double tarifaHora;
    private Double horasEstimadas;
    private Double costoTotal;
}