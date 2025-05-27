package com.yarel.gestion_empresarial.dto.tarea;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class TareaDTO {
    // Datos básicos de la tarea
    private Long id;
    private String titulo;
    private String descripcion;
    private String prioridad;
    private boolean completada;

    // Fechas importantes
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaVencimiento;
    private LocalDateTime fechaCompletada;

    // Relaciones con Id
    private Long empleadoId;
    private Long jefeId;
    private Long proyectoId;
    private Set<Long> registrosTiempoIds;

    // Nombres identificativos para las vistas
    private String nombreProyecto;
    private String empleadoNombre;
    private String jefeNombre;
}