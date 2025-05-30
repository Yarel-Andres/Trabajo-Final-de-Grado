package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reuniones")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Excluye colecciones para evitar ciclos infinitos en equals/hashCode
@EqualsAndHashCode(exclude = {"participantes", "registrosTiempo"})
public class Reunion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private boolean completada = false;

    @Column(name = "fecha_completada")
    private LocalDateTime fechaCompletada;

    @Column
    private String sala;

    // Jefe que organiza la reunión
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizador_id")
    private Jefe organizador;

    // Empleados que participan en la reunión
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "reunion_participantes",
            joinColumns = @JoinColumn(name = "reunion_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> participantes = new HashSet<>();

    // Registros de tiempo asociados a la reunión
    @OneToMany(mappedBy = "reunion", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<RegistroTiempo> registrosTiempo = new HashSet<>();
}
