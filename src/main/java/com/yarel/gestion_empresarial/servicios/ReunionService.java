package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.reunion.ReunionDTO;
import com.yarel.gestion_empresarial.dto.reunion.ReunionMapper;
import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Reunion;
import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.repositorios.EmpleadoRepository;
import com.yarel.gestion_empresarial.repositorios.JefeRepository;
import com.yarel.gestion_empresarial.repositorios.ReunionRepository;
import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReunionService {

    private final JefeRepository jefeRepository;
    private final ReunionRepository reunionRepository;
    private final ReunionMapper reunionMapper;
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;

    public ReunionService(JefeRepository jefeRepository, ReunionRepository reunionRepository,
                          ReunionMapper reunionMapper, UsuarioRepository usuarioRepository,
                          EmpleadoRepository empleadoRepository) {
        this.jefeRepository = jefeRepository;
        this.reunionRepository = reunionRepository;
        this.reunionMapper = reunionMapper;
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
    }

    // Obtiene reuniones organizadas por un jefe
    public List<ReunionDTO> findByOrganizadorId(Long organizadorId) {
        List<Reunion> reuniones = reunionRepository.findByOrganizadorIdWithParticipantes(organizadorId);
        return reuniones.stream().map(reunionMapper::toDto).collect(Collectors.toList());
    }

    // Obtiene reuniones donde participa un empleado concreto
    public List<ReunionDTO> findByParticipanteId(Long participanteId) {
        List<Reunion> todasLasReuniones = reunionRepository.findAll();
        List<Reunion> reunionesDelParticipante = todasLasReuniones.stream()
                .filter(reunion -> reunion.getParticipantes() != null &&
                        reunion.getParticipantes().stream()
                                .anyMatch(participante -> participante.getId().equals(participanteId)))
                .collect(Collectors.toList());

        return reunionesDelParticipante.stream().map(reunionMapper::toDto).collect(Collectors.toList());
    }

    // Obtiene reuniones completadas donde participa un empleado concreto
    public List<ReunionDTO> findCompletadasByParticipanteId(Long participanteId) {
        List<Reunion> todasLasReuniones = reunionRepository.findAll();
        List<Reunion> reunionesCompletadas = todasLasReuniones.stream()
                .filter(reunion -> reunion.isCompletada() &&
                        reunion.getParticipantes() != null &&
                        reunion.getParticipantes().stream()
                                .anyMatch(participante -> participante.getId().equals(participanteId)))
                .collect(Collectors.toList());

        return reunionesCompletadas.stream().map(reunionMapper::toDto).collect(Collectors.toList());
    }

    // Busca una reunión por su ID
    public Optional<ReunionDTO> findById(Long reunionId) {
        return reunionRepository.findById(reunionId).map(reunionMapper::toDto);
    }

    // Marca una reunión como completada
    @Transactional
    public ReunionDTO finalizarReunion(Long reunionId) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new RuntimeException("Reunión no encontrada con ID: " + reunionId));

        reunion.setCompletada(true);
        reunion = reunionRepository.save(reunion);

        return reunionMapper.toDto(reunion);
    }

    // Crea una nueva reunión asignada por un jefe
    @Transactional
    public ReunionDTO saveReunionForJefe(ReunionDTO reunionDTO, String nombreUsuario) {
        try {
            // Buscar el jefe organizador
            Optional<Jefe> jefeOpt = jefeRepository.findByNombreUsuario(nombreUsuario);
            if (jefeOpt.isEmpty()) {
                throw new RuntimeException("Jefe no encontrado");
            }
            Jefe jefe = jefeOpt.get();

            // Convertir DTO a entidad
            Reunion reunion = reunionMapper.toEntity(reunionDTO);
            reunion.setOrganizador(jefe);

            // Asignar participantes si los hay
            if (reunionDTO.getParticipantesIds() != null && !reunionDTO.getParticipantesIds().isEmpty()) {
                Set<Usuario> participantes = new HashSet<>();

                for (Long participanteId : reunionDTO.getParticipantesIds()) {
                    Optional<Empleado> empleadoOpt = empleadoRepository.findById(participanteId);
                    if (empleadoOpt.isPresent()) {
                        participantes.add(empleadoOpt.get());
                    } else {
                        Optional<Usuario> usuarioOpt = usuarioRepository.findById(participanteId);
                        if (usuarioOpt.isPresent()) {
                            participantes.add(usuarioOpt.get());
                        }
                    }
                }

                reunion.setParticipantes(participantes);
            }

            // Guardar y convertir a DTO
            Reunion reunionGuardada = reunionRepository.save(reunion);
            return reunionMapper.toDto(reunionGuardada);

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la reunión: " + e.getMessage(), e);
        }
    }

    // Obtiene todas las reuniones del sistema
    public List<ReunionDTO> findAll() {
        return reunionRepository.findAll().stream().map(reunionMapper::toDto).collect(Collectors.toList());
    }
}
