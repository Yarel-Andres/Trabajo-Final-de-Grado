package com.yarel.gestion_empresarial.dto.registroTiempo;

import com.yarel.gestion_empresarial.entidades.RegistroTiempo;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface RegistroTiempoMapper {

    // Conversión de entidad a DTO
    @Mapping(target = "usuarioNombre", ignore = true) // Se asigna manualmente en el servicio
    @Mapping(target = "tipoRegistro", ignore = true) // Se calcula en el servicio
    RegistroTiempoDTO toDto(RegistroTiempo registroTiempo);

    // Conversión de DTO a entidad
    @Mapping(target = "usuario", ignore = true) // Se asigna en el servicio
    @Mapping(target = "tarea", ignore = true) // Se asigna en el servicio
    @Mapping(target = "proyecto", ignore = true) // Se asigna en el servicio
    @Mapping(target = "reunion", ignore = true) // Se asigna en el servicio
    RegistroTiempo toEntity(RegistroTiempoDTO registroTiempoDto);

    // Conversión de listas
    List<RegistroTiempoDTO> toDtoList(List<RegistroTiempo> registrosTiempo);
    List<RegistroTiempo> toEntityList(List<RegistroTiempoDTO> registrosTiempoDto);
}