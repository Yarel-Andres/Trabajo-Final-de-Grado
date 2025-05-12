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
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;
    private Double horasTrabajadas;
    private String comentario;

    // Campo adicional para mostrar el nombre del usuario en los informes
    private String usuarioNombre;
}
