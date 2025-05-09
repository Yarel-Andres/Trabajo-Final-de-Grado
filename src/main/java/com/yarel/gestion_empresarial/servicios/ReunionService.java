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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
            // Debug - imprimir valores recibidos
            System.out.println("ReunionDTO recibido: " + reunionDTO);
            System.out.println("Jefe nombre usuario: " + jefeNombreUsuario);
            if (reunionDTO.getParticipantesIds() != null) {
                System.out.println("Participantes IDs: " + reunionDTO.getParticipantesIds());
            } else {
                System.out.println("Participantes IDs es null");
            }

            // Validaciones básicas
            if (reunionDTO == null) {
                throw new IllegalArgumentException("Los datos de la reunión no pueden ser nulos");
            }

            if (reunionDTO.getTitulo() == null || reunionDTO.getTitulo().trim().isEmpty()) {
                throw new IllegalArgumentException("El título de la reunión es obligatorio");
            }

            if (reunionDTO.getFechaHora() == null) {
                throw new IllegalArgumentException("La fecha y hora de la reunión son obligatorias");
            }

            if (reunionDTO.getSala() == null || reunionDTO.getSala().trim().isEmpty()) {
                throw new IllegalArgumentException("La sala de la reunión es obligatoria");
            }

            // Buscar el jefe por nombre de usuario
            Jefe jefe = jefeRepository.findByNombreUsuario(jefeNombreUsuario)
                    .orElseThrow(() -> new RuntimeException("Jefe no encontrado con nombre de usuario: " + jefeNombreUsuario));

            // Crear una nueva reunión en lugar de usar el mapper
            Reunion reunion = new Reunion();
            reunion.setTitulo(reunionDTO.getTitulo());
            reunion.setDescripcion(reunionDTO.getDescripcion());
            reunion.setFechaHora(reunionDTO.getFechaHora());
            reunion.setSala(reunionDTO.getSala());
            reunion.setOrganizador(jefe);

            // Inicializar conjunto de participantes
            Set<Usuario> participantes = new HashSet<>();

            // Añadir al jefe como participante automáticamente
            participantes.add(jefe);
            System.out.println("Jefe añadido como participante: " + jefe.getId());

            // Añadir los demás participantes
            if (reunionDTO.getParticipantesIds() != null && !reunionDTO.getParticipantesIds().isEmpty()) {
                for (Long participanteId : reunionDTO.getParticipantesIds()) {
                    System.out.println("Buscando participante con ID: " + participanteId);
                    usuarioRepository.findById(participanteId).ifPresent(participantes::add);
                }
            }

            // Asignar participantes a la reunión
            reunion.setParticipantes(participantes);
            System.out.println("Total de participantes: " + participantes.size());

            // Guardar la reunión
            reunion = reunionRepository.save(reunion);
            System.out.println("Reunión guardada con ID: " + reunion.getId());

            // Convertir de vuelta a DTO manualmente para evitar problemas con el mapper
            ReunionDTO result = new ReunionDTO();
            result.setId(reunion.getId());
            result.setTitulo(reunion.getTitulo());
            result.setDescripcion(reunion.getDescripcion());
            result.setFechaHora(reunion.getFechaHora());
            result.setSala(reunion.getSala());
            result.setOrganizadorId(reunion.getOrganizador().getId());

            // Convertir participantes a IDs
            Set<Long> participantesIds = reunion.getParticipantes().stream()
                    .map(Usuario::getId)
                    .collect(Collectors.toSet());
            result.setParticipantesIds(participantesIds);

            return result;
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

        List<Reunion> reuniones = reunionRepository.findByParticipante(participante);
        return reunionMapper.toDtoList(reuniones);
    }
}
