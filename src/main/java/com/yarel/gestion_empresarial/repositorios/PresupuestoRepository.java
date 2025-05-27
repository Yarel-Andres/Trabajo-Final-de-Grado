package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.Presupuesto;
import com.yarel.gestion_empresarial.entidades.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {
    List<Presupuesto> findByProyecto(Proyecto proyecto);
}