package com.yarel.gestion_empresarial.dto.tarea;

import com.yarel.gestion_empresarial.entidades.Tarea;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.entidades.RegistroTiempo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface TareaMapper {

    // Conversiones de entidad a DTO
    @Mapping(source = "empleado.id", target = "empleadoId")
    @Mapping(source = "jefe.id", target = "jefeId")
    @Mapping(source = "proyecto.id", target = "proyectoId")
    @Mapping(source = "proyecto.nombre", target = "nombreProyecto")
    TareaDTO toDto(Tarea tarea);

    List<TareaDTO> toDtoList(List<Tarea> tareas);

    // Conversiones de DTO a entidad
    @Mapping(source = "empleadoId", target = "empleado.id")
    @Mapping(source = "jefeId", target = "jefe.id")
    @Mapping(source = "proyectoId", target = "proyecto.id")
    @Mapping(target = "registrosTiempo", ignore = true)
    Tarea toEntity(TareaDTO dto);
}
