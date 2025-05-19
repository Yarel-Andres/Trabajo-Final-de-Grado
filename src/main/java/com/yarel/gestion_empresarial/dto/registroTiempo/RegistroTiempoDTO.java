package com.yarel.gestion_empresarial.dto.registroTiempo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RegistroTiempoDTO {
    private Long id;
    private Long usuarioId;
    private Long tareaId;
    private Long proyectoId;
    private Long reunionId;
    private LocalDate fechaRegistro;

    // Campos originales que causan el problema
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;

    // Campos temporales para el binding del formulario (no se mapean a la base de datos)
    private transient String fechaInicioTemp;
    private transient Integer horaInicioTemp;
    private transient Integer minutosInicioTemp;
    private transient String fechaFinTemp;
    private transient Integer horaFinTemp;
    private transient Integer minutosFinTemp;

    private Double horasTrabajadas;
    private String comentario;

    // Campo adicional para mostrar el nombre del usuario en los informes
    private String usuarioNombre;

    // Nuevo campo para almacenar el tipo de registro
    private String tipoRegistro;
}
