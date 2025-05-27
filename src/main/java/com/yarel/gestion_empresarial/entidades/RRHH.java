package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "rrhh")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("RRHH")
public class RRHH extends Usuario {

    // Área de especialización del personal de RRHH
    @Column(name = "area_especializacion")
    private String areaEspecializacion;

    // Certificaciones profesionales obtenidas
    @Column(name = "certificaciones")
    private String certificaciones;

    // Fecha de incorporación al departamento de RRHH
    @Column(name = "fecha_incorporacion_rrhh")
    private LocalDate fechaIncorporacionRRHH;
}
