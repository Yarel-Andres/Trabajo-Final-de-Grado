package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_tiempo")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
