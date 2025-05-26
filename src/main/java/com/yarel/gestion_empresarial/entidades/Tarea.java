package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

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

    // Se almacena como tipo TEXT en la base de datos, lo que permite grandes cantidades de texto.
    @Column(columnDefinition = "TEXT")
    private String descripcion;


    // Eager indica que la relación se cargará inmediatamente con la tarea
    @ManyToOne(fetch = FetchType.EAGER)
    // indica la columna que almacenará la referencia al empleado
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jefe_id")
    private Jefe jefe;

    // Relación con Proyecto - Una tarea pertenece a un proyecto
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @Column(name = "fecha_creacion")
    // Se inicializa automáticamente con la fecha y hora actual
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column
    private boolean completada = false;

    // Guarda la fecha en que la tarea fue completada
    @Column(name = "fecha_completada")
    private LocalDateTime fechaCompletada;

    @Column
    private String prioridad;
    // permite que cualquier cambio en Tarea afecte también a RegistroTiempo
    // (por ejemplo, si se elimina una tarea, también se eliminan sus registros de tiempo)
    @OneToMany(mappedBy = "tarea", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private Set<RegistroTiempo> registrosTiempo = new HashSet<>();

    // Implementación personalizada de hashCode para evitar ciclos infinitos
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
