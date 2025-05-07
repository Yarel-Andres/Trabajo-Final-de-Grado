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

    @Transactional(readOnly = true)
    public List<JefeDTO> findAll() {
        List<Jefe> jefes = jefeRepository.findAll();
        return jefeMapper.toDtoList(jefes);
    }

    @Transactional(readOnly = true)
    public Optional<JefeDTO> findById(Long id) {
        return jefeRepository.findById(id)
                .map(jefeMapper::toDto);
    }

    @Transactional
    public JefeDTO save(JefeDTO jefeDTO) {
        Jefe jefe = jefeMapper.toEntity(jefeDTO);
        jefe = jefeRepository.save(jefe);
        return jefeMapper.toDto(jefe);
    }

    @Transactional
    public Optional<JefeDTO> update(Long id, JefeDTO jefeDTO) {
        if (!jefeRepository.existsById(id)) {
            return Optional.empty();
        }

        jefeDTO.setId(id);
        Jefe jefe = jefeMapper.toEntity(jefeDTO);
        jefe = jefeRepository.save(jefe);
        return Optional.of(jefeMapper.toDto(jefe));
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (!jefeRepository.existsById(id)) {
            return false;
        }

        jefeRepository.deleteById(id);
        return true;
    }
}
