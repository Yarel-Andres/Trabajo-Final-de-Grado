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

import java.time.LocalDateTime;
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

    // Obtiene todas las tareas
    @Transactional(readOnly = true)
    public List<TareaDTO> findAll() {
        return tareaMapper.toDtoList(tareaRepository.findAll());
    }

    // Guarda una tarea nueva
    @Transactional
    public TareaDTO save(TareaDTO tareaDTO) {
        Tarea tarea = tareaMapper.toEntity(tareaDTO);

        // Buscar empleado por ID
        Empleado empleado = empleadoRepository.findById(tareaDTO.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + tareaDTO.getEmpleadoId()));

        // Buscar jefe por ID
        Jefe jefe = jefeRepository.findById(tareaDTO.getJefeId())
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado con ID: " + tareaDTO.getJefeId()));

        // Buscar proyecto si se proporciona
        if (tareaDTO.getProyectoId() != null) {
            Proyecto proyecto = proyectoRepository.findById(tareaDTO.getProyectoId())
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + tareaDTO.getProyectoId()));
            tarea.setProyecto(proyecto);
        }

        tarea.setEmpleado(empleado);
        tarea.setJefe(jefe);

        return tareaMapper.toDto(tareaRepository.save(tarea));
    }

    // Guarda tarea asignada por un jefe
    @Transactional
    public TareaDTO saveTareaForJefe(TareaDTO tareaDTO, String jefeNombreUsuario) {
        Tarea tarea = tareaMapper.toEntity(tareaDTO);

        // Buscar empleado por ID
        Empleado empleado = empleadoRepository.findById(tareaDTO.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + tareaDTO.getEmpleadoId()));

        // Buscar jefe por nombre de usuario
        Jefe jefe = jefeRepository.findByNombreUsuario(jefeNombreUsuario)
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado con nombre de usuario: " + jefeNombreUsuario));

        // Buscar proyecto si se proporciona
        if (tareaDTO.getProyectoId() != null) {
            Proyecto proyecto = proyectoRepository.findById(tareaDTO.getProyectoId())
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + tareaDTO.getProyectoId()));
            tarea.setProyecto(proyecto);

            // Añadir empleado al proyecto si no está asignado
            if (!proyecto.getEmpleados().contains(empleado)) {
                proyecto.getEmpleados().add(empleado);
                proyectoRepository.save(proyecto);
            }
        }

        tarea.setEmpleado(empleado);
        tarea.setJefe(jefe);

        return tareaMapper.toDto(tareaRepository.save(tarea));
    }

    // Busca tareas de un empleado
    @Transactional(readOnly = true)
    public List<TareaDTO> findByEmpleadoId(Long empleadoId) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + empleadoId));

        return tareaMapper.toDtoList(tareaRepository.findByEmpleado(empleado));
    }

    // Busca tareas completadas de un empleado
    @Transactional(readOnly = true)
    public List<TareaDTO> findCompletadasByEmpleadoId(Long empleadoId) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + empleadoId));

        List<Tarea> tareasCompletadas = tareaRepository.findByEmpleado(empleado).stream()
                .filter(Tarea::isCompletada)
                .collect(Collectors.toList());

        return tareaMapper.toDtoList(tareasCompletadas);
    }

    // Finaliza una tarea
    @Transactional
    public TareaDTO finalizarTarea(Long tareaId) {
        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + tareaId));

        tarea.setCompletada(true);
        tarea.setFechaCompletada(LocalDateTime.now());

        return tareaMapper.toDto(tareaRepository.save(tarea));
    }

    // Busca tareas por proyecto
    @Transactional(readOnly = true)
    public List<TareaDTO> findByProyectoId(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + proyectoId));

        return tareaMapper.toDtoList(tareaRepository.findByProyecto(proyecto));
    }

    // Obtiene una tarea por ID
    @Transactional(readOnly = true)
    public Optional<TareaDTO> findById(Long id) {
        return tareaRepository.findById(id).map(tareaMapper::toDto);
    }

    // Busca tareas asignadas por un jefe
    @Transactional(readOnly = true)
    public List<TareaDTO> findByJefeId(Long jefeId) {
        Jefe jefe = jefeRepository.findById(jefeId)
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado con ID: " + jefeId));

        return tareaMapper.toDtoList(tareaRepository.findByJefe(jefe));
    }

    // Agrupa tareas por proyecto
    @Transactional(readOnly = true)
    public Map<String, List<TareaDTO>> findByJefeIdGroupByProyecto(Long jefeId) {
        List<TareaDTO> tareas = findByJefeId(jefeId);

        // Agrupar por proyecto
        return tareas.stream()
                .collect(Collectors.groupingBy(
                        tarea -> tarea.getNombreProyecto() != null ? tarea.getNombreProyecto() : "Sin proyecto"
                ));
    }

    // Eliminar tarea
    @Transactional
    public void eliminarTarea(Long tareaId) {
        // Verificar que la tarea existe
        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + tareaId));

        // Verificar que la tarea está completada
        if (!tarea.isCompletada()) {
            throw new RuntimeException("Solo se pueden eliminar tareas completadas");
        }

        // Desasociar los registros de tiempo de esta tarea (establecer tarea_id a null)
        // pero mantener los registros para informes de RRHH
        if (tarea.getRegistrosTiempo() != null && !tarea.getRegistrosTiempo().isEmpty()) {
            tarea.getRegistrosTiempo().forEach(registro -> registro.setTarea(null));
        }

        // Eliminar la tarea
        tareaRepository.delete(tarea);
    }
}
