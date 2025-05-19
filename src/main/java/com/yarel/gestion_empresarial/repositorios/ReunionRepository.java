package com.yarel.gestion_empresarial.repositorios;

import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Reunion;
import com.yarel.gestion_empresarial.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
// Importa @Query para crear consultas
import org.springframework.data.jpa.repository.Query;
// Importa @Param para pasar parámetros a las consultas
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Long> {

    List<Reunion> findByOrganizador(Jefe organizador);
    List<Reunion> findBySala(String sala);

    // Busca reuniones programadas despues de la hora y fecha dada
    List<Reunion> findByFechaHoraAfter(LocalDateTime fechaHora);

    // Busca reuniones programadas antes de la hora y fecha dada
    List<Reunion> findByFechaHoraBefore(LocalDateTime fechaHora);

    // Busca reuniones en las que participa un participante en concreto
    // Modificado para cargar los participantes de manera EAGER
    @Query("SELECT DISTINCT r FROM Reunion r LEFT JOIN FETCH r.participantes WHERE r IN (SELECT r2 FROM Reunion r2 JOIN r2.participantes p WHERE p = :participante)")
    List<Reunion> findByParticipante(@Param("participante") Usuario participante);
}
