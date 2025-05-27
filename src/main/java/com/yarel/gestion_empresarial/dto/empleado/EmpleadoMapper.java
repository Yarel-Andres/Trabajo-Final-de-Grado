package com.yarel.gestion_empresarial.dto.empleado;

import com.yarel.gestion_empresarial.entidades.Empleado;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EmpleadoMapper {

    // Conversiones individuales
    EmpleadoDTO toDto(Empleado entity);
    Empleado toEntity(EmpleadoDTO dto);

    // Conversiones de listas
    List<EmpleadoDTO> toDtoList(List<Empleado> entities);
    List<Empleado> toEntityList(List<EmpleadoDTO> dtos);
}