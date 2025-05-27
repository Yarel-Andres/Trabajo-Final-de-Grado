package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.registroTiempo.RegistroTiempoDTO;
import com.yarel.gestion_empresarial.servicios.RegistroTiempoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador REST para gestionar Registros de Tiempo
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/registros-tiempo")
@Slf4j
public class RegistroTiempoController {

    // Inyección del servicio de registros de tiempo
    private final RegistroTiempoService registroTiempoService;

    // Obtener todos los registros de tiempo
    @GetMapping
    public ResponseEntity<List<RegistroTiempoDTO>> getAllRegistrosTiempo() {
        List<RegistroTiempoDTO> registros = registroTiempoService.findAll();
        return ResponseEntity.ok(registros);
    }

    // Obtener un registro por ID
    @GetMapping("/{id}")
    public ResponseEntity<RegistroTiempoDTO> getRegistroTiempoById(@PathVariable Long id) {
        return registroTiempoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener registros por usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<RegistroTiempoDTO>> getRegistrosTiempoByUsuarioId(@PathVariable Long usuarioId) {
        List<RegistroTiempoDTO> registros = registroTiempoService.findByUsuarioId(usuarioId);
        return ResponseEntity.ok(registros);
    }

    // Obtener registros por tarea
    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<List<RegistroTiempoDTO>> getRegistrosTiempoByTareaId(@PathVariable Long tareaId) {
        List<RegistroTiempoDTO> registros = registroTiempoService.findByTareaId(tareaId);
        return ResponseEntity.ok(registros);
    }

    // Obtener registros por proyecto
    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<RegistroTiempoDTO>> getRegistrosTiempoByProyectoId(@PathVariable Long proyectoId) {
        List<RegistroTiempoDTO> registros = registroTiempoService.findByProyectoId(proyectoId);
        return ResponseEntity.ok(registros);
    }

    // Obtener registros por reunión
    @GetMapping("/reunion/{reunionId}")
    public ResponseEntity<List<RegistroTiempoDTO>> getRegistrosTiempoByReunionId(@PathVariable Long reunionId) {
        List<RegistroTiempoDTO> registros = registroTiempoService.findByReunionId(reunionId);
        return ResponseEntity.ok(registros);
    }

    // Crear un nuevo registro de tiempo
    @PostMapping
    public ResponseEntity<RegistroTiempoDTO> createRegistroTiempo(@Valid @RequestBody RegistroTiempoDTO registroTiempoDTO) {
        RegistroTiempoDTO createdRegistro = registroTiempoService.save(registroTiempoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRegistro);
    }

    // Crear un registro de tiempo para el usuario autenticado
    @PostMapping("/usuario")
    public ResponseEntity<RegistroTiempoDTO> createRegistroTiempoForUsuario(@Valid @RequestBody RegistroTiempoDTO registroTiempoDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String nombreUsuario = authentication.getName();

        RegistroTiempoDTO createdRegistro = registroTiempoService.saveForUsuario(registroTiempoDTO, nombreUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRegistro);
    }


    // Eliminar un registro por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegistroTiempo(@PathVariable Long id) {
        boolean deleted = registroTiempoService.deleteById(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
