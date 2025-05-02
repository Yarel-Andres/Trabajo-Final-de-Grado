package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "jefes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("JEFE")
public class Jefe extends Usuario {

    @Column(name = "fecha_nombramiento")
    private LocalDate fechaNombramiento;

    @Column(name = "nivel_responsabilidad")
    private String nivelResponsabilidad;

    // Un jefe puede asignar varias tareas
    @OneToMany(mappedBy = "jefe", fetch = FetchType.LAZY)
    private Set<Tarea> tareasAsignadas = new HashSet<>();

    // Un jefe puede gestionar varios proyectos
    @OneToMany(mappedBy = "jefe", fetch = FetchType.LAZY)
    private Set<Proyecto> proyectos = new HashSet<>();

    // Un jefe puede programar varias reuniones
    @OneToMany(mappedBy = "organizador", fetch = FetchType.LAZY)
    private Set<Reunion> reuniones = new HashSet<>();
}
