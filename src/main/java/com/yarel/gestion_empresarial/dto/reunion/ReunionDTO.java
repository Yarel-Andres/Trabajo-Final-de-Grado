package com.yarel.gestion_empresarial.dto.reunion;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ReunionDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaHora;
    private String sala;
    private Long organizadorId;
    private Set<Long> participantesIds;
    private Set<Long> registrosTiempoIds;
}