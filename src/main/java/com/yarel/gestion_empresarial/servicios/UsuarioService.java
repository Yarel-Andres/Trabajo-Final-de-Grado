// ** Pendiente de revisar y modificar en su caso. Esta creado de momento para crear la visual de reuniones
package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.usuario.UsuarioDTO;
import com.yarel.gestion_empresarial.dto.usuario.UsuarioMapper;
import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    /**
     * Obtiene todos los usuarios del sistema
     * @return Lista de DTOs de usuarios
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarioMapper.toDtoList(usuarios);
    }

    /**
     * Busca un usuario por su ID
     * @param id ID del usuario
     * @return DTO del usuario encontrado o vacío si no existe
     */
    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findById(Long id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toDto);
    }

    /**
     * Busca un usuario por su nombre de usuario
     * @param nombreUsuario Nombre de usuario
     * @return DTO del usuario encontrado o vacío si no existe
     */
    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findByNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .map(usuarioMapper::toDto);
    }

    /**
     * Busca un usuario por su correo electrónico
     * @param correo Correo electrónico
     * @return DTO del usuario encontrado o vacío si no existe
     */
    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .map(usuarioMapper::toDto);
    }

    /**
     * Verifica si existe un usuario con el nombre de usuario dado
     * @param nombreUsuario Nombre de usuario a verificar
     * @return true si existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean existsByNombreUsuario(String nombreUsuario) {
        return usuarioRepository.existsByNombreUsuario(nombreUsuario);
    }

    /**
     * Verifica si existe un usuario con el correo electrónico dado
     * @param correo Correo electrónico a verificar
     * @return true si existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean existsByCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }
}