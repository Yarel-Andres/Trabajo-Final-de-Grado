package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "empleados")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Incluye campos de Usuario en equals/hashCode, excluye colecciones para evitar ciclos infinitos
@EqualsAndHashCode(callSuper = true, exclude = {"tareas", "proyectos"})
@DiscriminatorValue("EMPLEADO")
public class Empleado extends Usuario {

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @Column
    private String puesto;

    @Column
    private String telefono;

    @Column
    private Double salario;

    // Relación uno a muchos: un empleado puede tener muchas tareas
    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY)
    private Set<Tarea> tareas = new HashSet<>();

    // Relación muchos a muchos: empleados pueden participar en múltiples proyectos
    @ManyToMany(mappedBy = "empleados", fetch = FetchType.LAZY)
    private Set<Proyecto> proyectos = new HashSet<>();
}
