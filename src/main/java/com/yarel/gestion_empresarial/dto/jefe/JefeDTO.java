package com.yarel.gestion_empresarial.dto.jefe;

import com.yarel.gestion_empresarial.dto.usuario.UsuarioDTO;
import lombok.Data;
import java.time.LocalDate;

@Data
public class JefeDTO extends UsuarioDTO {
    private LocalDate fechaNombramiento;
    private String nivelResponsabilidad;
}

