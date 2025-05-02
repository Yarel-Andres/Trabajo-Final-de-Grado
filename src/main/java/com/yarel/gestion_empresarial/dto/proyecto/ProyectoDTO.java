package com.yarel.gestion_empresarial.dto.proyecto;

import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class ProyectoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Long jefeId;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private LocalDate fechaFinReal;
    private String estado;
    private Double presupuesto;
    private Set<Long> registrosTiempoIds;
}