package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.proyecto.ProyectoDTO;
import com.yarel.gestion_empresarial.dto.registroTiempo.RegistroTiempoDTO;
import com.yarel.gestion_empresarial.servicios.ProyectoService;
import com.yarel.gestion_empresarial.servicios.RegistroTiempoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rrhh/proyectos")
public class RRHHProyectoController {

    @Autowired
    private ProyectoService proyectoService;

    @Autowired
    private RegistroTiempoService registroTiempoService;

    @GetMapping
    public String listarProyectos(Model model) {
        try {
            List<ProyectoDTO> proyectos = proyectoService.findAll();

            // Crear un mapa para almacenar el total de horas por proyecto
            Map<Long, Double> horasPorProyecto = new HashMap<>();

            // Crear un mapa para almacenar los registros de tiempo por proyecto
            Map<Long, List<RegistroTiempoDTO>> registrosPorProyecto = new HashMap<>();

            // Para cada proyecto, obtener sus registros de tiempo y calcular el total de horas
            for (ProyectoDTO proyecto : proyectos) {
                List<RegistroTiempoDTO> registros = registroTiempoService.findByProyectoId(proyecto.getId());
                double totalHoras = registros.stream()
                        .mapToDouble(RegistroTiempoDTO::getHorasTrabajadas)
                        .sum();

                horasPorProyecto.put(proyecto.getId(), totalHoras);
                registrosPorProyecto.put(proyecto.getId(), registros);

                // Formatear el estado para la vista de RRHH
                if ("EN_PROGRESO".equals(proyecto.getEstado())) {
                    proyecto.setEstado("En proceso");
                } else if ("FINALIZADO".equals(proyecto.getEstado())) {
                    proyecto.setEstado("Finalizado");
                }
            }

            model.addAttribute("proyectos", proyectos);
            model.addAttribute("horasPorProyecto", horasPorProyecto);
            model.addAttribute("registrosPorProyecto", registrosPorProyecto);

            return "rrhh/proyectos/listar";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar proyectos: " + e.getMessage());
            return "error/general";
        }
    }
}