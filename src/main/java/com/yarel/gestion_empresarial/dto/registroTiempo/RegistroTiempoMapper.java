package com.yarel.gestion_empresarial.dto.registroTiempo;

import com.yarel.gestion_empresarial.entidades.RegistroTiempo;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;


@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface RegistroTiempoMapper {

    RegistroTiempoDTO toDto(RegistroTiempo entity);
    RegistroTiempo toEntity(RegistroTiempoDTO dto);

    List<RegistroTiempoDTO> toDtoList(List<RegistroTiempo> list);
    List<RegistroTiempo> toEntityList(List<RegistroTiempoDTO> list);

}