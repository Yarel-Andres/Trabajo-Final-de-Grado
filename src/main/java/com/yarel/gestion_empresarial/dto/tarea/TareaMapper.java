package com.yarel.gestion_empresarial.dto.tarea;

import com.yarel.gestion_empresarial.entidades.Tarea;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.entidades.RegistroTiempo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // ✅ correcto

import java.util.List;


@Mapper(componentModel = "spring")
public interface TareaMapper {

    @Mapping(source = "empleado.id", target = "empleadoId")
    @Mapping(source = "jefe.id", target = "jefeId")
    TareaDTO toDto(Tarea tarea);

    List<TareaDTO> toDtoList(List<Tarea> tareas);

    @Mapping(source = "empleadoId", target = "empleado.id")
    @Mapping(source = "jefeId", target = "jefe.id")
    @Mapping(target = "registrosTiempo", ignore = true)
    Tarea toEntity(TareaDTO dto);
}


