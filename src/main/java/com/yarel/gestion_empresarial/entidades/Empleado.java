package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Permiten manejar una colección de Tarea relacionada con cada Empleado
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "empleados")
@Data
@NoArgsConstructor
@AllArgsConstructor
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


    // Define una relacion de 1 a n entre empleado y tarea, un empleado puede tener muchas tareas
                                    // las tareas del empleado no se cargarán inmediatamente, sino solo cuando sean necesarias
    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY)
    // Se usa Set<Tarea> para evitar tareas duplicadas y new HashSet<>() inicializa la colección vacía.
    private Set<Tarea> tareas = new HashSet<>();
}

