// Controlador para gestión completa de tareas: asignación, visualización, finalización y eliminación

package com.yarel.gestion_empresarial.controller.vistas;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.proyecto.ProyectoDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.dto.usuario.UsuarioDTO;
import com.yarel.gestion_empresarial.servicios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class TareaViewController {

    // Inyección de servicios para manejo de datos de tareas, empleados, proyectos y usuarios
    private final EmpleadoService empleadoService;
    private final TareaService tareaService;
    private final UsuarioService usuarioService;
    private final ProyectoService proyectoService;

    @Autowired
    public TareaViewController(EmpleadoService empleadoService, TareaService tareaService,
                               UsuarioService usuarioService, ProyectoService proyectoService) {
        this.empleadoService = empleadoService;
        this.tareaService = tareaService;
        this.usuarioService = usuarioService;
        this.proyectoService = proyectoService;
    }

    // Formulario para asignar tareas: Solo accesible para jefes, carga empleados y proyectos disponibles
    @GetMapping("/tareas/asignar")
    public String asignarTareaForm(Model model, Principal principal) {
        // Verificación de rol: Solo jefes pueden asignar tareas
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        // Carga de datos para formulario: Lista de empleados y proyectos disponibles
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        model.addAttribute("empleados", empleados);

        List<ProyectoDTO> proyectos = proyectoService.findAll();
        model.addAttribute("proyectos", proyectos);

        model.addAttribute("tarea", new TareaDTO());

        return "tareas/asignar";
    }

    // Procesamiento de asignación de tareas: Valida datos, convierte fechas y asigna tarea al empleado
    @PostMapping("/tareas/asignar")
    public String asignarTarea(
            @ModelAttribute TareaDTO tarea,
            @RequestParam("fechaVencimientoDate") String fechaVencimientoDate,
            @RequestParam("horaVencimiento") int horaVencimiento,
            @RequestParam("minutosVencimiento") int minutosVencimiento,
            RedirectAttributes redirectAttributes) {
        // Verificación de rol: Solo jefes pueden asignar tareas
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        try {
            // Búsqueda del jefe: Obtiene datos del usuario autenticado para asignar como creador de la tarea
            String nombreCompleto = auth.getName();
            Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);

            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario");
                return "redirect:/tareas/asignar";
            }

            String nombreUsuario = usuarioOpt.get().getNombreUsuario();

            // Conversión de fecha: Combina fecha, hora y minutos en LocalDateTime para vencimiento
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fecha = LocalDate.parse(fechaVencimientoDate, formatter);
            LocalDateTime fechaHora = fecha.atTime(horaVencimiento, minutosVencimiento);
            tarea.setFechaVencimiento(fechaHora);

            // Asignación de tarea: Guarda en base de datos y confirma operación
            TareaDTO nuevaTarea = tareaService.saveTareaForJefe(tarea, nombreUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "Tarea asignada correctamente");
            return "redirect:/tareas/confirmacion";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al asignar la tarea: " + e.getMessage());
            return "redirect:/tareas/asignar";
        }
    }

    // Vista de tareas del empleado: Muestra tareas asignadas no completadas con información de jefe y proyecto
    @GetMapping("/tareas/ver")
    public String verTareas(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        // Búsqueda del empleado: Obtiene ID del empleado por nombre completo
        Long empleadoId = null;
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        for (EmpleadoDTO empleado : empleados) {
            if (empleado.getNombreCompleto().equals(nombreCompleto)) {
                empleadoId = empleado.getId();
                break;
            }
        }

        if (empleadoId == null) {
            model.addAttribute("error", "No se pudo encontrar el empleado");
            return "tareas/ver";
        }

        // Filtrado de tareas: Solo muestra tareas no completadas del empleado
        List<TareaDTO> tareas = tareaService.findByEmpleadoId(empleadoId);
        tareas = tareas.stream()
                .filter(tarea -> !tarea.isCompletada())
                .toList();

        // Enriquecimiento de datos: Añade nombres de jefes y proyectos a las tareas
        List<UsuarioDTO> usuarios = usuarioService.findAll();
        List<ProyectoDTO> proyectos = proyectoService.findAll();

        for (TareaDTO tarea : tareas) {
            // Asignación de nombre del jefe
            if (tarea.getJefeId() != null) {
                for (UsuarioDTO usuario : usuarios) {
                    if (usuario.getId().equals(tarea.getJefeId())) {
                        tarea.setJefeNombre(usuario.getNombreCompleto());
                        break;
                    }
                }
            }

            // Asignación de nombre del proyecto
            if (tarea.getProyectoId() != null) {
                for (ProyectoDTO proyecto : proyectos) {
                    if (proyecto.getId().equals(tarea.getProyectoId())) {
                        tarea.setNombreProyecto(proyecto.getNombre());
                        break;
                    }
                }
            }
        }

        model.addAttribute("tareas", tareas);

        return "tareas/ver";
    }

    // Vista de tareas asignadas por jefe: Muestra tareas creadas por el jefe agrupadas por proyecto
    @GetMapping("/tareas/asignadas")
    public String verTareasAsignadas(Model model) {
        // Verificación de rol: Solo jefes pueden ver tareas asignadas
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        String nombreCompleto = auth.getName();

        // Búsqueda del jefe: Obtiene ID del jefe autenticado
        Long jefeId = null;
        List<UsuarioDTO> usuarios = usuarioService.findAll();
        for (UsuarioDTO usuario : usuarios) {
            if (usuario.getNombreCompleto().equals(nombreCompleto)) {
                jefeId = usuario.getId();
                break;
            }
        }

        if (jefeId == null) {
            model.addAttribute("error", "No se pudo encontrar el jefe");
            return "tareas/asignadas";
        }

        // Obtención de tareas: Tareas asignadas por el jefe agrupadas por proyecto
        Map<String, List<TareaDTO>> tareasAgrupadas = tareaService.findByJefeIdGroupByProyecto(jefeId);

        // Enriquecimiento de datos: Añade nombres de empleados a las tareas
        List<EmpleadoDTO> empleados = empleadoService.findAll();

        for (List<TareaDTO> tareas : tareasAgrupadas.values()) {
            for (TareaDTO tarea : tareas) {
                for (EmpleadoDTO empleado : empleados) {
                    if (empleado.getId().equals(tarea.getEmpleadoId())) {
                        tarea.setEmpleadoNombre(empleado.getNombreCompleto());
                        break;
                    }
                }
            }
        }

        model.addAttribute("tareasAgrupadas", tareasAgrupadas);

        // Información adicional: Lista de proyectos para contexto
        List<ProyectoDTO> proyectos = proyectoService.findAll();
        model.addAttribute("proyectos", proyectos);

        return "tareas/asignadas";
    }

    // Finalización de tareas: Permite al empleado marcar sus tareas como completadas
    @PostMapping("/tareas/finalizar")
    public String finalizarTarea(@RequestParam("tareaId") Long tareaId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        try {
            // Verificación de propiedad: Confirma que la tarea pertenece al empleado autenticado
            Long empleadoId = null;
            List<EmpleadoDTO> empleados = empleadoService.findAll();
            for (EmpleadoDTO empleado : empleados) {
                if (empleado.getNombreCompleto().equals(nombreCompleto)) {
                    empleadoId = empleado.getId();
                    break;
                }
            }

            if (empleadoId == null) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el empleado");
                return "redirect:/tareas/ver";
            }

            // Validación de tarea: Verifica existencia y pertenencia al empleado
            Optional<TareaDTO> tareaOpt = tareaService.findById(tareaId);
            if (tareaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar la tarea");
                return "redirect:/tareas/ver";
            }

            TareaDTO tarea = tareaOpt.get();
            if (!tarea.getEmpleadoId().equals(empleadoId)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para finalizar esta tarea");
                return "redirect:/tareas/ver";
            }

            // Finalización: Marca la tarea como completada en base de datos
            tareaService.finalizarTarea(tareaId);
            redirectAttributes.addFlashAttribute("mensaje", "Tarea finalizada correctamente");
            return "redirect:/tareas/ver";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al finalizar la tarea: " + e.getMessage());
            return "redirect:/tareas/ver";
        }
    }

    // Eliminación de tareas completadas: Solo jefes pueden eliminar tareas que ya están completadas
    @PostMapping("/tareas/eliminar")
    public String eliminarTarea(@RequestParam("tareaId") Long tareaId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verificación de rol: Solo jefes pueden eliminar tareas
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para realizar esta acción");
            return "redirect:/dashboard";
        }

        String nombreCompleto = auth.getName();

        try {
            // Búsqueda del jefe: Obtiene ID del jefe autenticado
            Long jefeId = null;
            List<UsuarioDTO> usuarios = usuarioService.findAll();
            for (UsuarioDTO usuario : usuarios) {
                if (usuario.getNombreCompleto().equals(nombreCompleto)) {
                    jefeId = usuario.getId();
                    break;
                }
            }

            if (jefeId == null) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el jefe");
                return "redirect:/tareas/asignadas";
            }

            // Validación de tarea: Verifica existencia, propiedad y estado completado
            Optional<TareaDTO> tareaOpt = tareaService.findById(tareaId);
            if (tareaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar la tarea");
                return "redirect:/tareas/asignadas";
            }

            TareaDTO tarea = tareaOpt.get();

            if (!tarea.getJefeId().equals(jefeId)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar esta tarea");
                return "redirect:/tareas/asignadas";
            }

            if (!tarea.isCompletada()) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden eliminar tareas completadas");
                return "redirect:/tareas/asignadas";
            }

            // Eliminación: Borra la tarea de la base de datos
            tareaService.eliminarTarea(tareaId);
            redirectAttributes.addFlashAttribute("mensaje", "Tarea eliminada correctamente");
            return "redirect:/tareas/asignadas";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la tarea: " + e.getMessage());
            return "redirect:/tareas/asignadas";
        }
    }

    // Página de confirmación para tareas: Muestra confirmación después de asignar una tarea
    @GetMapping("/tareas/confirmacion")
    public String confirmacionTarea(Model model) {
        // Verificación de rol: Solo jefes pueden ver confirmación de tareas
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        return "tareas/confirmacion";
    }
}
