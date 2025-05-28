package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.proyecto.ProyectoDTO;
import com.yarel.gestion_empresarial.dto.registroTiempo.RegistroTiempoDTO;
import com.yarel.gestion_empresarial.servicios.ProyectoService;
import com.yarel.gestion_empresarial.servicios.RegistroTiempoService;
import com.yarel.gestion_empresarial.servicios.PresupuestoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

// Controlador para gestionar las funcionalidades de RRHH
@Slf4j
@Controller
@RequestMapping("/rrhh/proyectos")
@RequiredArgsConstructor
public class RRHHController {

    // Inyección del servicio de proyectos y registros de tiempo
    private final ProyectoService proyectoService;
    private final RegistroTiempoService registroTiempoService;
    private final PresupuestoService presupuestoService;

    // Listar todos los proyectos con información de horas trabajadas
    @GetMapping
    public String listarProyectos(Model model) {
        try {
            List<ProyectoDTO> proyectos = proyectoService.findAll();

            // Crear un mapa para almacenar el total de horas por proyecto
            Map<Long, Double> horasPorProyecto = new HashMap<>();

            // Crear un mapa para almacenar los registros de tiempo por proyecto
            Map<Long, List<RegistroTiempoDTO>> registrosPorProyecto = new HashMap<>();

            // Crear un mapa para verificar qué proyectos ya tienen presupuesto
            Map<Long, Boolean> proyectosConPresupuesto = new HashMap<>();

            // Para cada proyecto, verificar si ya tiene presupuestos
            for (ProyectoDTO proyecto : proyectos) {
                boolean tienePresupuesto = !presupuestoService.findByProyectoId(proyecto.getId()).isEmpty();
                proyectosConPresupuesto.put(proyecto.getId(), tienePresupuesto);
            }

            // Para cada proyecto, obtener sus registros de tiempo y calcular el total de horas
            for (ProyectoDTO proyecto : proyectos) {
                List<RegistroTiempoDTO> registros = registroTiempoService.findByProyectoId(proyecto.getId());
                double totalHoras = registros.stream()
                        .mapToDouble(RegistroTiempoDTO::getHorasTrabajadas)
                        .sum();

                horasPorProyecto.put(proyecto.getId(), totalHoras);
                registrosPorProyecto.put(proyecto.getId(), registros);
            }

            // Formatear el estado para la vista de RRHH
            for (ProyectoDTO proyecto : proyectos) {
                String estadoOriginal = proyecto.getEstado();

                if ("EN_PROGRESO".equals(estadoOriginal)) {
                    proyecto.setEstado("En proceso");
                } else if ("FINALIZADO".equals(estadoOriginal)) {
                    proyecto.setEstado("Finalizado");
                } else if ("PLANIFICACION".equals(estadoOriginal)) {
                    proyecto.setEstado("Planificación");
                } else if ("PAUSADO".equals(estadoOriginal)) {
                    proyecto.setEstado("Pausado");
                } else if ("CANCELADO".equals(estadoOriginal)) {
                    proyecto.setEstado("Cancelado");
                }

                // Debug: Mostrar el estado original y el convertido
                System.out.println("Proyecto ID: " + proyecto.getId() +
                        ", Estado original: " + estadoOriginal +
                        ", Estado convertido: " + proyecto.getEstado() +
                        ", Completado: " + proyecto.isCompletado());
            }

            model.addAttribute("proyectos", proyectos);
            model.addAttribute("horasPorProyecto", horasPorProyecto);
            model.addAttribute("registrosPorProyecto", registrosPorProyecto);
            model.addAttribute("proyectosConPresupuesto", proyectosConPresupuesto);

            return "rrhh/proyectos/listar";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar proyectos: " + e.getMessage());
            return "error/general";
        }
    }
}
