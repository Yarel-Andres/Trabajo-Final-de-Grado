package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "presupuestos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreCliente;

    // Proyecto asociado al presupuesto
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    // Personal de RRHH que crea el presupuesto
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rrhh_id")
    private RRHH creador;

    @Column(nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();

    @Column
    private Double tarifaHora;

    @Column
    private Double horasEstimadas;

    @Column
    private Double costoTotal;

    @Column
    private String descripcion;

    // Estados= BORRADOR, ENVIADO, ACEPTADO, RECHAZADO
    @Column
    private String estado = "BORRADOR";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Presupuesto that = (Presupuesto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
