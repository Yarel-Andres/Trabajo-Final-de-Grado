package com.yarel.gestion_empresarial.dto.usuario;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombreUsuario;
    private String correo;
    private String nombreCompleto;
    private String rol;
    private boolean activo;
}
