package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.servicios.EmpleadoService;
import com.yarel.gestion_empresarial.servicios.TareaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tareas")
public class TareaController {

    private final TareaService tareaService;
    // Inyecta el servicio EmpleadoService, que contiene la lógica de negocio relacionada con los empl

    // Devuelve una lista de todos los empleados
    @GetMapping
    public ResponseEntity<List<TareaDTO>> getAllEmpleados() {
        List<TareaDTO> tareas = tareaService.findAll();
        // Envuelve la respuesta en un objeto HTTP con estado 200 OK
        return ResponseEntity.ok(tareas);
    }
    // Crea un nuevo empleado
    @PostMapping                                  // Valida que el DTO cumple con las restricciones definidas
    // Permite recibir el JSON del usuario y mapearlo al objeto EmpleadoDTO
    public ResponseEntity<TareaDTO> createEmpleado(@Valid @RequestBody TareaDTO tareaDTO) {
        TareaDTO createdTarea = tareaService.save(tareaDTO);
        // Construye una respuesta HTTP con el estado 201 CREATED. Este estado indica que el recurso (empleado) se
        // creó correctamente en el servidor.           // Incluye el empleado creado en la respuesta
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTarea);
    }
}
