package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.proyecto.ProyectoDTO;
import com.yarel.gestion_empresarial.dto.proyecto.ProyectoMapper;
import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Proyecto;
import com.yarel.gestion_empresarial.repositorios.EmpleadoRepository;
import com.yarel.gestion_empresarial.repositorios.JefeRepository;
import com.yarel.gestion_empresarial.repositorios.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final ProyectoMapper proyectoMapper;
    private final JefeRepository jefeRepository;
    private final EmpleadoRepository empleadoRepository;

    // Obtiene todos los proyectos
    @Transactional(readOnly = true)
    public List<ProyectoDTO> findAll() {
        List<Proyecto> proyectos = proyectoRepository.findAll();
        return proyectoMapper.toDtoList(proyectos);
    }

    // Obtiene un proyecto por Id
    @Transactional(readOnly = true)
    public Optional<ProyectoDTO> findById(Long id) {
        return proyectoRepository.findById(id)
                .map(proyectoMapper::toDto);
    }

    // Crear proyecto y asignarlo a empleados
    @Transactional
    public ProyectoDTO saveProyectoForJefe(ProyectoDTO proyectoDTO, String jefeNombreUsuario) {
        try {
            // Buscar el jefe por nombre de usuario
            Jefe jefe = jefeRepository.findByNombreUsuario(jefeNombreUsuario)
                    .orElseThrow(() -> new RuntimeException("Jefe no encontrado con nombre de usuario: " + jefeNombreUsuario));

            // Asignar el ID del jefe al DTO
            proyectoDTO.setJefeId(jefe.getId());

            // Usar el mapper para convertir DTO a entidad
            Proyecto proyecto = proyectoMapper.toEntity(proyectoDTO);

            // Asegurarse de que el jefe esté correctamente establecido
            proyecto.setJefe(jefe);

            // Inicializar conjunto de empleados si es null
            if (proyecto.getEmpleados() == null) {
                proyecto.setEmpleados(new HashSet<>());
            }

            // Añadir los empleados seleccionados
            if (proyectoDTO.getEmpleadosIds() != null) {
                for (Long empleadoId : proyectoDTO.getEmpleadosIds()) {
                    empleadoRepository.findById(empleadoId).ifPresent(
                            empleado -> proyecto.getEmpleados().add(empleado)
                    );
                }
            }

            // Guardar el proyecto
            Proyecto proyectoGuardado = proyectoRepository.save(proyecto);

            // Usar el mapper para convertir la entidad guardada de vuelta a DTO
            return proyectoMapper.toDto(proyectoGuardado);
        } catch (Exception e) {
            // Registra el error detallado
            System.err.println("Error al guardar el proyecto: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar para que el controlador la maneje
        }
    }

    // Obtiene proyectos por ID de empleado
    @Transactional(readOnly = true)
    public List<ProyectoDTO> findByEmpleadoId(Long empleadoId) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + empleadoId));

        List<Proyecto> proyectos = proyectoRepository.findByEmpleadosContaining(empleado);
        return proyectoMapper.toDtoList(proyectos);
    }

    // Obtiene proyectos por ID de jefe
    @Transactional(readOnly = true)
    public List<ProyectoDTO> findByJefeId(Long jefeId) {
        Jefe jefe = jefeRepository.findById(jefeId)
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado con ID: " + jefeId));

        List<Proyecto> proyectos = proyectoRepository.findByJefe(jefe);
        return proyectoMapper.toDtoList(proyectos);
    }

    // Obtiene proyectos finalizados
    @Transactional
    public void finalizarProyecto(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + proyectoId));

        proyecto.setCompletado(true);
        proyecto.setFechaFinReal(LocalDate.now());

        proyectoRepository.save(proyecto);
    }

    // Añadir método para obtener proyectos completados por empleado
    @Transactional(readOnly = true)
    public List<ProyectoDTO> findCompletadosByEmpleadoId(Long empleadoId) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + empleadoId));

        List<Proyecto> proyectos = proyectoRepository.findByEmpleadosContaining(empleado);

        // Filtrar solo los proyectos completados
        List<Proyecto> proyectosCompletados = proyectos.stream()
                .filter(Proyecto::isCompletado)
                .collect(Collectors.toList());

        return proyectoMapper.toDtoList(proyectosCompletados);
    }
}
