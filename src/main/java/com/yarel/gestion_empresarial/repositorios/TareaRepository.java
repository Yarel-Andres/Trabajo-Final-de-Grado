package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByEmpleado(Empleado empleado);
    List<Tarea> findByJefe(Jefe jefe);
    List<Tarea> findByCompletada(boolean completada);
    List<Tarea> findByPrioridad(String prioridad);

    // Busca tareas con la fecha de vencimiento anterior a la fecha dada por parámetro
    List<Tarea> findByFechaVencimientoBefore(LocalDate fecha);

    // Busca tareas con la fecha de vencimiento posterior a la fecha dada por parámetro
    List<Tarea> findByFechaVencimientoAfter(LocalDate fecha);
}