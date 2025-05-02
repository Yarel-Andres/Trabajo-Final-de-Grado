package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

// Importa la anotación @Repository, que marca la clase como un repositorio
import org.springframework.stereotype.Repository;

// Importa Optional, que se usa para manejar valores que pueden ser null
import java.util.Optional;


@Repository
// Declara una interfaz (no una clase) que extiende JpaRepository, lo que le otorga acceso a métodos CRUD
                                // Hereda funcionalidades de JpaRepository para la entidad Usuario, donde Usuario es la
                                // entidad manejada y Long es el tipo de la clave primaria (id)
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca por nombre o correo del usuario, utilizando el parámetro recibido
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    Optional<Usuario> findByCorreo(String correo);

    // Devuelven true o false si el usuario buscado existe o no
    boolean existsByNombreUsuario(String nombreUsuario);
    boolean existsByCorreo(String correo);
}