package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.usuario.UsuarioDTO;
import com.yarel.gestion_empresarial.dto.usuario.UsuarioMapper;
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

    // Obtener todos los usuarios
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        return usuarioMapper.toDtoList(usuarioRepository.findAll());
    }

    // Buscar usuario por ID
    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findById(Long id) {
        return usuarioRepository.findById(id).map(usuarioMapper::toDto);
    }


    // Buscar usuario por nombre completo
    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findByNombreCompleto(String nombreCompleto) {
        return usuarioRepository.findByNombreCompleto(nombreCompleto).map(usuarioMapper::toDto);
    }


}
