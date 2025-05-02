package com.yarel.gestion_empresarial.dto.usuario;

import com.yarel.gestion_empresarial.entidades.Usuario;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

// Indica que esta interfaz será procesada por MapStruct para generar automáticamente la implementación de los
// métodos definidos en la interfaz
@Mapper(
        // Configura el modelo de componente como compatible con Spring. Esto permite que el UsuarioMapper se registre
        // como un bean en el contexto de Spring y pueda ser inyectado en otras clases.
        componentModel = "spring",
        //Establece cómo MapStruct debe manejar las colecciones. En este caso, utiliza el enfoque de adder, lo que
        // significa que prefiere métodos para agregar elementos a una colección (por ejemplo, addElement() en
        // lugar de sobrescribir la colección completa)
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface UsuarioMapper {

    // Estos métodos se encargan de mapear las propiedades entre los dos objetos según sus nombres y tipos, permitiendo
    // una transformación directa
    UsuarioDTO toDto(Usuario entity);
    Usuario toEntity(UsuarioDTO dto);

    // Estos métodos son útiles para transformar colecciones completas en un solo paso
    List<UsuarioDTO> toDtoList(List<Usuario> list);
    List<Usuario> toEntityList(List<UsuarioDTO> list);

}