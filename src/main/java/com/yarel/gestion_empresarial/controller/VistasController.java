package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.proyecto.ProyectoDTO;
import com.yarel.gestion_empresarial.dto.registroTiempo.RegistroTiempoDTO;
import com.yarel.gestion_empresarial.dto.reunion.ReunionDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.dto.usuario.UsuarioDTO;
import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.servicios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Optional;

@Controller
public class VistasController {

    private final EmpleadoService empleadoService;
    private final TareaService tareaService;
    private final ReunionService reunionService;
    private final UsuarioService usuarioService;
    private final ProyectoService proyectoService;
    private final RegistroTiempoService registroTiempoService;

    @Autowired
    public VistasController(EmpleadoService empleadoService, TareaService tareaService, ReunionService reunionService, UsuarioService usuarioService, ProyectoService proyectoService, RegistroTiempoService registroTiempoService) {
        this.empleadoService = empleadoService;
        this.tareaService = tareaService;
        this.reunionService = reunionService;
        this.usuarioService = usuarioService;
        this.proyectoService = proyectoService;
        this.registroTiempoService = registroTiempoService;
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
            // Buscar el nombre de usuario del jefe por su nombre completo
            String nombreCompleto = auth.getName();
            Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);

            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario");
                return "redirect:/tareas/asignar";
            }

            String nombreUsuario = usuarioOpt.get().getNombreUsuario();

            // Asignar la tarea usando el servicio existente
            TareaDTO nuevaTarea = tareaService.saveTareaForJefe(tarea, nombreUsuario);
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
        String nombreCompleto = auth.getName();

        // Obtener el ID del empleado actual por su nombre completo
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
            // Buscar el nombre de usuario del jefe por su nombre completo
            String nombreCompleto = auth.getName();
            Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);

            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario");
                return "redirect:/reuniones/crear";
            }

            String nombreUsuario = usuarioOpt.get().getNombreUsuario();

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
            } else {
                reunion.setParticipantesIds(new HashSet<>());
            }

            // Asignar la reunión usando el servicio
            ReunionDTO nuevaReunion = reunionService.saveReunionForJefe(reunion, nombreUsuario);
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
        String nombreCompleto = auth.getName();

        // Obtener el ID del usuario actual por su nombre completo
        Long usuarioId = null;
        List<UsuarioDTO> usuarios = usuarioService.findAll();
        for (UsuarioDTO usuario : usuarios) {
            if (usuario.getNombreCompleto().equals(nombreCompleto)) {
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

    @GetMapping("/proyectos/crear")
    public String crearProyectoForm(Model model) {
        // Verificar que el usuario es un jefe
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        // Obtener lista de empleados para el selector
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        model.addAttribute("empleados", empleados);

        // Crear un DTO vacío para el formulario
        ProyectoDTO proyecto = new ProyectoDTO();
        proyecto.setFechaInicio(LocalDate.now());
        proyecto.setFechaFinEstimada(LocalDate.now().plusMonths(3));
        proyecto.setEstado("PLANIFICACION");
        model.addAttribute("proyecto", proyecto);

        return "proyectos/crear";
    }

    @PostMapping("/proyectos/crear")
    public String crearProyecto(@ModelAttribute ProyectoDTO proyecto, @RequestParam(value = "empleadosIds", required = false) List<Long> empleadosIds, RedirectAttributes redirectAttributes) {
        // Verificar que el usuario es un jefe
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        try {
            // Buscar el nombre de usuario del jefe por su nombre completo
            String nombreCompleto = auth.getName();
            Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);

            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario");
                return "redirect:/proyectos/crear";
            }

            String nombreUsuario = usuarioOpt.get().getNombreUsuario();

            // Verificar que los datos necesarios estén presentes
            if (proyecto.getNombre() == null || proyecto.getNombre().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El nombre del proyecto es obligatorio");
                return "redirect:/proyectos/crear";
            }

            if (proyecto.getFechaInicio() == null) {
                redirectAttributes.addFlashAttribute("error", "La fecha de inicio es obligatoria");
                return "redirect:/proyectos/crear";
            }

            if (proyecto.getFechaFinEstimada() == null) {
                redirectAttributes.addFlashAttribute("error", "La fecha de fin estimada es obligatoria");
                return "redirect:/proyectos/crear";
            }

            // Asignar los empleados seleccionados
            if (empleadosIds != null && !empleadosIds.isEmpty()) {
                proyecto.setEmpleadosIds(new HashSet<>(empleadosIds));
            } else {
                proyecto.setEmpleadosIds(new HashSet<>());
            }

            // Asignar el proyecto usando el servicio
            ProyectoDTO nuevoProyecto = proyectoService.saveProyectoForJefe(proyecto, nombreUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "Proyecto creado correctamente");
            return "redirect:/proyectos/confirmacion";
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error completo en los logs
            redirectAttributes.addFlashAttribute("error", "Error al crear el proyecto: " + e.getMessage());
            return "redirect:/proyectos/crear";
        }
    }

    @GetMapping("/proyectos/confirmacion")
    public String confirmacionProyecto() {
        return "proyectos/confirmacion";
    }

    @GetMapping("/proyectos/ver")
    public String verProyectos(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();
        boolean isJefe = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"));

        // Obtener el ID del usuario actual por su nombre completo
        Long usuarioId = null;
        List<UsuarioDTO> usuarios = usuarioService.findAll();
        for (UsuarioDTO usuario : usuarios) {
            if (usuario.getNombreCompleto().equals(nombreCompleto)) {
                usuarioId = usuario.getId();
                break;
            }
        }

        if (usuarioId == null) {
            model.addAttribute("error", "No se pudo encontrar el usuario");
            return "proyectos/ver";
        }

        List<ProyectoDTO> proyectos;

        // Si es jefe, mostrar sus proyectos creados
        if (isJefe) {
            proyectos = proyectoService.findByJefeId(usuarioId);
        } else {
            // Si es empleado, mostrar los proyectos asignados
            proyectos = proyectoService.findByEmpleadoId(usuarioId);
        }

        model.addAttribute("proyectos", proyectos);
        model.addAttribute("isJefe", isJefe);

        return "proyectos/ver";
    }



    // Mostrar formulario para registrar tiempo
    @GetMapping("/registros-tiempo/crear")
    public String crearRegistroTiempoForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        // Obtener el ID del usuario actual
        Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("error", "No se pudo encontrar el usuario");
            return "registros-tiempo/crear";
        }

        // Obtener tareas, proyectos y reuniones disponibles
        List<TareaDTO> tareas = new ArrayList<>();
        List<ProyectoDTO> proyectos = new ArrayList<>();
        List<ReunionDTO> reuniones = new ArrayList<>();

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLEADO"))) {
            // Si es empleado, mostrar solo sus tareas, proyectos y reuniones
            Long usuarioId = usuarioOpt.get().getId();
            tareas = tareaService.findByEmpleadoId(usuarioId);
            proyectos = proyectoService.findByEmpleadoId(usuarioId);
            reuniones = reunionService.findByParticipanteId(usuarioId);
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            // Si es jefe, mostrar todas las tareas, proyectos y reuniones que ha creado
            Long usuarioId = usuarioOpt.get().getId();
            tareas = tareaService.findAll(); // Filtrar por jefe si es necesario
            proyectos = proyectoService.findByJefeId(usuarioId);
            reuniones = reunionService.findAll(); // Filtrar por organizador si es necesario
        }

        model.addAttribute("tareas", tareas);
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("reuniones", reuniones);
        model.addAttribute("registroTiempo", new RegistroTiempoDTO());

        return "registros-tiempo/crear";
    }

    // Procesar el formulario de registro de tiempo
    @PostMapping("/registros-tiempo/crear")
    public String crearRegistroTiempo(@ModelAttribute RegistroTiempoDTO registroTiempo, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        try {
            // Buscar el nombre de usuario por su nombre completo
            Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);

            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario");
                return "redirect:/registros-tiempo/crear";
            }

            String nombreUsuario = usuarioOpt.get().getNombreUsuario();

            // Guardar el registro de tiempo
            RegistroTiempoDTO nuevoRegistro = registroTiempoService.saveForUsuario(registroTiempo, nombreUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "Tiempo registrado correctamente");
            return "redirect:/registros-tiempo/confirmacion";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al registrar el tiempo: " + e.getMessage());
            return "redirect:/registros-tiempo/crear";
        }
    }

    // Página de confirmación
    @GetMapping("/registros-tiempo/confirmacion")
    public String confirmacionRegistroTiempo() {
        return "registros-tiempo/confirmacion";
    }

    // Ver registros de tiempo
    @GetMapping("/registros-tiempo/ver")
    public String verRegistrosTiempo(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        // Obtener el ID del usuario actual
        Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("error", "No se pudo encontrar el usuario");
            return "registros-tiempo/ver";
        }

        Long usuarioId = usuarioOpt.get().getId();
        List<RegistroTiempoDTO> registros = registroTiempoService.findByUsuarioId(usuarioId);
        model.addAttribute("registros", registros);

        return "registros-tiempo/ver";
    }

    // Ver informes de tiempo (para jefes y RRHH)
    @GetMapping("/registros-tiempo/informes")
    public String verInformesRegistrosTiempo(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verificar que el usuario es un jefe o RRHH
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE")) &&
                !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RRHH"))) {
            return "redirect:/dashboard";
        }

        // Obtener todos los empleados para el selector
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        model.addAttribute("empleados", empleados);

        // Obtener todos los proyectos para el selector
        List<ProyectoDTO> proyectos = proyectoService.findAll();
        model.addAttribute("proyectos", proyectos);

        return "registros-tiempo/informes";
    }

    // Obtener informes de tiempo por empleado (para jefes y RRHH)
    @GetMapping("/registros-tiempo/informes/empleado/{empleadoId}")
    public String verInformesRegistrosTiempoPorEmpleado(@PathVariable Long empleadoId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verificar que el usuario es un jefe o RRHH
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE")) &&
                !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RRHH"))) {
            return "redirect:/dashboard";
        }

        List<RegistroTiempoDTO> registros = registroTiempoService.findByUsuarioId(empleadoId);
        model.addAttribute("registros", registros);

        // Obtener información del empleado
        Optional<EmpleadoDTO> empleadoOpt = empleadoService.findById(empleadoId);
        empleadoOpt.ifPresent(empleado -> model.addAttribute("empleado", empleado));

        return "registros-tiempo/informes-empleado";
    }
}
