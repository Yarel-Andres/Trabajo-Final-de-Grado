package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "proyectos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"registrosTiempo", "empleados"})
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jefe_id")
    private Jefe jefe;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin_estimada")
    private LocalDate fechaFinEstimada;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @Column
    private String estado;

    @Column
    private Double presupuesto;


    @Column(name = "completado", nullable = false)
    private boolean completado = false;

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }


    @OneToMany(mappedBy = "proyecto", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<RegistroTiempo> registrosTiempo = new HashSet<>();

    // Relación muchos a muchos con Empleado para que se le puedan asginar proyectos
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "proyecto_empleados",
            joinColumns = @JoinColumn(name = "proyecto_id"),
            inverseJoinColumns = @JoinColumn(name = "empleado_id")
    )
    private Set<Empleado> empleados = new HashSet<>();

    // Implementación personalizada de hashCode para evitar ciclos infinitos
    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, fechaInicio);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proyecto proyecto = (Proyecto) o;
        return Objects.equals(id, proyecto.id) &&
                Objects.equals(nombre, proyecto.nombre) &&
                Objects.equals(fechaInicio, proyecto.fechaInicio);
    }

    // Añade esta relación a la clase Proyecto.java
    @OneToMany(mappedBy = "proyecto", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Presupuesto> presupuestos = new HashSet<>();
}
