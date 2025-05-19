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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tareas")
@Validated // Para que @Valid funcione a nivel de clase
public class TareaController {

    private final TareaService tareaService;
    private final EmpleadoRepository empleadoRepository;

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

    // Metodo para finalizar una tarea (API)
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<TareaDTO> finalizarTarea(@PathVariable Long id) {
        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Verificar que el empleado solo pueda finalizar sus propias tareas
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

        // Finalizar la tarea
        TareaDTO tareaFinalizada = tareaService.finalizarTarea(id);
        return ResponseEntity.ok(tareaFinalizada);
    }

    // Metodo para que un empleado pueda ver sus tareas
    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<TareaDTO>> getTareasByEmpleadoId(@PathVariable Long empleadoId) {
        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Verificar que el empleado solo pueda ver sus propias tareas
        Optional<Empleado> empleadoOpt = empleadoRepository.findById(empleadoId);
        if (empleadoOpt.isEmpty()) {
            // Si no se encuentra el empleado, se devuelve un código 404 Not Found, indicando que el empleado no existe
            return ResponseEntity.notFound().build();
        }

        // Si el usuario autenticado no es el mismo empleado (empleado.getNombreUsuario().equals(username)), se verifica
        //si tiene un rol de jefe o rrhh que tambien tienen acceso a ver las tareas de los empleados
        Empleado empleado = empleadoOpt.get();
        if (!empleado.getNombreUsuario().equals(username) &&
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE")) &&
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RRHH"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Usar el servicio para obtener las tareas
        List<TareaDTO> tareasDTO = tareaService.findByEmpleadoId(empleadoId);

        // Retorna la lista de tareas con código 200 OK
        return ResponseEntity.ok(tareasDTO);
    }
}
