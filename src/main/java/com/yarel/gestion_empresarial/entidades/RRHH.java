package com.yarel.gestion_empresarial.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "rrhh")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("RRHH")
public class RRHH extends Usuario {

    @Column(name = "area_especializacion")
    private String areaEspecializacion;

    @Column(name = "certificaciones")
    private String certificaciones;

    @Column(name = "fecha_incorporacion_rrhh")
    private LocalDate fechaIncorporacionRRHH;
}

