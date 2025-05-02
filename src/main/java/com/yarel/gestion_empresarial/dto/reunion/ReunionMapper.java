package com.yarel.gestion_empresarial.dto.reunion;

import com.yarel.gestion_empresarial.entidades.Reunion;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;


@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface ReunionMapper {

    ReunionDTO toDto(Reunion entity);
    Reunion toEntity(ReunionDTO dto);

    List<ReunionDTO> toDtoList(List<Reunion> list);
    List<Reunion> toEntityList(List<ReunionDTO> list);

}