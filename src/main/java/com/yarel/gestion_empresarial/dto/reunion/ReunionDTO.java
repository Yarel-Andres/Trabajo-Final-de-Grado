package com.yarel.gestion_empresarial.dto.reunion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Data
public class ReunionDTO {
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime fechaHora;

    @NotBlank(message = "La sala es obligatoria")
    private String sala;

    private Long organizadorId;

    private Set<Long> participantesIds;
    private Set<Long> registrosTiempoIds;

    @Override
    public String toString() {
        return "ReunionDTO{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fechaHora=" + fechaHora +
                ", sala='" + sala + '\'' +
                ", organizadorId=" + organizadorId +
                ", participantesIds=" + participantesIds +
                '}';
    }

    // Asegúrate de que participantesIds nunca sea null
    public Set<Long> getParticipantesIds() {
        if (participantesIds == null) {
            participantesIds = new HashSet<>();
        }
        return participantesIds;
    }
}
