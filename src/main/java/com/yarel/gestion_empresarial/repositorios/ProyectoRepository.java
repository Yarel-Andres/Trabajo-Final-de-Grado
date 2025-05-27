package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    List<Proyecto> findByJefe(Jefe jefe);

    // Metodo para buscar proyectos por empleado
    List<Proyecto> findByEmpleadosContaining(Empleado empleado);
}
