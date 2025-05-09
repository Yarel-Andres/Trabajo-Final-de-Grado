package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.servicios.EmpleadoService;
import com.yarel.gestion_empresarial.servicios.TareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class VistasController {

    private final EmpleadoService empleadoService;
    private final TareaService tareaService;

    @Autowired
    public VistasController(EmpleadoService empleadoService, TareaService tareaService) {
        this.empleadoService = empleadoService;
        this.tareaService = tareaService;
    }

    @GetMapping("/")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() &&
                !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());

        boolean isJefe = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"));
        boolean isEmpleado = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLEADO"));

        model.addAttribute("isJefe", isJefe);
        model.addAttribute("isEmpleado", isEmpleado);

        return "dashboard/index";
    }

    @GetMapping("/tareas/asignar")
    public String asignarTareaForm(Model model) {
        // Verificar que el usuario es un jefe
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        // Obtener lista de empleados para el selector
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        model.addAttribute("empleados", empleados);
        model.addAttribute("tarea", new TareaDTO());

        return "tareas/asignar";
    }

    @PostMapping("/tareas/asignar")
    public String asignarTarea(@ModelAttribute TareaDTO tarea, RedirectAttributes redirectAttributes) {
        // Verificar que el usuario es un jefe
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        try {
            // Asignar la tarea usando el servicio existente
            TareaDTO nuevaTarea = tareaService.saveTareaForJefe(tarea, auth.getName());
            redirectAttributes.addFlashAttribute("mensaje", "Tarea asignada correctamente");
            return "redirect:/tareas/confirmacion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al asignar la tarea: " + e.getMessage());
            return "redirect:/tareas/asignar";
        }
    }

    @GetMapping("/tareas/confirmacion")
    public String confirmacionTarea() {
        return "tareas/confirmacion";
    }

    @GetMapping("/tareas/ver")
    public String verTareas(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Obtener el ID del empleado actual
        Long empleadoId = null;
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        for (EmpleadoDTO empleado : empleados) {
            if (empleado.getNombreUsuario().equals(username)) {
                empleadoId = empleado.getId();
                break;
            }
        }

        if (empleadoId == null) {
            model.addAttribute("error", "No se pudo encontrar el empleado");
            return "tareas/ver";
        }

        // Obtener las tareas del empleado
        List<TareaDTO> tareas = tareaService.findByEmpleadoId(empleadoId);
        model.addAttribute("tareas", tareas);

        return "tareas/ver";
    }
}
