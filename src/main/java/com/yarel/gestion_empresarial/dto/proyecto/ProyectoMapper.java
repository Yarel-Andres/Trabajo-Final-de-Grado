package com.yarel.gestion_empresarial.dto.proyecto;

import com.yarel.gestion_empresarial.entidades.Proyecto;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;


@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface ProyectoMapper {

    ProyectoDTO toDto(Proyecto entity);
    Proyecto toEntity(ProyectoDTO dto);

    List<ProyectoDTO> toDtoList(List<Proyecto> list);
    List<Proyecto> toEntityList(List<ProyectoDTO> list);

}