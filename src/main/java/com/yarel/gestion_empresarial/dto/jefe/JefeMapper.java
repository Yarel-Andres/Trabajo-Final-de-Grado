package com.yarel.gestion_empresarial.dto.jefe;

import com.yarel.gestion_empresarial.entidades.Jefe;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface JefeMapper {

    // Conversiones individuales
    JefeDTO toDto(Jefe jefe);
    Jefe toEntity(JefeDTO jefeDto);

    // Conversiones de listas
    List<JefeDTO> toDtoList(List<Jefe> jefes);
    List<Jefe> toEntityList(List<JefeDTO> jefeDtos);
}