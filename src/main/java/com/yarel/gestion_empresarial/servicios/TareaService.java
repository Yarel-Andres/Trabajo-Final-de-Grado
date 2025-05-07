package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaMapper;
import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Tarea;
import com.yarel.gestion_empresarial.repositorios.TareaRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final TareaRepository tareaRepository;
    private final TareaMapper tareaMapper;

    @Transactional(readOnly = true)
    public List<TareaDTO> findAll() {
        // obtiene una lista de entidades Empleado
        List<Tarea> empleados = tareaRepository.findAll();
        // Convierte esta lista en una lista de DTOs con empleadoMapper.toDtoList
        return tareaMapper.toDtoList(empleados);
    }

    @Transactional
    public TareaDTO save(TareaDTO tareaDTO) {
        Tarea tarea = tareaMapper.toEntity(tareaDTO);
        tarea = tareaRepository.save(tarea);
        return tareaMapper.toDto(tarea);
    }
}
