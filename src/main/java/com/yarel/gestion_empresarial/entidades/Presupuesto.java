package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "presupuestos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreCliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

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

    @Column
    private String estado = "BORRADOR"; // BORRADOR, ENVIADO, ACEPTADO, RECHAZADO

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
