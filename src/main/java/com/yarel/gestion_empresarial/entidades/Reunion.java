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

@Entity
@Table(name = "reuniones")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    // Añadir estos campos a la clase Reunion
    @Column(nullable = false)
    private boolean completada = false;

    @Column(name = "fecha_completada")
    private LocalDateTime fechaCompletada;

    @Column
    private String sala;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizador_id")
    private Jefe organizador;

    // una reunión puede tener múltiples participantes y un usuario puede participar en varias reuniones
    @ManyToMany(fetch = FetchType.EAGER)
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

    // Implementación personalizada de hashCode para evitar ciclos infinitos
    @Override
    public int hashCode() {
        return Objects.hash(id, titulo, fechaHora, sala);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reunion reunion = (Reunion) o;
        return Objects.equals(id, reunion.id) &&
                Objects.equals(titulo, reunion.titulo) &&
                Objects.equals(fechaHora, reunion.fechaHora) &&
                Objects.equals(sala, reunion.sala);
    }
}
