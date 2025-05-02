package com.yarel.gestion_empresarial.dto.empleado;

import com.yarel.gestion_empresarial.entidades.Empleado;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;


@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface EmpleadoMapper {

    EmpleadoDTO toDto(Empleado entity);
    Empleado toEntity(EmpleadoDTO dto);

    List<EmpleadoDTO> toDtoList(List<Empleado> list);
    List<Empleado> toEntityList(List<EmpleadoDTO> list);

}