package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.reunion.ReunionDTO;
import com.yarel.gestion_empresarial.servicios.ReunionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reuniones")
@Slf4j
public class ReunionController {

    private final ReunionService reunionService;

    // Obtiene todas las reuniones
    @GetMapping
    public ResponseEntity<List<ReunionDTO>> getAllReuniones() {
        List<ReunionDTO> reuniones = reunionService.findAll();
        return ResponseEntity.ok(reuniones);
    }

    // Obtiene una reunion por su id
    @GetMapping("/{id}")
    public ResponseEntity<ReunionDTO> getReunionById(@PathVariable Long id) {
        return reunionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear reunion como jefe
    @PostMapping("/jefe")
    public ResponseEntity<?> crearReunion(@Valid @RequestBody ReunionDTO reunionDTO) {
        try {
            // Log para depuración de la fecha recibida
            log.info("Fecha recibida en el controlador: {}", reunionDTO.getFechaHora());
            log.info("Participantes recibidos: {}", reunionDTO.getParticipantesIds());

            // Validar que la fecha no sea nula
            if (reunionDTO.getFechaHora() == null) {
                log.error("Error: La fecha y hora son obligatorias");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Error: La fecha y hora son obligatorias");
            }

            // Obtener el nombre de usuario del jefe autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String jefeNombreUsuario = authentication.getName();
            log.info("Usuario autenticado: {}", jefeNombreUsuario);

            // Llamar al servicio para guardar la reunión
            ReunionDTO nuevaReunion = reunionService.saveReunionForJefe(reunionDTO, jefeNombreUsuario);
            log.info("Reunión creada con ID: {}", nuevaReunion.getId());

            // Devuelve un ResponseEntity con la lista de ReunionDTOs y un código de estado 200 OK.
            return new ResponseEntity<>(nuevaReunion, HttpStatus.CREATED);

        } catch (DateTimeParseException e) {
            // Error específico para problemas de formato de fecha
            log.error("Error al parsear la fecha: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error en el formato de fecha. Use el formato correcto (yyyy-MM-ddTHH:mm)");

        } catch (Exception e) {
            // Captura cualquier otra excepción
            log.error("Error al crear la reunión: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al programar la reunión: " + e.getMessage());
        }
    }

    // Obtener reunion con id de participante(empleado)
    @GetMapping("/participante/{participanteId}")
    public ResponseEntity<List<ReunionDTO>> getReunionesForParticipante(@PathVariable Long participanteId) {
        List<ReunionDTO> reuniones = reunionService.findByParticipanteId(participanteId);
        return ResponseEntity.ok(reuniones);
    }

    // Manejador de excepciones global para el controlador
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        log.error("Error no manejado en ReunionController: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al procesar la solicitud: " + e.getMessage());
    }
}
