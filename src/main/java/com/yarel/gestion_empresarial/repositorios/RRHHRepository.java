package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.RRHH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RRHHRepository extends JpaRepository<RRHH, Long> {
}