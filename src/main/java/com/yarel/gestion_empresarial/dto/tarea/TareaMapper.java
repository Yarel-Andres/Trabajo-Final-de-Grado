package com.yarel.gestion_empresarial.dto.tarea;

import com.yarel.gestion_empresarial.entidades.Tarea;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;


@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface TareaMapper {

    TareaDTO toDto(Tarea entity);
    Tarea toEntity(TareaDTO dto);

    List<TareaDTO> toDtoList(List<Tarea> list);
    List<Tarea> toEntityList(List<TareaDTO> list);

}