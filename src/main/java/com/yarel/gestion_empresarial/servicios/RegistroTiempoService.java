package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.registroTiempo.RegistroTiempoDTO;
import com.yarel.gestion_empresarial.dto.registroTiempo.RegistroTiempoMapper;
import com.yarel.gestion_empresarial.entidades.RegistroTiempo;
import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.entidades.Tarea;
import com.yarel.gestion_empresarial.entidades.Proyecto;
import com.yarel.gestion_empresarial.entidades.Reunion;
import com.yarel.gestion_empresarial.repositorios.RegistroTiempoRepository;
import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import com.yarel.gestion_empresarial.repositorios.TareaRepository;
import com.yarel.gestion_empresarial.repositorios.ProyectoRepository;
import com.yarel.gestion_empresarial.repositorios.ReunionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;


@Service
@RequiredArgsConstructor
public class RegistroTiempoService {

    private final RegistroTiempoRepository registroTiempoRepository;
    private final RegistroTiempoMapper registroTiempoMapper;
    private final UsuarioRepository usuarioRepository;
    private final TareaRepository tareaRepository;
    private final ProyectoRepository proyectoRepository;
    private final ReunionRepository reunionRepository;

    // Obtener todos los registros de tiempo
    @Transactional(readOnly = true)
    public List<RegistroTiempoDTO> findAll() {
        List<RegistroTiempo> registros = registroTiempoRepository.findAll();
        List<RegistroTiempoDTO> registrosDTO = registroTiempoMapper.toDtoList(registros);

        // Determinar el tipo de registro para cada DTO
        for (int i = 0; i < registros.size(); i++) {
            RegistroTiempo registro = registros.get(i);
            RegistroTiempoDTO dto = registrosDTO.get(i);

            if (registro.getTarea() != null) {
                dto.setTipoRegistro("Tarea");
            } else if (registro.getProyecto() != null) {
                dto.setTipoRegistro("Proyecto");
            } else if (registro.getReunion() != null) {
                dto.setTipoRegistro("Reunión");
            } else {
                dto.setTipoRegistro("Tarea (Eliminada)");
            }
        }

        return registrosDTO;
    }

    // Obtener un registro por ID
    @Transactional(readOnly = true)
    public Optional<RegistroTiempoDTO> findById(Long id) {
        return registroTiempoRepository.findById(id)
                .map(registroTiempoMapper::toDto);
    }

    // Obtener registros por usuario
    @Transactional(readOnly = true)
    public List<RegistroTiempoDTO> findByUsuarioId(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        List<RegistroTiempo> registros = registroTiempoRepository.findByUsuario(usuario);
        List<RegistroTiempoDTO> registrosDTO = registroTiempoMapper.toDtoList(registros);

        // Completa DTOs con información adicional
        for (int i = 0; i < registros.size(); i++) {
            RegistroTiempoDTO dto = registrosDTO.get(i);
            RegistroTiempo registro = registros.get(i);

            dto.setUsuarioId(usuarioId);

            if (registro.getTarea() != null) {
                dto.setTipoRegistro("Tarea");
                dto.setTareaId(registro.getTarea().getId());
                if (registro.getTarea().getTitulo() != null) {
                    dto.setComentario("Tarea: " + registro.getTarea().getTitulo());
                }
            } else if (registro.getProyecto() != null) {
                dto.setTipoRegistro("Proyecto");
                dto.setProyectoId(registro.getProyecto().getId());
                if (registro.getProyecto().getNombre() != null) {
                    dto.setComentario("Proyecto: " + registro.getProyecto().getNombre());
                }
            } else if (registro.getReunion() != null) {
                dto.setTipoRegistro("Reunión");
                dto.setReunionId(registro.getReunion().getId());
                if (registro.getReunion().getTitulo() != null) {
                    dto.setComentario("Reunión: " + registro.getReunion().getTitulo());
                }
            } else {
                dto.setTipoRegistro("Tarea (Eliminada)");
                if (registro.getComentario() != null && !registro.getComentario().isEmpty()) {
                    dto.setComentario(registro.getComentario());
                } else {
                    dto.setComentario("Tiempo registrado en tarea eliminada");
                }
            }
        }

        return registrosDTO;
    }

    // Obtener registros por tarea
    @Transactional(readOnly = true)
    public List<RegistroTiempoDTO> findByTareaId(Long tareaId) {
        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + tareaId));

        List<RegistroTiempo> registros = registroTiempoRepository.findByTarea(tarea);
        List<RegistroTiempoDTO> registrosDTO = registroTiempoMapper.toDtoList(registros);

        // Completa DTOs con información del usuario y tarea
        for (int i = 0; i < registros.size(); i++) {
            RegistroTiempoDTO dto = registrosDTO.get(i);
            RegistroTiempo registro = registros.get(i);

            if (registro.getUsuario() != null) {
                dto.setUsuarioId(registro.getUsuario().getId());
                dto.setUsuarioNombre(registro.getUsuario().getNombreCompleto());
            }

            dto.setTipoRegistro("Tarea");
            if (tarea.getTitulo() != null) {
                dto.setComentario("Tarea: " + tarea.getTitulo());
            }
        }

        return registrosDTO;
    }

    // Obtener registros por proyecto
    @Transactional(readOnly = true)
    public List<RegistroTiempoDTO> findByProyectoId(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + proyectoId));

        List<RegistroTiempo> registros = registroTiempoRepository.findByProyecto(proyecto);
        List<RegistroTiempoDTO> registrosDTO = registroTiempoMapper.toDtoList(registros);

        // Completa DTOs con información del usuario y clasificar por tipo
        for (int i = 0; i < registros.size(); i++) {
            RegistroTiempo registro = registros.get(i);
            RegistroTiempoDTO dto = registrosDTO.get(i);

            if (registro.getUsuario() != null) {
                dto.setUsuarioId(registro.getUsuario().getId());
                dto.setUsuarioNombre(registro.getUsuario().getNombreCompleto());
            }

            if (registro.getTarea() != null) {
                dto.setTipoRegistro("Tarea");
                dto.setTareaId(registro.getTarea().getId());
                if (registro.getTarea().getTitulo() != null) {
                    dto.setComentario("Tarea: " + registro.getTarea().getTitulo());
                }
            } else if (registro.getReunion() != null) {
                dto.setTipoRegistro("Reunión");
                dto.setReunionId(registro.getReunion().getId());
                if (registro.getReunion().getTitulo() != null) {
                    dto.setComentario("Reunión: " + registro.getReunion().getTitulo());
                }
            } else {
                dto.setTipoRegistro("Proyecto");
                if (proyecto.getNombre() != null) {
                    dto.setComentario("Proyecto: " + proyecto.getNombre());
                }
            }
        }

        return registrosDTO;
    }

    // Obtener registros por reunión
    @Transactional(readOnly = true)
    public List<RegistroTiempoDTO> findByReunionId(Long reunionId) {
        List<RegistroTiempo> registros = registroTiempoRepository.findByReunionId(reunionId);
        List<RegistroTiempoDTO> registrosDTO = registroTiempoMapper.toDtoList(registros);

        // Completa DTOs con información del usuario
        for (int i = 0; i < registros.size(); i++) {
            RegistroTiempo registro = registros.get(i);
            RegistroTiempoDTO dto = registrosDTO.get(i);

            if (registro.getUsuario() != null) {
                dto.setUsuarioId(registro.getUsuario().getId());
                dto.setUsuarioNombre(registro.getUsuario().getNombreCompleto());
            }

            dto.setTipoRegistro("Reunión");
            if (registro.getReunion() != null && registro.getReunion().getTitulo() != null) {
                dto.setComentario("Reunión: " + registro.getReunion().getTitulo());
            }
        }

        return registrosDTO;
    }

    // Crear un nuevo registro de tiempo
    @Transactional
    public RegistroTiempoDTO save(RegistroTiempoDTO registroTiempoDTO) {
        RegistroTiempo registroTiempo = registroTiempoMapper.toEntity(registroTiempoDTO);

        // Establecer el usuario
        if (registroTiempoDTO.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(registroTiempoDTO.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + registroTiempoDTO.getUsuarioId()));
            registroTiempo.setUsuario(usuario);
        }

        // Establecer la tarea y automáticamente el proyecto asociado
        if (registroTiempoDTO.getTareaId() != null) {
            Tarea tarea = tareaRepository.findById(registroTiempoDTO.getTareaId())
                    .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + registroTiempoDTO.getTareaId()));
            registroTiempo.setTarea(tarea);
            registroTiempoDTO.setTipoRegistro("Tarea");

            if (tarea.getProyecto() != null) {
                registroTiempo.setProyecto(tarea.getProyecto());
                registroTiempoDTO.setProyectoId(tarea.getProyecto().getId());
            }

            // Guardar información histórica en comentario
            if (tarea.getTitulo() != null) {
                String comentarioExistente = registroTiempo.getComentario();
                String comentarioTarea = "Tarea: " + tarea.getTitulo();
                if (comentarioExistente != null && !comentarioExistente.isEmpty()) {
                    registroTiempo.setComentario(comentarioTarea + " - " + comentarioExistente);
                } else {
                    registroTiempo.setComentario(comentarioTarea);
                }
            }
        }

        // Establecer el proyecto si no se estableció por la tarea
        if (registroTiempoDTO.getProyectoId() != null && registroTiempo.getProyecto() == null) {
            Proyecto proyecto = proyectoRepository.findById(registroTiempoDTO.getProyectoId())
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + registroTiempoDTO.getProyectoId()));
            registroTiempo.setProyecto(proyecto);

            if (registroTiempoDTO.getTipoRegistro() == null) {
                registroTiempoDTO.setTipoRegistro("Proyecto");
            }
        }

        // Establecer la reunión
        if (registroTiempoDTO.getReunionId() != null) {
            Reunion reunion = reunionRepository.findById(registroTiempoDTO.getReunionId())
                    .orElseThrow(() -> new RuntimeException("Reunión no encontrada con ID: " + registroTiempoDTO.getReunionId()));
            registroTiempo.setReunion(reunion);
            registroTiempoDTO.setTipoRegistro("Reunión");
        }

        // Calcular horas trabajadas automáticamente
        if (registroTiempo.getHorasTrabajadas() == null &&
                registroTiempo.getHoraInicio() != null &&
                registroTiempo.getHoraFin() != null) {

            Duration duracion = Duration.between(registroTiempo.getHoraInicio(), registroTiempo.getHoraFin());
            double horas = duracion.toMinutes() / 60.0;
            registroTiempo.setHorasTrabajadas(horas);
        }

        // Establecer fecha de registro por defecto
        if (registroTiempo.getFechaRegistro() == null) {
            registroTiempo.setFechaRegistro(LocalDate.now());
        }

        registroTiempo = registroTiempoRepository.save(registroTiempo);
        RegistroTiempoDTO resultado = registroTiempoMapper.toDto(registroTiempo);

        // Mantener el tipo de registro después del mapeo
        if (registroTiempoDTO.getTipoRegistro() != null) {
            resultado.setTipoRegistro(registroTiempoDTO.getTipoRegistro());
        } else {
            if (resultado.getTareaId() != null) {
                resultado.setTipoRegistro("Tarea");
            } else if (resultado.getProyectoId() != null) {
                resultado.setTipoRegistro("Proyecto");
            } else if (resultado.getReunionId() != null) {
                resultado.setTipoRegistro("Reunión");
            }
        }

        return resultado;
    }

    // Crear un registro para un usuario específico
    @Transactional
    public RegistroTiempoDTO saveForUsuario(RegistroTiempoDTO registroTiempoDTO, String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con nombre: " + nombreUsuario));

        registroTiempoDTO.setUsuarioId(usuario.getId());
        return save(registroTiempoDTO);
    }


    // Eliminar un registro
    @Transactional
    public boolean deleteById(Long id) {
        if (!registroTiempoRepository.existsById(id)) {
            return false;
        }

        registroTiempoRepository.deleteById(id);
        return true;
    }

    // Obtener registros de tareas asignadas por un jefe
    @Transactional(readOnly = true)
    public List<RegistroTiempoDTO> findByJefeId(Long jefeId) {
        List<Tarea> tareasDelJefe = tareaRepository.findByJefeId(jefeId);

        if (tareasDelJefe.isEmpty()) {
            return new ArrayList<>();
        }

        List<RegistroTiempoDTO> todosLosRegistros = new ArrayList<>();

        for (Tarea tarea : tareasDelJefe) {
            List<RegistroTiempo> registrosDeTarea = registroTiempoRepository.findByTarea(tarea);
            List<RegistroTiempoDTO> registrosDTODeTarea = registroTiempoMapper.toDtoList(registrosDeTarea);

            // Enriquecer los DTOs con información adicional
            for (int i = 0; i < registrosDeTarea.size(); i++) {
                RegistroTiempo registro = registrosDeTarea.get(i);
                RegistroTiempoDTO dto = registrosDTODeTarea.get(i);

                if (registro.getUsuario() != null) {
                    dto.setUsuarioId(registro.getUsuario().getId());
                    dto.setUsuarioNombre(registro.getUsuario().getNombreCompleto());
                }

                dto.setTareaId(tarea.getId());
                dto.setTipoRegistro("Tarea");

                if (tarea.getTitulo() != null) {
                    dto.setComentario("Tarea: " + tarea.getTitulo());
                }

                if (tarea.getProyecto() != null) {
                    dto.setProyectoId(tarea.getProyecto().getId());
                }
            }

            todosLosRegistros.addAll(registrosDTODeTarea);
        }

        return todosLosRegistros;
    }
}
