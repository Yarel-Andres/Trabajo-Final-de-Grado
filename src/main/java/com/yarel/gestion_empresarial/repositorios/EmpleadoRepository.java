package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    // Hereda funcionalidades básicas de JpaRepository (save, findAll, findById, delete, etc.)
}
