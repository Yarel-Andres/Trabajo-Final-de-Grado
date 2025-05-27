package com.yarel.gestion_empresarial.dto.reunion;

import com.yarel.gestion_empresarial.entidades.Reunion;
import com.yarel.gestion_empresarial.entidades.Usuario;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface ReunionMapper {

    // Conversión de entidad a DTO
    @Mapping(source = "organizador.id", target = "organizadorId")
    @Mapping(source = "organizador.nombreCompleto", target = "organizadorNombre")
    @Mapping(source = "participantes", target = "participantesIds", qualifiedByName = "participantesToIds")
    ReunionDTO toDto(Reunion reunion);

    // Conversión de DTO a entidad
    @Mapping(source = "organizadorId", target = "organizador.id")
    @Mapping(target = "participantes", ignore = true) // Se maneja en el servicio
    Reunion toEntity(ReunionDTO reunionDto);

    // Conversiones de listas
    List<ReunionDTO> toDtoList(List<Reunion> reuniones);
    List<Reunion> toEntityList(List<ReunionDTO> reunionesDto);

    // Metodo auxiliar para convertir participantes a IDs
    @Named("participantesToIds")
    default Set<Long> participantesToIds(Set<Usuario> participantes) {
        if (participantes == null) {
            return new HashSet<>();
        }
        return participantes.stream()
                .map(Usuario::getId)
                .collect(Collectors.toSet());
    }
}