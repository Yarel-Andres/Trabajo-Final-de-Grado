package com.yarel.gestion_empresarial.dto.proyecto;

import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

@Data
public class ProyectoDTO {
    // Datos basicos del proyecto
    private Long id;
    private String nombre;
    private String descripcion;
    private String estado;
    private boolean completado = false;

    // Fechas del proyecto
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private LocalDate fechaFinReal;

    // Relaciones con el Jefe creador del proyecto
    private Long jefeId;
    private String jefeNombre;

    // Relaciones con los empleados asignados
    private Set<Long> empleadosIds = new HashSet<>();
    private List<String> empleadosNombres = new ArrayList<>();

    // Información financiera
    private Double presupuesto;

    // Registros de tiempo asociados
    private Set<Long> registrosTiempoIds;

    // Metodos para asegurar que las colecciones nunca sean null
    public Set<Long> getEmpleadosIds() {
        if (empleadosIds == null) {
            empleadosIds = new HashSet<>();
        }
        return empleadosIds;
    }

    public List<String> getEmpleadosNombres() {
        if (empleadosNombres == null) {
            empleadosNombres = new ArrayList<>();
        }
        return empleadosNombres;
    }
}