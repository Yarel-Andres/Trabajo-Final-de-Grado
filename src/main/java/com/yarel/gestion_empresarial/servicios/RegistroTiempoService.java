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
        return registroTiempoMapper.toDtoList(registros);
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
        return registroTiempoMapper.toDtoList(registros);
    }

    // Obtener registros por tarea
    @Transactional(readOnly = true)
    public List<RegistroTiempoDTO> findByTareaId(Long tareaId) {
        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + tareaId));

        List<RegistroTiempo> registros = registroTiempoRepository.findByTarea(tarea);
        return registroTiempoMapper.toDtoList(registros);
    }

    // Obtener registros por proyecto
    @Transactional(readOnly = true)
    public List<RegistroTiempoDTO> findByProyectoId(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + proyectoId));

        List<RegistroTiempo> registros = registroTiempoRepository.findByProyecto(proyecto);
        return registroTiempoMapper.toDtoList(registros);
    }

    // Obtener registros por reunión
    @Transactional(readOnly = true)
    public List<RegistroTiempoDTO> findByReunionId(Long reunionId) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new RuntimeException("Reunión no encontrada con ID: " + reunionId));

        List<RegistroTiempo> registros = registroTiempoRepository.findByReunion(reunion);
        return registroTiempoMapper.toDtoList(registros);
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

        // Establecer la tarea (si aplica)
        if (registroTiempoDTO.getTareaId() != null) {
            Tarea tarea = tareaRepository.findById(registroTiempoDTO.getTareaId())
                    .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + registroTiempoDTO.getTareaId()));
            registroTiempo.setTarea(tarea);
        }

        // Establecer el proyecto (si aplica)
        if (registroTiempoDTO.getProyectoId() != null) {
            Proyecto proyecto = proyectoRepository.findById(registroTiempoDTO.getProyectoId())
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + registroTiempoDTO.getProyectoId()));
            registroTiempo.setProyecto(proyecto);
        }

        // Establecer la reunión (si aplica)
        if (registroTiempoDTO.getReunionId() != null) {
            Reunion reunion = reunionRepository.findById(registroTiempoDTO.getReunionId())
                    .orElseThrow(() -> new RuntimeException("Reunión no encontrada con ID: " + registroTiempoDTO.getReunionId()));
            registroTiempo.setReunion(reunion);
        }

        // Calcular horas trabajadas si no se proporcionaron
        if (registroTiempo.getHorasTrabajadas() == null &&
                registroTiempo.getHoraInicio() != null &&
                registroTiempo.getHoraFin() != null) {

            Duration duracion = Duration.between(registroTiempo.getHoraInicio(), registroTiempo.getHoraFin());
            double horas = duracion.toMinutes() / 60.0;
            registroTiempo.setHorasTrabajadas(horas);
        }

        // Establecer fecha de registro si no se proporcionó
        if (registroTiempo.getFechaRegistro() == null) {
            registroTiempo.setFechaRegistro(LocalDate.now());
        }

        registroTiempo = registroTiempoRepository.save(registroTiempo);
        return registroTiempoMapper.toDto(registroTiempo);
    }

    // Crear un registro de tiempo para un usuario específico
    @Transactional
    public RegistroTiempoDTO saveForUsuario(RegistroTiempoDTO registroTiempoDTO, String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con nombre: " + nombreUsuario));

        registroTiempoDTO.setUsuarioId(usuario.getId());
        return save(registroTiempoDTO);
    }

    // Actualizar un registro existente
    @Transactional
    public Optional<RegistroTiempoDTO> update(Long id, RegistroTiempoDTO registroTiempoDTO) {
        if (!registroTiempoRepository.existsById(id)) {
            return Optional.empty();
        }

        registroTiempoDTO.setId(id);
        return Optional.of(save(registroTiempoDTO));
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
}