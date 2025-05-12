package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    List<Proyecto> findByJefe(Jefe jefe);
    List<Proyecto> findByEstado(String estado);

    // Busca proyectos con la fecha de inicio posterior a la fecha dada por parámetro
    List<Proyecto> findByFechaInicioAfter(LocalDate fecha);

    // Busca proyectos con la fecha de fin estimada anterior a la fecha dada por parámetro
    List<Proyecto> findByFechaFinEstimadaBefore(LocalDate fecha);

    // Busca proyectos con presupuesto mayor al presupuerto dado por parámetro
    List<Proyecto> findByPresupuestoGreaterThan(Double presupuesto);

    // Metodo para buscar proyectos por empleado
    List<Proyecto> findByEmpleadosContaining(Empleado empleado);
}
