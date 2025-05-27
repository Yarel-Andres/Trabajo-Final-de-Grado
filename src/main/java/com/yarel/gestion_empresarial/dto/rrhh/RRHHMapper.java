package com.yarel.gestion_empresarial.dto.rrhh;

import com.yarel.gestion_empresarial.entidades.RRHH;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface RRHHMapper {

    // Conversiones individuales
    RRHHDTO toDto(RRHH rrhh);
    RRHH toEntity(RRHHDTO rrhhDto);

    // Conversiones de listas
    List<RRHHDTO> toDtoList(List<RRHH> rrhhList);
    List<RRHH> toEntityList(List<RRHHDTO> rrhhDtoList);
}