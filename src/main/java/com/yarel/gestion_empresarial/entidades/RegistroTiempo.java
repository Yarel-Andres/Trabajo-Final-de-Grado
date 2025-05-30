// Entidad que representa un registro de tiempo asociado a usuarios, tareas, proyectos y reuniones
package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "registros_tiempo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RegistroTiempo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Un usuario puede tener múltiples registros de tiempo
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Una tarea puede estar asociada a múltiples registros de tiempo
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tarea_id")
    private Tarea tarea;

    // Uun proyecto puede tener múltiples registros de tiempo
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    // Una reunión puede tener múltiples registros de tiempo
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reunion_id")
    private Reunion reunion;

    // Fecha en que se creó el registro de tiempo
    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro = LocalDate.now();

    // Hora de inicio del registro de tiempo
    @Column(name = "hora_inicio")
    private LocalDateTime horaInicio;

    // Hora de finalización del registro de tiempo
    @Column(name = "hora_fin")
    private LocalDateTime horaFin;

    // Cantidad de horas trabajadas en el registro
    @Column(name = "horas_trabajadas")
    private Double horasTrabajadas;

    // Comentario opcional sobre el registro de tiempo
    @Column
    private String comentario;

    // Implementación personalizada de hashCode para evitar ciclos infinitos
    @Override
    public int hashCode() {
        return Objects.hash(id, fechaRegistro, horaInicio, horaFin);
    }

    // Implementación de equals para comparar objetos de RegistroTiempo
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistroTiempo registro = (RegistroTiempo) o;
        return Objects.equals(id, registro.id) &&
                Objects.equals(fechaRegistro, registro.fechaRegistro) &&
                Objects.equals(horaInicio, registro.horaInicio) &&
                Objects.equals(horaFin, registro.horaFin);
    }
}

