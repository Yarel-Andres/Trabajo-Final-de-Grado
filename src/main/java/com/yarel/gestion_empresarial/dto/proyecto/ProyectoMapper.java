package com.yarel.gestion_empresarial.dto.proyecto;

import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Proyecto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@Mapper(componentModel = "spring")
public interface ProyectoMapper {

    // Conversión de entidad a DTO
    @Mapping(source = "jefe.id", target = "jefeId")
    @Mapping(source = "jefe.nombreCompleto", target = "jefeNombre")
    @Mapping(source = "empleados", target = "empleadosIds", qualifiedByName = "empleadosToIds")
    ProyectoDTO toDto(Proyecto proyecto);

    // Conversión de DTO a entidad
    @Mapping(source = "jefeId", target = "jefe.id")
    @Mapping(target = "empleados", ignore = true)
    Proyecto toEntity(ProyectoDTO proyectoDto);

    // Conversiones en lote
    List<ProyectoDTO> toDtoList(List<Proyecto> proyectos);
    List<Proyecto> toEntityList(List<ProyectoDTO> proyectosDto);

    // Metodo auxiliar para convertir empleados a IDs
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