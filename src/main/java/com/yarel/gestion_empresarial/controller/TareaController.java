package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.servicios.TareaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tareas")
@Validated // Para que @Valid funcione a nivel de clase
public class TareaController {

    private final TareaService tareaService;

    // Metodo para obtener todas las tareas
    @GetMapping
    public ResponseEntity<List<TareaDTO>> getAllTareas() {
        List<TareaDTO> tareas = tareaService.findAll();
        return ResponseEntity.ok(tareas);
    }

    // Metodo para crear una nueva tarea
    @PostMapping
    public ResponseEntity<TareaDTO> createTarea(@Valid @RequestBody TareaDTO tareaDTO) {
        TareaDTO createdTarea = tareaService.save(tareaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTarea);
    }

    // Metodo para crear una nueva tarea asignada por un jefe
    @PostMapping("/jefe")
    public ResponseEntity<TareaDTO> crearTareaAsignada(@Valid @RequestBody TareaDTO tareaDTO) {
        // 1. Obtener el nombre de usuario del jefe autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jefeNombreUsuario = userDetails.getUsername();

        // 2. Llamar al servicio para guardar la tarea, pasando el nombre del jefe
        TareaDTO nuevaTarea = tareaService.saveTareaForJefe(tareaDTO, jefeNombreUsuario);

        // 3. Devolver la respuesta
        return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);
    }
}

