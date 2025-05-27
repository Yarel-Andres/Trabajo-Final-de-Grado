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

    // Inyección del servicio de reuniones
    private final ReunionService reunionService;


    // Obtener una reunión por su ID
    @GetMapping("/{id}")
    public ResponseEntity<ReunionDTO> getReunionById(@PathVariable Long id) {
        return reunionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear reunión como jefe
    @PostMapping("/jefe")
    public ResponseEntity<?> crearReunion(@Valid @RequestBody ReunionDTO reunionDTO) {
        try {
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

            // Llamar al servicio para guardar la reunión
            ReunionDTO nuevaReunion = reunionService.saveReunionForJefe(reunionDTO, jefeNombreUsuario);

            // Devuelve un ResponseEntity con la reunión creada y código 201 CREATED
            return new ResponseEntity<>(nuevaReunion, HttpStatus.CREATED);

        } catch (DateTimeParseException e) {
            // Error específico para problemas de formato de fecha
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error en el formato de fecha. Use el formato correcto (yyyy-MM-ddTHH:mm)");

        } catch (Exception e) {
            // Captura cualquier otra excepción
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al programar la reunión: " + e.getMessage());
        }
    }

    // Obtener reuniones por ID de participante (empleado)
    @GetMapping("/participante/{participanteId}")
    public ResponseEntity<List<ReunionDTO>> getReunionesForParticipante(@PathVariable Long participanteId) {
        try {
            List<ReunionDTO> reuniones = reunionService.findByParticipanteId(participanteId);
            return ResponseEntity.ok(reuniones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Manejador de excepciones global para el controlador
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al procesar la solicitud: " + e.getMessage());
    }
}
