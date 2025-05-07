package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.empleado.EmpleadoConTareasDTO;
import com.yarel.gestion_empresarial.servicios.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Define esta clase como un controlador REST, lo que significa que manejará solicitudes HTTP y
// devolverá respuestas directamente
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    // Inyecta el servicio EmpleadoService, que contiene la lógica de negocio relacionada con los empl
    @Autowired
    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    // Devuelve una lista de todos los empleados
    @GetMapping
    public ResponseEntity<List<EmpleadoDTO>> getAllEmpleados() {
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        // Envuelve la respuesta en un objeto HTTP con estado 200 OK
        return ResponseEntity.ok(empleados);
    }

    // Busca un empleado por su ID
    @GetMapping("/{id}")                            // Indica que el parámetro id debe ser obtenido desde la URL
    public ResponseEntity<EmpleadoDTO> getEmpleadoById(@PathVariable Long id) {
        return empleadoService.findById(id)
                // Si encuentra un empleado (como un objeto Optional), el metodo map toma ese empleado y lo envuelve
                // en un objeto ResponseEntity con el estado HTTP 200 OK
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener empleado con sus tareas
    @GetMapping("/{id}/tareas")
    public ResponseEntity<EmpleadoConTareasDTO> getEmpleadoConTareas(@PathVariable Long id) {
        return empleadoService.findByIdWithTareas(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // Crea un nuevo empleado
    @PostMapping                                  // Valida que el DTO cumple con las restricciones definidas
                                                    // Permite recibir el JSON del usuario y mapearlo al objeto EmpleadoDTO
    public ResponseEntity<EmpleadoDTO> createEmpleado(@Valid @RequestBody EmpleadoDTO empleadoDTO) {
        EmpleadoDTO createdEmpleado = empleadoService.save(empleadoDTO);
        // Construye una respuesta HTTP con el estado 201 CREATED. Este estado indica que el recurso (empleado) se
        // creó correctamente en el servidor.           // Incluye el empleado creado en la respuesta
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmpleado);
    }

    // Actualiza un empleado existente
    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoDTO> updateEmpleado(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoDTO empleadoDTO) {
        // Utiliza el metodo update de EmpleadoService, pasando el id y los nuevos datos del empleado
        return empleadoService.update(id, empleadoDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
                        // No habrá un cuerpo en la respuesta HTTP, solo el estado de la respuesta
    public ResponseEntity<Void> deleteEmpleado(@PathVariable Long id) {
        // Devuelve true si el empleado fue eliminado o false si no se encontro el empleado con ese id
        boolean deleted = empleadoService.deleteById(id);
        // Si se eliminó con éxito, responde con 204 No Content
        // Si no se encuentra el empleado, responde con 404 Not Found
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}