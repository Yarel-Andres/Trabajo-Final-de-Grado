package com.yarel.gestion_empresarial.dto.rrhh;

import com.yarel.gestion_empresarial.entidades.RRHH;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;


@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface RRHHMapper {

    RRHHDTO toDto(RRHH entity);
    RRHH toEntity(RRHHDTO dto);

    List<RRHHDTO> toDtoList(List<RRHH> list);
    List<RRHH> toEntityList(List<RRHHDTO> list);

}