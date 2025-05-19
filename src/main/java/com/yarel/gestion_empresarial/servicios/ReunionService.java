package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.reunion.ReunionDTO;
import com.yarel.gestion_empresarial.dto.reunion.ReunionMapper;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Reunion;
import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.repositorios.JefeRepository;
import com.yarel.gestion_empresarial.repositorios.ReunionRepository;
import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ReunionService {

    private final ReunionRepository reunionRepository;
    private final ReunionMapper reunionMapper;
    private final JefeRepository jefeRepository;
    private final UsuarioRepository usuarioRepository;


    // Obtiene todas las reuniones
    @Transactional(readOnly = true)
    public List<ReunionDTO> findAll() {
        List<Reunion> reuniones = reunionRepository.findAll();
        return reunionMapper.toDtoList(reuniones);
    }

    // Obtiene una reunion por Id
    @Transactional(readOnly = true)
    public Optional<ReunionDTO> findById(Long id) {
        return reunionRepository.findById(id)
                .map(reunionMapper::toDto);
    }



    // Crear reunion y asignarla a empleados, asi como a si mismo
    @Transactional
    public ReunionDTO saveReunionForJefe(ReunionDTO reunionDTO, String jefeNombreUsuario) {
        try {
            // Buscar el jefe por nombre de usuario
            Jefe jefe = jefeRepository.findByNombreUsuario(jefeNombreUsuario)
                    .orElseThrow(() -> new RuntimeException("Jefe no encontrado con nombre de usuario: " + jefeNombreUsuario));

            // Asignar el ID del jefe al DTO
            reunionDTO.setOrganizadorId(jefe.getId());

            // Usar el mapper para convertir DTO a entidad
            Reunion reunion = reunionMapper.toEntity(reunionDTO);

            // Asegurarse de que el organizador esté correctamente establecido
            reunion.setOrganizador(jefe);

            // Inicializar conjunto de participantes si es null
            if (reunion.getParticipantes() == null) {
                reunion.setParticipantes(new HashSet<>());
            }

            // Añadir al jefe como participante automáticamente si no está ya incluido
            reunion.getParticipantes().add(jefe);

            // Añadir los demás participantes
            if (reunionDTO.getParticipantesIds() != null) {
                for (Long participanteId : reunionDTO.getParticipantesIds()) {
                    usuarioRepository.findById(participanteId).ifPresent(
                            participante -> reunion.getParticipantes().add(participante)
                    );
                }
            }

            // Guardar la reunión
            Reunion reunionGuardada = reunionRepository.save(reunion);

            // Usar el mapper para convertir la entidad guardada de vuelta a DTO
            return reunionMapper.toDto(reunionGuardada);
        } catch (Exception e) {
            // Registra el error detallado
            System.err.println("Error al guardar la reunión: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar para que el controlador la maneje
        }
    }


    // Obtiene reunion por ID de participante
    @Transactional(readOnly = true)
    public List<ReunionDTO> findByParticipanteId(Long participanteId) {
        Usuario participante = usuarioRepository.findById(participanteId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + participanteId));

        // Usar la consulta modificada para cargar TODOS los participantes
        List<Reunion> reuniones = reunionRepository.findByParticipante(participante);

        // Log para depuración
        System.out.println("Reuniones encontradas: " + reuniones.size());

        // Convertir a DTOs
        List<ReunionDTO> reunionesDTO = new ArrayList<>();

        for (Reunion reunion : reuniones) {
            // Usar el mapper para la conversión básica
            ReunionDTO dto = reunionMapper.toDto(reunion);

            // Asegurarse de que los conjuntos estén inicializados
            if (dto.getParticipantesIds() == null) {
                dto.setParticipantesIds(new HashSet<>());
            }
            if (dto.getParticipantesNombres() == null) {
                dto.setParticipantesNombres(new HashSet<>());
            }

            // Cargar manualmente los participantes para evitar problemas de lazy loading
            Set<Usuario> participantes = reunion.getParticipantes();
            System.out.println("Reunión ID: " + reunion.getId() + ", Participantes cargados: " + participantes.size());

            // Limpiar y añadir los IDs y nombres de participantes
            dto.getParticipantesIds().clear();
            dto.getParticipantesNombres().clear();

            for (Usuario user : participantes) {
                dto.getParticipantesIds().add(user.getId());
                dto.getParticipantesNombres().add(user.getNombreCompleto());
                System.out.println("  - Añadido participante: " + user.getNombreCompleto());
            }

            // Añadir el DTO a la lista
            reunionesDTO.add(dto);
        }

        return reunionesDTO;
    }

    // Obtiene reuniones por ID del organizador (jefe)
    @Transactional(readOnly = true)
    public List<ReunionDTO> findByOrganizadorId(Long organizadorId) {
        Jefe organizador = jefeRepository.findById(organizadorId)
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado con ID: " + organizadorId));

        List<Reunion> reuniones = reunionRepository.findByOrganizador(organizador);

        // Convertir a DTOs y enriquecer con nombres de participantes
        List<ReunionDTO> reunionesDTO = reunionMapper.toDtoList(reuniones);

        // Para cada reunión, añadir los nombres de los participantes
        for (int i = 0; i < reuniones.size(); i++) {
            Reunion reunion = reuniones.get(i);
            ReunionDTO reunionDTO = reunionesDTO.get(i);

            // Inicializar el conjunto de participantes si es null
            if (reunionDTO.getParticipantesIds() == null) {
                reunionDTO.setParticipantesIds(new HashSet<>());
            }

            // Cargar explícitamente los participantes para evitar LazyInitializationException
            Set<Usuario> participantes = new HashSet<>(reunion.getParticipantes());

            // Añadir IDs de participantes al DTO
            Set<Long> participantesIds = participantes.stream()
                    .map(Usuario::getId)
                    .collect(Collectors.toSet());
            reunionDTO.setParticipantesIds(participantesIds);
        }

        return reunionesDTO;
    }

    @Transactional
    public void finalizarReunion(Long reunionId) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new RuntimeException("Reunión no encontrada con ID: " + reunionId));

        reunion.setCompletada(true);
        reunion.setFechaCompletada(LocalDateTime.now());

        reunionRepository.save(reunion);
    }

    // Añadir método para obtener reuniones completadas
    @Transactional(readOnly = true)
    public List<ReunionDTO> findCompletadasByParticipanteId(Long participanteId) {
        Usuario participante = usuarioRepository.findById(participanteId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + participanteId));

        List<Reunion> reuniones = reunionRepository.findByParticipante(participante);

        // Filtrar solo las reuniones completadas
        List<Reunion> reunionesCompletadas = reuniones.stream()
                .filter(Reunion::isCompletada)
                .collect(Collectors.toList());

        return reunionMapper.toDtoList(reunionesCompletadas);
    }
}
