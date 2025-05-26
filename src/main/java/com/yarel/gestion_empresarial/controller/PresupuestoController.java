package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.presupuesto.PresupuestoDTO;
import com.yarel.gestion_empresarial.dto.proyecto.ProyectoDTO;
import com.yarel.gestion_empresarial.dto.registroTiempo.RegistroTiempoDTO;
import com.yarel.gestion_empresarial.servicios.PresupuestoService;
import com.yarel.gestion_empresarial.servicios.ProyectoService;
import com.yarel.gestion_empresarial.servicios.RegistroTiempoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Controlador para gestionar Presupuestos de RRHH
@Controller
@RequestMapping("/rrhh/presupuestos")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;
    private final ProyectoService proyectoService;
    private final RegistroTiempoService registroTiempoService;

    // Inyección de servicios necesarios
    public PresupuestoController(PresupuestoService presupuestoService,
                                 ProyectoService proyectoService,
                                 RegistroTiempoService registroTiempoService) {
        this.presupuestoService = presupuestoService;
        this.proyectoService = proyectoService;
        this.registroTiempoService = registroTiempoService;
    }

    // Listar todos los presupuestos
    @GetMapping
    public String listarPresupuestos(Model model) {
        try {
            List<PresupuestoDTO> presupuestos = presupuestoService.findAll();
            model.addAttribute("presupuestos", presupuestos);
            return "rrhh/presupuestos/listar";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar presupuestos: " + e.getMessage());
            return "error/general";
        }
    }

    // Mostrar formulario de creación de presupuesto
    @GetMapping("/crear/{proyectoId}")
    public String mostrarFormularioCreacion(@PathVariable Long proyectoId, Model model) {
        try {
            Optional<ProyectoDTO> proyectoOpt = proyectoService.findById(proyectoId);

            if (proyectoOpt.isEmpty()) {
                model.addAttribute("error", "El proyecto no existe");
                return "redirect:/rrhh/proyectos";
            }

            ProyectoDTO proyecto = proyectoOpt.get();

            // Obtener las horas registradas para el proyecto
            List<RegistroTiempoDTO> registros = registroTiempoService.findByProyectoId(proyectoId);
            double totalHoras = registros.stream()
                    .mapToDouble(RegistroTiempoDTO::getHorasTrabajadas)
                    .sum();

            // Crear un nuevo DTO de presupuesto
            PresupuestoDTO presupuesto = new PresupuestoDTO();
            presupuesto.setProyectoId(proyectoId);
            presupuesto.setProyectoNombre(proyecto.getNombre());
            presupuesto.setFechaCreacion(LocalDate.now());
            presupuesto.setHorasEstimadas(totalHoras);
            presupuesto.setEstado("BORRADOR");

            model.addAttribute("presupuesto", presupuesto);
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("totalHoras", totalHoras);

            return "rrhh/presupuestos/crear";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "error/general";
        }
    }

    // Guardar un nuevo presupuesto
    @PostMapping("/guardar")
    public String guardarPresupuesto(@ModelAttribute PresupuestoDTO presupuesto,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (result.hasErrors()) {
            return "rrhh/presupuestos/crear";
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String nombreUsuario = auth.getName();

            presupuestoService.save(presupuesto, nombreUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "Presupuesto creado correctamente");
            return "redirect:/rrhh/presupuestos";
        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el presupuesto: " + e.getMessage());

            // Recuperar información del proyecto para volver a mostrar el formulario
            if (presupuesto.getProyectoId() != null) {
                try {
                    Optional<ProyectoDTO> proyectoOpt = proyectoService.findById(presupuesto.getProyectoId());
                    if (proyectoOpt.isPresent()) {
                        model.addAttribute("proyecto", proyectoOpt.get());
                    }
                } catch (Exception ex) {
                    // Error silencioso al recuperar proyecto
                }
            }

            model.addAttribute("presupuesto", presupuesto);
            return "rrhh/presupuestos/crear";
        }
    }

    // Ver detalles de un presupuesto
    @GetMapping("/{id}")
    public String verPresupuesto(@PathVariable Long id, Model model) {
        try {
            Optional<PresupuestoDTO> presupuesto = presupuestoService.findById(id);

            if (presupuesto.isEmpty()) {
                model.addAttribute("error", "El presupuesto no existe");
                return "redirect:/rrhh/presupuestos";
            }

            model.addAttribute("presupuesto", presupuesto.get());
            return "rrhh/presupuestos/ver";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el presupuesto: " + e.getMessage());
            return "error/general";
        }
    }

    // Mostrar formulario de edición de presupuesto
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        try {
            Optional<PresupuestoDTO> presupuestoOpt = presupuestoService.findById(id);

            if (presupuestoOpt.isEmpty()) {
                model.addAttribute("error", "No se pudo encontrar el presupuesto");
                return "redirect:/rrhh/presupuestos";
            }

            PresupuestoDTO presupuesto = presupuestoOpt.get();

            // Verificar que el presupuesto esté en estado BORRADOR
            if (!"BORRADOR".equals(presupuesto.getEstado())) {
                model.addAttribute("error", "Solo se pueden editar presupuestos en estado borrador");
                return "redirect:/rrhh/presupuestos/" + id;
            }

            // Obtener información del proyecto
            Optional<ProyectoDTO> proyectoOpt = proyectoService.findById(presupuesto.getProyectoId());
            if (proyectoOpt.isPresent()) {
                model.addAttribute("proyecto", proyectoOpt.get());
            }

            model.addAttribute("presupuesto", presupuesto);
            model.addAttribute("esEdicion", true);

            return "rrhh/presupuestos/editar";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "error/general";
        }
    }

    // Actualizar un presupuesto existente
    @PostMapping("/{id}/actualizar")
    public String actualizarPresupuesto(@PathVariable Long id, @ModelAttribute PresupuestoDTO presupuesto,
                                        BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "rrhh/presupuestos/editar";
        }

        try {
            // Verificar que el presupuesto existe
            Optional<PresupuestoDTO> presupuestoExistenteOpt = presupuestoService.findById(id);
            if (presupuestoExistenteOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el presupuesto");
                return "redirect:/rrhh/presupuestos";
            }

            PresupuestoDTO presupuestoExistente = presupuestoExistenteOpt.get();

            // Verificar que el presupuesto esté en estado BORRADOR
            if (!"BORRADOR".equals(presupuestoExistente.getEstado())) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden editar presupuestos en estado borrador");
                return "redirect:/rrhh/presupuestos/" + id;
            }

            // Mantener valores que no deben cambiar
            presupuesto.setId(id);
            presupuesto.setProyectoId(presupuestoExistente.getProyectoId());
            presupuesto.setProyectoNombre(presupuestoExistente.getProyectoNombre());
            presupuesto.setFechaCreacion(presupuestoExistente.getFechaCreacion());
            presupuesto.setEstado("BORRADOR");

            // Calcular el costo total
            presupuesto.setCostoTotal(presupuesto.getTarifaHora() * presupuesto.getHorasEstimadas());

            // Guardar el presupuesto actualizado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String nombreUsuario = auth.getName();

            presupuestoService.update(presupuesto, nombreUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "Presupuesto actualizado correctamente");
            return "redirect:/rrhh/presupuestos/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el presupuesto: " + e.getMessage());
            return "redirect:/rrhh/presupuestos/" + id;
        }
    }

    // Aprobar un presupuesto
    @PostMapping("/{id}/aprobar")
    public String aprobarPresupuesto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Verificar que el presupuesto existe
            Optional<PresupuestoDTO> presupuestoOpt = presupuestoService.findById(id);
            if (presupuestoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el presupuesto");
                return "redirect:/rrhh/presupuestos";
            }

            PresupuestoDTO presupuesto = presupuestoOpt.get();

            // Verificar que el presupuesto esté en estado BORRADOR
            if (!"BORRADOR".equals(presupuesto.getEstado())) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden aprobar presupuestos en estado borrador");
                return "redirect:/rrhh/presupuestos/" + id;
            }

            // Cambiar el estado a ENVIADO
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String nombreUsuario = auth.getName();

            presupuestoService.cambiarEstado(id, "ENVIADO", nombreUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "Presupuesto aprobado y enviado correctamente");
            return "redirect:/rrhh/presupuestos/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al aprobar el presupuesto: " + e.getMessage());
            return "redirect:/rrhh/presupuestos/" + id;
        }
    }
}
