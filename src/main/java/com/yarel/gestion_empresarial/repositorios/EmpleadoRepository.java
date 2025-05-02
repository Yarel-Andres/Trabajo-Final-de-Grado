package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    List<Empleado> findByPuesto(String puesto);
    // Busca empleados con un salario mayor al que le pasamos por parámetro
    List<Empleado> findBySalarioGreaterThan(Double salario);
    List<Empleado> findByActivoTrue();
}