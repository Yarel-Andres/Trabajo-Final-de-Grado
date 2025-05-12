package com.yarel.gestion_empresarial.dto.proyecto;

import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class ProyectoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Long jefeId;
    private String jefeNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private LocalDate fechaFinReal;
    private String estado;
    private Double presupuesto;
    private Set<Long> empleadosIds = new HashSet<>();
    private Set<Long> registrosTiempoIds;

    // Metodo para asegurar que empleadosIds nunca sea null
    public Set<Long> getEmpleadosIds() {
        if (empleadosIds == null) {
            empleadosIds = new HashSet<>();
        }
        return empleadosIds;
    }
}
