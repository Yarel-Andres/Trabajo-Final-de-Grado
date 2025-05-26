package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.repositorios.EmpleadoRepository;
import com.yarel.gestion_empresarial.servicios.TareaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

// Controlador REST para gestionar Tareas
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tareas")
@Validated
public class TareaController {

    // Inyección del servicio de tareas y repositorio de empleados
    private final TareaService tareaService;
    private final EmpleadoRepository empleadoRepository;

    // Obtener todas las tareas
    @GetMapping
    public ResponseEntity<List<TareaDTO>> getAllTareas() {
        List<TareaDTO> tareas = tareaService.findAll();
        return ResponseEntity.ok(tareas);
    }

    // Crear una nueva tarea
    @PostMapping
    public ResponseEntity<TareaDTO> createTarea(@Valid @RequestBody TareaDTO tareaDTO) {
        TareaDTO createdTarea = tareaService.save(tareaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTarea);
    }

    // Crear una nueva tarea asignada por un jefe
    @PostMapping("/jefe")
    public ResponseEntity<TareaDTO> crearTareaAsignada(@Valid @RequestBody TareaDTO tareaDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jefeNombreUsuario = userDetails.getUsername();

        TareaDTO nuevaTarea = tareaService.saveTareaForJefe(tareaDTO, jefeNombreUsuario);
        return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);
    }

    // Finalizar una tarea
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<TareaDTO> finalizarTarea(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<TareaDTO> tareaOpt = tareaService.findById(id);
        if (tareaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TareaDTO tarea = tareaOpt.get();
        Optional<Empleado> empleadoOpt = empleadoRepository.findById(tarea.getEmpleadoId());
        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Empleado empleado = empleadoOpt.get();
        if (!empleado.getNombreUsuario().equals(username) &&
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE")) &&
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RRHH"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TareaDTO tareaFinalizada = tareaService.finalizarTarea(id);
        return ResponseEntity.ok(tareaFinalizada);
    }

    // Obtener tareas por empleado
    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<TareaDTO>> getTareasByEmpleadoId(@PathVariable Long empleadoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<Empleado> empleadoOpt = empleadoRepository.findById(empleadoId);
        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Empleado empleado = empleadoOpt.get();
        if (!empleado.getNombreUsuario().equals(username) &&
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE")) &&
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RRHH"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TareaDTO> tareasDTO = tareaService.findByEmpleadoId(empleadoId);
        return ResponseEntity.ok(tareasDTO);
    }
}
