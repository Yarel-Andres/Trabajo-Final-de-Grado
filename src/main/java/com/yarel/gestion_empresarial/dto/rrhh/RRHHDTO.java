package com.yarel.gestion_empresarial.dto.rrhh;

import com.yarel.gestion_empresarial.dto.usuario.UsuarioDTO;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RRHHDTO extends UsuarioDTO {
    private String areaEspecializacion;
    private String certificaciones;
    private LocalDate fechaIncorporacionRRHH;
}