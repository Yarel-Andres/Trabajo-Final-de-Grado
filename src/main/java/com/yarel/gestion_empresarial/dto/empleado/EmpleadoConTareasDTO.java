package com.yarel.gestion_empresarial.dto.empleado;

import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EmpleadoConTareasDTO {
    private Long id;
    private String nombreUsuario;
    private String correo;
    private String nombreCompleto;
    private LocalDate fechaContratacion;
    private String puesto;
    private String telefono;
    private Double salario;
    private List<TareaDTO> tareas;
}