package com.yarel.gestion_empresarial.dto.tarea;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class TareaDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private Long empleadoId;
    private Long jefeId;
    private Long proyectoId; // Nuevo campo para el ID del proyecto
    private LocalDateTime fechaCreacion;
    private LocalDate fechaVencimiento;
    private boolean completada;
    private LocalDateTime fechaCompletada;
    private String prioridad;
    private Set<Long> registrosTiempoIds;

    // Campos adicionales para mostrar información en las vistas
    private String nombreProyecto; // Para mostrar el nombre del proyecto en las vistas
    private String empleadoNombre; // Para mostrar el nombre del empleado en las vistas
    private String jefeNombre; // Para mostrar el nombre del jefe que asignó la tarea
}
