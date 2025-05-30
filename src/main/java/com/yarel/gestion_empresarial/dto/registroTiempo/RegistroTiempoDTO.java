package com.yarel.gestion_empresarial.dto.registroTiempo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RegistroTiempoDTO {
    // Datos basicos del registro
    private Long id;
    private LocalDate fechaRegistro;
    private Double horasTrabajadas;
    private String comentario;

    // Fechas y horas de trabajo
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;

    // Relaciones con Ids
    private Long usuarioId;
    private Long tareaId;
    private Long proyectoId;
    private Long reunionId;

    // Campos temporales para formularios que no se mapean con la base de datos
    private transient String fechaInicioTemp;
    private transient Integer horaInicioTemp;
    private transient Integer minutosInicioTemp;
    private transient String fechaFinTemp;
    private transient Integer horaFinTemp;
    private transient Integer minutosFinTemp;

    // Información adicional para vistas e informes
    private String usuarioNombre;
    private String tipoRegistro;
}