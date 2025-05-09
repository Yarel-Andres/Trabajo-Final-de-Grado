// Esta interfaz utiliza MapStruct para definir cómo convertir objetos Reunion a ReunionDTO. Maneja mapeos de
// propiedades simples, propiedades anidadas y transformaciones de colecciones, además de permitir la integración con Spring.
// El uso de @Mapping(target = "participantes", ignore = true) en el mapeo de DTO a Entidad es una consideración importante
// para la gestión de relaciones complejas en JPA.

package com.yarel.gestion_empresarial.dto.reunion;

import com.yarel.gestion_empresarial.entidades.Reunion;
import com.yarel.gestion_empresarial.entidades.Usuario;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@Mapper(
        // Esto significa que MapStruct generará una clase con la anotación @Component (o similar) que Spring puede
        // detectar y gestionar, permitiéndote inyectarla en otros componentes de Spring usando @Autowired.
        componentModel = "spring",
        // Define cómo MapStruct debe manejar el mapeo de colecciones. ADDER_PREFERRED significa que si el objeto de
        // destino tiene un metodo "add" para añadir elementos a una colección, MapStruct intentará usarlo en lugar de
        // simplemente establecer la colección directamente "set"
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface ReunionMapper {

    // Indica cómo mapear propiedades anidadas
    @Mapping(source = "organizador.id", target = "organizadorId")
    // Indica cómo mapear una colección compleja a una colección de IDs.
    @Mapping(source = "participantes", target = "participantesIds", qualifiedByName = "participantesToIds")
    //  Metodo es para convertir un objeto Reunion en un ReunionDTO
    ReunionDTO toDto(Reunion entity);


    // Realiza el mapeo inverso al del DTO
    @Mapping(source = "organizadorId", target = "organizador.id")
    // Le dice a MapStruct que ignore la propiedad participantes en la entidad Reunion cuando se mapea desde el DTO.
    @Mapping(target = "participantes", ignore = true)
    //  Metodo es para convertir un ReunionDTO de nuevo en un objeto Reunion
    Reunion toEntity(ReunionDTO dto);

    // Iteran sobre la lista de entrada y aplicarán el mapeo individual a cada elemento
    List<ReunionDTO> toDtoList(List<Reunion> list);
    List<Reunion> toEntityList(List<ReunionDTO> list);

    // Asigna un nombre a este metodo para que pueda ser referenciado explícitamente por la anotación @Mapping
    @Named("participantesToIds")
    // Permite proporcionar una implementación por defecto directamente en la interfaz.
    default Set<Long> participantesToIds(Set<Usuario> participantes) {
        // Si la colección de participantes es nula, el metodo devuelve nulo
        if (participantes == null) {
            return new HashSet<>();  // Devolver un conjunto vacío en lugar de null
        }       // Crea un flujo de los objetos Usuario en la colección participantes
        return participantes.stream()
                // Para cada objeto Usuario en el flujo, aplica la función Usuario::getId (una referencia a metodo)
                // para obtener solo su ID (Long). Esto transforma el flujo de Usuario a un flujo de Long
                .map(Usuario::getId)
                // Recoge todos los IDs del flujo y los agrupa en un nuevo Set<Long>
                .collect(Collectors.toSet());
    }
}
