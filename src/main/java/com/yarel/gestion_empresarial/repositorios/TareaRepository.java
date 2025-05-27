package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Proyecto;
import com.yarel.gestion_empresarial.entidades.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findByEmpleado(Empleado empleado);
    List<Tarea> findByJefe(Jefe jefe);
    List<Tarea> findByProyecto(Proyecto proyecto);

    // Metodo para encontrar tareas por ID de jefe
    @Query("SELECT t FROM Tarea t WHERE t.jefe.id = :jefeId")
    List<Tarea> findByJefeId(@Param("jefeId") Long jefeId);
}
