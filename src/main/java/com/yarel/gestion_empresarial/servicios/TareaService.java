package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaMapper;
import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Tarea;
import com.yarel.gestion_empresarial.repositorios.EmpleadoRepository;
import com.yarel.gestion_empresarial.repositorios.JefeRepository;
import com.yarel.gestion_empresarial.repositorios.TareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final TareaRepository tareaRepository;
    private final TareaMapper tareaMapper;
    private final EmpleadoRepository empleadoRepository;
    private final JefeRepository jefeRepository;

    // Metodo para obtener todas las tareas
    @Transactional(readOnly = true)
    public List<TareaDTO> findAll() {
        List<Tarea> tareas = tareaRepository.findAll();
        return tareaMapper.toDtoList(tareas);
    }

    // Metodo para guardar una tarea (sin jefe especificado)
    @Transactional
    public TareaDTO save(TareaDTO tareaDTO) {
        Tarea tarea = tareaMapper.toEntity(tareaDTO);

        // Buscar el empleado por ID
        Empleado empleado = empleadoRepository.findById(tareaDTO.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + tareaDTO.getEmpleadoId()));

        // Buscar el jefe por ID
        Jefe jefe = jefeRepository.findById(tareaDTO.getJefeId())
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado con ID: " + tareaDTO.getJefeId()));

        // Asignar las referencias a la entidad
        tarea.setEmpleado(empleado);
        tarea.setJefe(jefe);

        tarea = tareaRepository.save(tarea);
        return tareaMapper.toDto(tarea);
    }

    // Metodo para guardar una tarea asignada por un jefe
    @Transactional
    public TareaDTO saveTareaForJefe(TareaDTO tareaDTO, String jefeNombreUsuario) {
        Tarea tarea = tareaMapper.toEntity(tareaDTO);

        // Buscar el empleado por ID
        Empleado empleado = empleadoRepository.findById(tareaDTO.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + tareaDTO.getEmpleadoId()));

        // Buscar el jefe por nombre de usuario
        Jefe jefe = jefeRepository.findByNombreUsuario(jefeNombreUsuario)
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado con nombre de usuario: " + jefeNombreUsuario));

        // Asignar las referencias
        tarea.setEmpleado(empleado);
        tarea.setJefe(jefe);

        tarea = tareaRepository.save(tarea);
        return tareaMapper.toDto(tarea);
    }


    // Metodo para buscar tareas de un empleado
    @Transactional(readOnly = true)
    public List<TareaDTO> findByEmpleadoId(Long empleadoId) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + empleadoId));

        List<Tarea> tareas = tareaRepository.findByEmpleado(empleado);
        return tareaMapper.toDtoList(tareas);
    }
}

