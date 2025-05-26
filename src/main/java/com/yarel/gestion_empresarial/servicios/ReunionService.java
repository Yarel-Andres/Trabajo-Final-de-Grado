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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReunionService {

    @Autowired
    private JefeRepository jefeRepository;

    @Autowired
    private ReunionRepository reunionRepository;

    @Autowired
    private ReunionMapper reunionMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    /**
     * Encuentra todas las reuniones organizadas por un usuario específico
     * @param organizadorId ID del organizador
     * @return Lista de reuniones organizadas por el usuario
     */
    public List<ReunionDTO> findByOrganizadorId(Long organizadorId) {
        System.out.println("=== RECUPERANDO REUNIONES ===");
        System.out.println("Buscando reuniones para organizador ID: " + organizadorId);

        List<Reunion> reunionesDelOrganizador = reunionRepository.findByOrganizadorIdWithParticipantes(organizadorId);

        System.out.println("Reuniones encontradas: " + reunionesDelOrganizador.size());

        for (Reunion reunion : reunionesDelOrganizador) {
            System.out.println("- Reunión ID: " + reunion.getId() + ", Título: " + reunion.getTitulo());
            System.out.println("  Participantes en entidad: " + (reunion.getParticipantes() != null ? reunion.getParticipantes().size() : "null"));
            if (reunion.getParticipantes() != null) {
                for (Usuario p : reunion.getParticipantes()) {
                    System.out.println("    - " + p.getNombreCompleto() + " (ID: " + p.getId() + ")");
                }
            }
        }

        List<ReunionDTO> resultado = reunionesDelOrganizador.stream()
                .map(reunion -> {
                    ReunionDTO dto = reunionMapper.toDto(reunion);
                    System.out.println("Después de mapper.toDto() para reunión " + reunion.getId() + ":");
                    System.out.println("  ParticipantesIds: " + dto.getParticipantesIds());
                    return dto;
                })
                .collect(Collectors.toList());

        System.out.println("=== FIN RECUPERACIÓN ===");
        return resultado;
    }

    /**
     * Encuentra todas las reuniones por participante
     * @param participanteId ID del participante
     * @return Lista de reuniones del participante
     */
    public List<ReunionDTO> findByParticipanteId(Long participanteId) {
        // Buscar todas las reuniones y filtrar por participante
        List<Reunion> todasLasReuniones = reunionRepository.findAll();
        List<Reunion> reunionesDelParticipante = todasLasReuniones.stream()
                .filter(reunion -> reunion.getParticipantes() != null &&
                        reunion.getParticipantes().stream()
                                .anyMatch(participante -> participante.getId().equals(participanteId)))
                .collect(Collectors.toList());

        return reunionesDelParticipante.stream()
                .map(reunionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Encuentra todas las reuniones completadas por participante
     * @param participanteId ID del participante
     * @return Lista de reuniones completadas del participante
     */
    public List<ReunionDTO> findCompletadasByParticipanteId(Long participanteId) {
        // Buscar todas las reuniones completadas y filtrar por participante
        List<Reunion> todasLasReuniones = reunionRepository.findAll();
        List<Reunion> reunionesCompletadas = todasLasReuniones.stream()
                .filter(reunion -> reunion.isCompletada() &&
                        reunion.getParticipantes() != null &&
                        reunion.getParticipantes().stream()
                                .anyMatch(participante -> participante.getId().equals(participanteId)))
                .collect(Collectors.toList());

        return reunionesCompletadas.stream()
                .map(reunionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca una reunión por su ID
     * @param reunionId ID de la reunión
     * @return Optional con la reunión si existe
     */
    public Optional<ReunionDTO> findById(Long reunionId) {
        Optional<Reunion> reunionOpt = reunionRepository.findById(reunionId);
        if (reunionOpt.isPresent()) {
            return Optional.of(reunionMapper.toDto(reunionOpt.get()));
        }
        return Optional.empty();
    }

    /**
     * Finaliza una reunión marcándola como completada
     * @param reunionId ID de la reunión a finalizar
     * @return La reunión actualizada
     */
    @Transactional
    public ReunionDTO finalizarReunion(Long reunionId) {
        // Buscar la reunión por su ID
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new RuntimeException("Reunión no encontrada con ID: " + reunionId));

        // Marcar como completada
        reunion.setCompletada(true);

        // Guardar los cambios
        reunion = reunionRepository.save(reunion);

        // Convertir a DTO y devolver
        return reunionMapper.toDto(reunion);
    }

    @Transactional
    public ReunionDTO saveReunionForJefe(ReunionDTO reunionDTO, String nombreUsuario) {
        try {
            // LOG 1: Verificar datos de entrada
            System.out.println("=== GUARDANDO REUNIÓN ===");
            System.out.println("ReunionDTO recibido:");
            System.out.println("- Título: " + reunionDTO.getTitulo());
            System.out.println("- ParticipantesIds recibidos: " + reunionDTO.getParticipantesIds());
            System.out.println("- Cantidad de participantes: " + (reunionDTO.getParticipantesIds() != null ? reunionDTO.getParticipantesIds().size() : "null"));

            // Buscar el jefe
            Optional<Jefe> jefeOpt = jefeRepository.findByNombreUsuario(nombreUsuario);
            if (jefeOpt.isEmpty()) {
                throw new RuntimeException("Jefe no encontrado");
            }
            Jefe jefe = jefeOpt.get();

            // Convertir DTO a entidad
            Reunion reunion = reunionMapper.toEntity(reunionDTO);
            reunion.setOrganizador(jefe);

            // LOG 2: Verificar conversión
            System.out.println("Después de mapper.toEntity():");
            System.out.println("- Participantes en entidad: " + (reunion.getParticipantes() != null ? reunion.getParticipantes().size() : "null"));

            // Buscar y asignar participantes
            if (reunionDTO.getParticipantesIds() != null && !reunionDTO.getParticipantesIds().isEmpty()) {
                Set<Usuario> participantes = new HashSet<>();
                System.out.println("Buscando participantes:");

                for (Long participanteId : reunionDTO.getParticipantesIds()) {
                    System.out.println("- Buscando participante ID: " + participanteId);

                    Optional<Empleado> empleadoOpt = empleadoRepository.findById(participanteId);
                    if (empleadoOpt.isPresent()) {
                        participantes.add(empleadoOpt.get());
                        System.out.println("  ✓ Encontrado como empleado: " + empleadoOpt.get().getNombreCompleto());
                    } else {
                        Optional<Usuario> usuarioOpt = usuarioRepository.findById(participanteId);
                        if (usuarioOpt.isPresent()) {
                            participantes.add(usuarioOpt.get());
                            System.out.println("  ✓ Encontrado como usuario: " + usuarioOpt.get().getNombreCompleto());
                        } else {
                            System.out.println("  ✗ NO encontrado con ID: " + participanteId);
                        }
                    }
                }

                reunion.setParticipantes(participantes);
                System.out.println("Participantes asignados a la entidad: " + participantes.size());
            }

            // LOG 3: Verificar antes de guardar
            System.out.println("Antes de guardar:");
            System.out.println("- Participantes en entidad: " + reunion.getParticipantes().size());
            for (Usuario p : reunion.getParticipantes()) {
                System.out.println("  - " + p.getNombreCompleto() + " (ID: " + p.getId() + ")");
            }

            // Guardar
            Reunion reunionGuardada = reunionRepository.save(reunion);

            // LOG 4: Verificar después de guardar
            System.out.println("Después de guardar:");
            System.out.println("- ID de reunión guardada: " + reunionGuardada.getId());
            System.out.println("- Participantes guardados: " + reunionGuardada.getParticipantes().size());

            // Convertir de vuelta a DTO
            ReunionDTO resultado = reunionMapper.toDto(reunionGuardada);

            // LOG 5: Verificar DTO resultado
            System.out.println("DTO resultado:");
            System.out.println("- ParticipantesIds en DTO: " + resultado.getParticipantesIds());
            System.out.println("- Cantidad: " + (resultado.getParticipantesIds() != null ? resultado.getParticipantesIds().size() : "null"));
            System.out.println("=== FIN GUARDADO ===");

            return resultado;

        } catch (Exception e) {
            System.out.println("ERROR al guardar reunión: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al guardar la reunión: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todas las reuniones
     * @return Lista de todas las reuniones
     */
    public List<ReunionDTO> findAll() {
        List<Reunion> reuniones = reunionRepository.findAll();
        return reuniones.stream()
                .map(reunionMapper::toDto)
                .collect(Collectors.toList());
    }


}
