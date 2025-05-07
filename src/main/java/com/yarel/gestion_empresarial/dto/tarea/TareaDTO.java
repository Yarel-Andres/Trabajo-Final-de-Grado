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
    private LocalDateTime fechaCreacion;
    private LocalDate fechaVencimiento;
    private boolean completada;
    private LocalDateTime fechaCompletada;
    private String prioridad;
    private Set<Long> registrosTiempoIds;

}