package com.yarel.gestion_empresarial.dto.usuario;

import com.yarel.gestion_empresarial.entidades.Usuario;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

// MapStruct mapper para conversión automática entre Usuario y UsuarioDTO
@Mapper(
        componentModel = "spring",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface UsuarioMapper {

    // Conversión entre entidad y DTO
    UsuarioDTO toDto(Usuario entity);
    Usuario toEntity(UsuarioDTO dto);

    // Conversión de colecciones
    List<UsuarioDTO> toDtoList(List<Usuario> list);
    List<Usuario> toEntityList(List<UsuarioDTO> list);
}
