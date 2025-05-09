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

    // un usuario puede tener múltiples registros de tiempo
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tarea_id")
    private Tarea tarea;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reunion_id")
    private Reunion reunion;

    // registra la fecha en que se creó el registro de tiempo.
    // Se inicializa automáticamente con la fecha actual
    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro = LocalDate.now();

    @Column(name = "hora_inicio")
    private LocalDateTime horaInicio;

    @Column(name = "hora_fin")
    private LocalDateTime horaFin;

    @Column(name = "horas_trabajadas")
    private Double horasTrabajadas;

    @Column
    private String comentario;

    // Implementación personalizada de hashCode para evitar ciclos infinitos
    @Override
    public int hashCode() {
        return Objects.hash(id, fechaRegistro, horaInicio, horaFin);
    }

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
