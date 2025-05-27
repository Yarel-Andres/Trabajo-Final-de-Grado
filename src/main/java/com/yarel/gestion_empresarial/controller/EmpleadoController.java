package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.empleado.EmpleadoConTareasDTO;
import com.yarel.gestion_empresarial.servicios.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador REST para gestionar Empleados
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    // Inyección del servicio de empleados
    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    // Obtener todos los empleados
    @GetMapping
    public ResponseEntity<List<EmpleadoDTO>> getAllEmpleados() {
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        return ResponseEntity.ok(empleados);
    }

    // Obtener un empleado por ID
    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoDTO> getEmpleadoById(@PathVariable Long id) {
        return empleadoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
