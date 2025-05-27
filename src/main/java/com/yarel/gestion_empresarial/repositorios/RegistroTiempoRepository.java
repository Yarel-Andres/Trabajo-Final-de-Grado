package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegistroTiempoRepository extends JpaRepository<RegistroTiempo, Long> {

    List<RegistroTiempo> findByUsuario(Usuario usuario);
    List<RegistroTiempo> findByTarea(Tarea tarea);
    List<RegistroTiempo> findByProyecto(Proyecto proyecto);

    // Metodo que faltaba - busca registros por ID de reunión
    List<RegistroTiempo> findByReunionId(Long reunionId);
}
