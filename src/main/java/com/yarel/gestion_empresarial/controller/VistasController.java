package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.reunion.ReunionDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.dto.usuario.UsuarioDTO;
import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.servicios.EmpleadoService;
import com.yarel.gestion_empresarial.servicios.ReunionService;
import com.yarel.gestion_empresarial.servicios.TareaService;
import com.yarel.gestion_empresarial.servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;

@Controller
public class VistasController {

    private final EmpleadoService empleadoService;
    private final TareaService tareaService;
    private final ReunionService reunionService;
    private final UsuarioService usuarioService;

    @Autowired
    public VistasController(EmpleadoService empleadoService, TareaService tareaService, ReunionService reunionService, UsuarioService usuarioService) {
        this.empleadoService = empleadoService;
        this.tareaService = tareaService;
        this.reunionService = reunionService;
        this.usuarioService = usuarioService;
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



    @GetMapping("/reuniones/crear")
    public String crearReunionForm(Model model) {
        // Verificar que el usuario es un jefe
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        // Obtener lista de empleados para el selector
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        model.addAttribute("empleados", empleados);

        // Crear un DTO vacío para el formulario
        ReunionDTO reunion = new ReunionDTO();
        reunion.setFechaHora(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        model.addAttribute("reunion", reunion);

        return "reuniones/crear";
    }


    @PostMapping("/reuniones/crear")
    public String crearReunion(@ModelAttribute ReunionDTO reunion, @RequestParam(value = "participantesIds", required = false) List<Long> participantesIds, RedirectAttributes redirectAttributes) {
        // Verificar que el usuario es un jefe
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        try {
            // Debug - imprimir valores recibidos
            System.out.println("Título: " + reunion.getTitulo());
            System.out.println("Fecha y hora: " + reunion.getFechaHora());
            System.out.println("Sala: " + reunion.getSala());
            System.out.println("Participantes IDs: " + participantesIds);

            // Verificar que los datos necesarios estén presentes
            if (reunion.getTitulo() == null || reunion.getTitulo().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El título de la reunión es obligatorio");
                return "redirect:/reuniones/crear";
            }

            if (reunion.getFechaHora() == null) {
                redirectAttributes.addFlashAttribute("error", "La fecha y hora de la reunión son obligatorias");
                return "redirect:/reuniones/crear";
            }

            if (reunion.getSala() == null || reunion.getSala().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La sala de la reunión es obligatoria");
                return "redirect:/reuniones/crear";
            }

            // Asignar los participantes seleccionados
            if (participantesIds != null && !participantesIds.isEmpty()) {
                reunion.setParticipantesIds(new HashSet<>(participantesIds));
                System.out.println("Participantes asignados: " + reunion.getParticipantesIds());
            } else {
                System.out.println("No se seleccionaron participantes");
                reunion.setParticipantesIds(new HashSet<>());
            }

            // Asignar la reunión usando el servicio
            ReunionDTO nuevaReunion = reunionService.saveReunionForJefe(reunion, auth.getName());
            redirectAttributes.addFlashAttribute("mensaje", "Reunión programada correctamente");
            return "redirect:/reuniones/confirmacion";
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error completo en los logs
            redirectAttributes.addFlashAttribute("error", "Error al programar la reunión: " + e.getMessage());
            return "redirect:/reuniones/crear";
        }
    }

    @GetMapping("/reuniones/confirmacion")
    public String confirmacionReunion() {
        return "reuniones/confirmacion";
    }

    @GetMapping("/reuniones/ver")
    public String verReuniones(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Obtener el ID del usuario actual
        Long usuarioId = null;
        List<UsuarioDTO> usuarios = usuarioService.findAll();
        for (UsuarioDTO usuario : usuarios) {
            if (usuario.getNombreUsuario().equals(username)) {
                usuarioId = usuario.getId();
                break;
            }
        }

        if (usuarioId == null) {
            model.addAttribute("error", "No se pudo encontrar el usuario");
            return "reuniones/ver";
        }

        // Obtener las reuniones del usuario
        List<ReunionDTO> reuniones = reunionService.findByParticipanteId(usuarioId);
        model.addAttribute("reuniones", reuniones);

        return "reuniones/ver";
    }



}
