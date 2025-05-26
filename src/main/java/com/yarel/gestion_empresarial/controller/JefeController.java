package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.jefe.JefeDTO;
import com.yarel.gestion_empresarial.servicios.JefeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador REST para gestionar Jefes
@RestController
@RequestMapping("/api/jefes")
public class JefeController {

    // Inyección del servicio de jefes
    private final JefeService jefeService;

    public JefeController(JefeService jefeService) {
        this.jefeService = jefeService;
    }

    // Obtener todos los jefes
    @GetMapping
    public ResponseEntity<List<JefeDTO>> getAllJefes() {
        List<JefeDTO> jefes = jefeService.findAll();
        return ResponseEntity.ok(jefes);
    }

    // Obtener un jefe por ID
    @GetMapping("/{id}")
    public ResponseEntity<JefeDTO> getJefeById(@PathVariable Long id) {
        return jefeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear un nuevo jefe
    @PostMapping
    public ResponseEntity<JefeDTO> createJefe(@Valid @RequestBody JefeDTO jefeDTO) {
        JefeDTO createdJefe = jefeService.save(jefeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdJefe);
    }

    // Actualizar un jefe existente
    @PutMapping("/{id}")
    public ResponseEntity<JefeDTO> updateJefe(
            @PathVariable Long id,
            @Valid @RequestBody JefeDTO jefeDTO) {
        return jefeService.update(id, jefeDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar un jefe por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJefe(@PathVariable Long id) {
        boolean deleted = jefeService.deleteById(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
