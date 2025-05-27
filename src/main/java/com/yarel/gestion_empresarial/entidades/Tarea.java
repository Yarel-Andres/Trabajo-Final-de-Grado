package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

// Entidad que representa una tarea en el sistema
@Entity
@Table(name = "tareas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"registrosTiempo"})
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    // Descripción almacenada como TEXT para permitir contenido extenso
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Relación con empleado asignado - EAGER para cargar inmediatamente
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    // Relación con jefe que asigna la tarea
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jefe_id")
    private Jefe jefe;

    // Relación con proyecto al que pertenece la tarea
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    // Fecha de creación automática
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column
    private boolean completada = false;

    // Fecha en que se completó la tarea
    @Column(name = "fecha_completada")
    private LocalDateTime fechaCompletada;

    @Column
    private String prioridad;

    // Relación con registros de tiempo - LAZY para optimizar rendimiento
    @OneToMany(mappedBy = "tarea", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private Set<RegistroTiempo> registrosTiempo = new HashSet<>();

    // Implementación personalizada para evitar ciclos infinitos
    @Override
    public int hashCode() {
        return Objects.hash(id, titulo, fechaCreacion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarea tarea = (Tarea) o;
        return Objects.equals(id, tarea.id) &&
                Objects.equals(titulo, tarea.titulo) &&
                Objects.equals(fechaCreacion, tarea.fechaCreacion);
    }
}
