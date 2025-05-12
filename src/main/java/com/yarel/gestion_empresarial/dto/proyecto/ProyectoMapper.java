package com.yarel.gestion_empresarial.dto.proyecto;

import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Proyecto;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface ProyectoMapper {

    @Mapping(source = "jefe.id", target = "jefeId")
    @Mapping(source = "jefe.nombreCompleto", target = "jefeNombre")
    @Mapping(source = "empleados", target = "empleadosIds", qualifiedByName = "empleadosToIds")
    ProyectoDTO toDto(Proyecto entity);

    @Mapping(source = "jefeId", target = "jefe.id")
    @Mapping(target = "empleados", ignore = true)
    Proyecto toEntity(ProyectoDTO dto);

    List<ProyectoDTO> toDtoList(List<Proyecto> list);
    List<Proyecto> toEntityList(List<ProyectoDTO> list);

    @Named("empleadosToIds")
    default Set<Long> empleadosToIds(Set<Empleado> empleados) {
        if (empleados == null) {
            return new HashSet<>();
        }
        return empleados.stream()
                .map(Empleado::getId)
                .collect(Collectors.toSet());
    }
}
