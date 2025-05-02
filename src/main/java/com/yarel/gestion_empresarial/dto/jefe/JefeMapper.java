package com.yarel.gestion_empresarial.dto.jefe;

import com.yarel.gestion_empresarial.entidades.Jefe;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;


@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface JefeMapper {

    JefeDTO toDto(Jefe entity);
    Jefe toEntity(JefeDTO dto);

    List<JefeDTO> toDtoList(List<Jefe> list);
    List<Jefe> toEntityList(List<JefeDTO> list);

}