package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.jefe.JefeDTO;
import com.yarel.gestion_empresarial.dto.jefe.JefeMapper;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.repositorios.JefeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class JefeService {

    private final JefeRepository jefeRepository;
    private final JefeMapper jefeMapper;

    @Autowired
    public JefeService(JefeRepository jefeRepository, JefeMapper jefeMapper) {
        this.jefeRepository = jefeRepository;
        this.jefeMapper = jefeMapper;
    }

    // Obtiene todos los jefes para listas desplegables
    @Transactional(readOnly = true)
    public List<JefeDTO> findAll() {
        return jefeMapper.toDtoList(jefeRepository.findAll());
    }

    // Busca jefe por ID para validaciones
    @Transactional(readOnly = true)
    public Optional<JefeDTO> findById(Long id) {
        return jefeRepository.findById(id).map(jefeMapper::toDto);
    }
}
