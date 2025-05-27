package com.yarel.gestion_empresarial.dto.reunion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Data
public class ReunionDTO {
    // Datos básicos de la reunión
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime fechaHora;

    @NotBlank(message = "La sala es obligatoria")
    private String sala;

    // Estado de la reunión
    private boolean completada = false;
    private LocalDateTime fechaCompletada;

    // Relaciones - Organizador
    private Long organizadorId;
    private String organizadorNombre; // Para mostrar en vistas

    // Relaciones - Participantes
    private Set<Long> participantesIds;
    private Set<String> participantesNombres = new HashSet<>(); // Para mostrar en vistas

    // Registros de tiempo asociados
    private Set<Long> registrosTiempoIds;

    // Asegurar que participantesIds nunca sea null
    public Set<Long> getParticipantesIds() {
        if (participantesIds == null) {
            participantesIds = new HashSet<>();
        }
        return participantesIds;
    }

    // Asegurar que participantesNombres nunca sea null
    public Set<String> getParticipantesNombres() {
        if (participantesNombres == null) {
            participantesNombres = new HashSet<>();
        }
        return participantesNombres;
    }
}