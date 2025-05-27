// DTO de empleado que incluye sus tareas asignadas, lo utilizamos para vistas detalladas con información completa

package com.yarel.gestion_empresarial.dto.empleado;

import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class EmpleadoConTareasDTO {
    // Datos básicos del empleado
    private Long id;
    private String nombreUsuario;
    private String correo;
    private String nombreCompleto;

    // Datos específicos del empleado
    private LocalDate fechaContratacion;
    private String puesto;
    private String telefono;
    private Double salario;

    // Tareas asignadas
    private List<TareaDTO> tareas;
}