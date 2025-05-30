package com.yarel.gestion_empresarial.repositorios;


import com.yarel.gestion_empresarial.entidades.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;
// Importa @Query para crear consultas
import org.springframework.data.jpa.repository.Query;
// Importa @Param para pasar parámetros a las consultas
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Long> {

    // Obtiene todas las reuniones de un jefe específico, cargando
    // en una sola consulta tanto los participantes como el organizador
    // Evita el problema N+1 de JPA y optimiza el rendimiento
    @Query("SELECT DISTINCT r FROM Reunion r LEFT JOIN FETCH r.participantes LEFT JOIN FETCH r.organizador WHERE r.organizador.id = :organizadorId")
    List<Reunion> findByOrganizadorIdWithParticipantes(@Param("organizadorId") Long organizadorId);
}
