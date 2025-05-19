package com.yarel.gestion_empresarial.dto.presupuesto;

import com.yarel.gestion_empresarial.entidades.Presupuesto;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface PresupuestoMapper {

    @Mapping(source = "proyecto.id", target = "proyectoId")
    @Mapping(source = "proyecto.nombre", target = "proyectoNombre")
    @Mapping(source = "creador.id", target = "creadorId")
    @Mapping(source = "creador.nombreCompleto", target = "creadorNombre")
    PresupuestoDTO toDto(Presupuesto entity);

    @Mapping(source = "proyectoId", target = "proyecto.id")
    @Mapping(source = "creadorId", target = "creador.id")
    @Mapping(target = "proyecto.nombre", ignore = true)
    @Mapping(target = "creador.nombreCompleto", ignore = true)
    Presupuesto toEntity(PresupuestoDTO dto);

    List<PresupuestoDTO> toDtoList(List<Presupuesto> list);
    List<Presupuesto> toEntityList(List<PresupuestoDTO> list);
}
