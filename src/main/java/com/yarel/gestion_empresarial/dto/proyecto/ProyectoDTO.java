package com.yarel.gestion_empresarial.dto.proyecto;

import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

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

    private boolean completado = false;

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

    private Double presupuesto;
    private Set<Long> empleadosIds = new HashSet<>();
    private Set<Long> registrosTiempoIds;

    // Nuevo campo para almacenar los nombres de los empleados
    private List<String> empleadosNombres = new ArrayList<>();

    // Metodo para asegurar que empleadosIds nunca sea null
    public Set<Long> getEmpleadosIds() {
        if (empleadosIds == null) {
            empleadosIds = new HashSet<>();
        }
        return empleadosIds;
    }

    // Método para asegurar que empleadosNombres nunca sea null
    public List<String> getEmpleadosNombres() {
        if (empleadosNombres == null) {
            empleadosNombres = new ArrayList<>();
        }
        return empleadosNombres;
    }
}
