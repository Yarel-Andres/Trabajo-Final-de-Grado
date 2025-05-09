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

        List<Reunion> reuniones = reunionRepository.findByParticipante(participante);
        return reunionMapper.toDtoList(reuniones);
    }
}
