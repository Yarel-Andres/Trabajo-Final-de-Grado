package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.presupuesto.PresupuestoDTO;
import com.yarel.gestion_empresarial.dto.proyecto.ProyectoDTO;
import com.yarel.gestion_empresarial.dto.registroTiempo.RegistroTiempoDTO;
import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import com.yarel.gestion_empresarial.servicios.PresupuestoService;
import com.yarel.gestion_empresarial.servicios.ProyectoService;
import com.yarel.gestion_empresarial.servicios.RegistroTiempoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
@RequestMapping("/rrhh/presupuestos")
public class PresupuestoController {

    private static final Logger logger = LoggerFactory.getLogger(PresupuestoController.class);

    @Autowired
    private PresupuestoService presupuestoService;

    @Autowired
    private ProyectoService proyectoService;

    @Autowired
    private RegistroTiempoService registroTiempoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String listarPresupuestos(Model model) {
        logger.info("Accediendo a listarPresupuestos");
        try {
            List<PresupuestoDTO> presupuestos = presupuestoService.findAll();
            model.addAttribute("presupuestos", presupuestos);
            return "rrhh/presupuestos/listar";
        } catch (Exception e) {
            logger.error("Error al listar presupuestos", e);
            model.addAttribute("error", "Error al cargar presupuestos: " + e.getMessage());
            return "error/general";
        }
    }

    @GetMapping("/crear/{proyectoId}")
    public String mostrarFormularioCreacion(@PathVariable Long proyectoId, Model model) {
        logger.info("Accediendo a mostrarFormularioCreacion con proyectoId: {}", proyectoId);
        try {
            Optional<ProyectoDTO> proyectoOpt = proyectoService.findById(proyectoId);

            if (proyectoOpt.isEmpty()) {
                logger.warn("Proyecto no encontrado: {}", proyectoId);
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
            logger.error("Error al mostrar formulario de creación", e);
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "error/general";
        }
    }

    @PostMapping("/guardar")
    public String guardarPresupuesto(@ModelAttribute PresupuestoDTO presupuesto,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        logger.info("Guardando presupuesto para proyecto: {}", presupuesto.getProyectoId());
        if (result.hasErrors()) {
            logger.warn("Errores de validación: {}", result.getAllErrors());
            return "rrhh/presupuestos/crear";
        }

        try {
            // Obtener el nombre de usuario actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String nombreUsuario = auth.getName();
            logger.info("Usuario autenticado: {} con roles: {}", nombreUsuario,
                    auth.getAuthorities().stream()
                            .map(a -> a.getAuthority())
                            .collect(java.util.stream.Collectors.joining(", ")));

            // Guardar el presupuesto
            PresupuestoDTO presupuestoGuardado = presupuestoService.save(presupuesto, nombreUsuario);
            logger.info("Presupuesto guardado con ID: {}", presupuestoGuardado.getId());

            redirectAttributes.addFlashAttribute("mensaje", "Presupuesto creado correctamente");
            return "redirect:/rrhh/presupuestos";
        } catch (Exception e) {
            logger.error("Error al guardar presupuesto", e);
            // Añadir el error al modelo para mostrarlo en la misma página
            model.addAttribute("error", "Error al guardar el presupuesto: " + e.getMessage());

            // Recuperar información del proyecto para volver a mostrar el formulario
            if (presupuesto.getProyectoId() != null) {
                try {
                    Optional<ProyectoDTO> proyectoOpt = proyectoService.findById(presupuesto.getProyectoId());
                    if (proyectoOpt.isPresent()) {
                        model.addAttribute("proyecto", proyectoOpt.get());
                    }
                } catch (Exception ex) {
                    logger.error("Error al recuperar información del proyecto", ex);
                }
            }

            model.addAttribute("presupuesto", presupuesto);
            return "rrhh/presupuestos/crear";
        }
    }

    @GetMapping("/{id}")
    public String verPresupuesto(@PathVariable Long id, Model model) {
        logger.info("Accediendo a verPresupuesto con id: {}", id);
        try {
            Optional<PresupuestoDTO> presupuesto = presupuestoService.findById(id);

            if (presupuesto.isEmpty()) {
                logger.warn("Presupuesto no encontrado: {}", id);
                model.addAttribute("error", "El presupuesto no existe");
                return "redirect:/rrhh/presupuestos";
            }

            model.addAttribute("presupuesto", presupuesto.get());
            return "rrhh/presupuestos/ver";
        } catch (Exception e) {
            logger.error("Error al ver presupuesto", e);
            model.addAttribute("error", "Error al cargar el presupuesto: " + e.getMessage());
            return "error/general";
        }
    }

    @GetMapping("/debug-auth")
    @ResponseBody
    public String debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        StringBuilder sb = new StringBuilder();
        sb.append("Usuario autenticado: ").append(auth.getName()).append("\n");
        sb.append("Roles: ").append(auth.getAuthorities()).append("\n");
        sb.append("Detalles: ").append(auth.getDetails()).append("\n");
        sb.append("Principal: ").append(auth.getPrincipal()).append("\n");

        // Intentar buscar el usuario en la base de datos
        Optional<Usuario> usuarioByNombre = usuarioRepository.findByNombreUsuario(auth.getName());
        Optional<Usuario> usuarioByCompleto = usuarioRepository.findByNombreCompleto(auth.getName());

        sb.append("Usuario por nombreUsuario: ").append(usuarioByNombre.isPresent() ? "Encontrado" : "No encontrado").append("\n");
        if (usuarioByNombre.isPresent()) {
            Usuario u = usuarioByNombre.get();
            sb.append("  ID: ").append(u.getId()).append("\n");
            sb.append("  Rol: ").append(u.getRol()).append("\n");
            sb.append("  Nombre Completo: ").append(u.getNombreCompleto()).append("\n");
        }

        sb.append("Usuario por nombreCompleto: ").append(usuarioByCompleto.isPresent() ? "Encontrado" : "No encontrado").append("\n");
        if (usuarioByCompleto.isPresent()) {
            Usuario u = usuarioByCompleto.get();
            sb.append("  ID: ").append(u.getId()).append("\n");
            sb.append("  Rol: ").append(u.getRol()).append("\n");
            sb.append("  Nombre Usuario: ").append(u.getNombreUsuario()).append("\n");
        }

        return sb.toString();
    }
}
