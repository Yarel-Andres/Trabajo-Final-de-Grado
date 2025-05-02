package com.yarel.gestion_empresarial.dto.empleado;

import com.yarel.gestion_empresarial.dto.usuario.UsuarioDTO;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmpleadoDTO extends UsuarioDTO {
    private LocalDate fechaContratacion;
    private String puesto;
    private String telefono;
    private Double salario;
}