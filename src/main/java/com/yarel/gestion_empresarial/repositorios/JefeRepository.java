package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.Jefe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JefeRepository extends JpaRepository<Jefe, Long> {
    Optional<Jefe> findByNombreUsuario(String nombreUsuario);
}
