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
    List<RegistroTiempo> findByReunion(Reunion reunion);
    List<RegistroTiempo> findByFechaRegistro(LocalDate fechaRegistro);

    // Busca registros de tiempo con horas trabajadas mayores que el valor dado
    List<RegistroTiempo> findByHorasTrabajadasGreaterThan(Double horasTrabajadas);

    // Busca registros de tiempo entre dos fechas
    List<RegistroTiempo> findByFechaRegistroBetween(LocalDate fechaInicio, LocalDate fechaFin);
}