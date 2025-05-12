package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaMapper;
import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Proyecto;
import com.yarel.gestion_empresarial.entidades.Tarea;
import com.yarel.gestion_empresarial.repositorios.EmpleadoRepository;
import com.yarel.gestion_empresarial.repositorios.JefeRepository;
import com.yarel.gestion_empresarial.repositorios.ProyectoRepository;
import com.yarel.gestion_empresarial.repositorios.TareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final TareaRepository tareaRepository;
    private final TareaMapper tareaMapper;
    private final EmpleadoRepository empleadoRepository;
    private final JefeRepository jefeRepository;
    private final ProyectoRepository proyectoRepository; // Nuevo repositorio

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

        // Buscar el proyecto por ID (si se proporciona)
        if (tareaDTO.getProyectoId() != null) {
            Proyecto proyecto = proyectoRepository.findById(tareaDTO.getProyectoId())
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + tareaDTO.getProyectoId()));
            tarea.setProyecto(proyecto);
        }

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

        // Buscar el proyecto por ID (si se proporciona)
        if (tareaDTO.getProyectoId() != null) {
            Proyecto proyecto = proyectoRepository.findById(tareaDTO.getProyectoId())
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + tareaDTO.getProyectoId()));
            tarea.setProyecto(proyecto);

            // Verificar que el empleado esté asignado al proyecto
            if (!proyecto.getEmpleados().contains(empleado)) {
                // Opcionalmente, añadir el empleado al proyecto si no está asignado
                proyecto.getEmpleados().add(empleado);
                proyectoRepository.save(proyecto);
            }
        }

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

    // Método para buscar tareas por proyecto
    @Transactional(readOnly = true)
    public List<TareaDTO> findByProyectoId(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + proyectoId));

        List<Tarea> tareas = tareaRepository.findByProyecto(proyecto);
        return tareaMapper.toDtoList(tareas);
    }

    // Método para obtener una tarea por ID
    @Transactional(readOnly = true)
    public Optional<TareaDTO> findById(Long id) {
        return tareaRepository.findById(id)
                .map(tareaMapper::toDto);
    }

    // Método para buscar tareas asignadas por un jefe
    @Transactional(readOnly = true)
    public List<TareaDTO> findByJefeId(Long jefeId) {
        Jefe jefe = jefeRepository.findById(jefeId)
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado con ID: " + jefeId));

        List<Tarea> tareas = tareaRepository.findByJefe(jefe);
        return tareaMapper.toDtoList(tareas);
    }

    // Método para agrupar tareas por proyecto
    @Transactional(readOnly = true)
    public Map<String, List<TareaDTO>> findByJefeIdGroupByProyecto(Long jefeId) {
        List<TareaDTO> tareas = findByJefeId(jefeId);

        // Agrupar por proyecto
        return tareas.stream()
                .collect(Collectors.groupingBy(
                        tarea -> tarea.getNombreProyecto() != null ? tarea.getNombreProyecto() : "Sin proyecto"
                ));
    }
}
