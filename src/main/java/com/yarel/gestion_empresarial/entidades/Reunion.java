package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reuniones")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column
    private String sala;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizador_id")
    private Jefe organizador;

    // una reunión puede tener múltiples participantes y un usuario puede participar en varias reuniones
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "reunion_participantes",
            joinColumns = @JoinColumn(name = "reunion_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> participantes = new HashSet<>();

    // una reunión puede tener múltiples registros de tiempo asociados
    // cualquier cambio en Reunion afectará también a RegistroTiempo
    @OneToMany(mappedBy = "reunion", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<RegistroTiempo> registrosTiempo = new HashSet<>();
}

