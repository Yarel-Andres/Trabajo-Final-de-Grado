package com.yarel.gestion_empresarial.controller;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.proyecto.ProyectoDTO;
import com.yarel.gestion_empresarial.dto.registroTiempo.RegistroTiempoDTO;
import com.yarel.gestion_empresarial.dto.reunion.ReunionDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.dto.usuario.UsuarioDTO;
import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.servicios.*;
import lombok.RequiredArgsConstructor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class VistasController {

    private final EmpleadoService empleadoService;
    private final TareaService tareaService;
    private final ReunionService reunionService;
    private final UsuarioService usuarioService;
    private final ProyectoService proyectoService;
    private final RegistroTiempoService registroTiempoService;

    private static final Logger log = LoggerFactory.getLogger(VistasController.class);

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
        boolean isRRHH = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RRHH"));
        model.addAttribute("isRRHH", isRRHH);

        model.addAttribute("isJefe", isJefe);
        model.addAttribute("isEmpleado", isEmpleado);

        return "dashboard/index";
    }

    @GetMapping("/tareas/asignar")
    public String asignarTareaForm(Model model, Principal principal) {
        // Verificar que el usuario es un jefe
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        // Obtener lista de empleados para el selector
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        model.addAttribute("empleados", empleados);

        // Obtener lista de proyectos para el selector
        List<ProyectoDTO> proyectos = proyectoService.findAll();
        model.addAttribute("proyectos", proyectos);

        model.addAttribute("tarea", new TareaDTO());

        return "tareas/asignar";
    }

    @PostMapping("/tareas/asignar")
    public String asignarTarea(
            @ModelAttribute TareaDTO tarea,
            @RequestParam("fechaVencimientoDate") String fechaVencimientoDate,
            @RequestParam("horaVencimiento") int horaVencimiento,
            @RequestParam("minutosVencimiento") int minutosVencimiento,
            RedirectAttributes redirectAttributes) {
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

            // Convertir la fecha de string a LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fecha = LocalDate.parse(fechaVencimientoDate, formatter);
            LocalDateTime fechaHora = fecha.atTime(horaVencimiento, minutosVencimiento);
            tarea.setFechaVencimiento(fechaHora);

            // Asignar la tarea usando el servicio existente
            TareaDTO nuevaTarea = tareaService.saveTareaForJefe(tarea, nombreUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "Tarea asignada correctamente");
            return "redirect:/tareas/confirmacion";
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error completo en los logs
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

        // Obtener las tareas del empleado (solo las no completadas)
        List<TareaDTO> tareas = tareaService.findByEmpleadoId(empleadoId);
        // Filtrar para mostrar solo las tareas no completadas
        tareas = tareas.stream()
                .filter(tarea -> !tarea.isCompletada())
                .toList();

        // Obtener todos los usuarios para buscar los nombres de los jefes
        List<UsuarioDTO> usuarios = usuarioService.findAll();

        // Obtener todos los proyectos para añadir sus nombres a las tareas
        List<ProyectoDTO> proyectos = proyectoService.findAll();

        // Añadir el nombre del jefe y del proyecto a cada tarea
        for (TareaDTO tarea : tareas) {
            // Buscar y asignar el nombre del jefe
            if (tarea.getJefeId() != null) {
                for (UsuarioDTO usuario : usuarios) {
                    if (usuario.getId().equals(tarea.getJefeId())) {
                        tarea.setJefeNombre(usuario.getNombreCompleto());
                        break;
                    }
                }
            }

            // Buscar y asignar el nombre del proyecto
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

    // Finalizar tarea para el empleado
    @PostMapping("/tareas/finalizar")
    public String finalizarTarea(@RequestParam("tareaId") Long tareaId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        try {
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
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el empleado");
                return "redirect:/tareas/ver";
            }

            // Verificar que la tarea pertenece al empleado
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

            // Finalizar la tarea
            tareaService.finalizarTarea(tareaId);
            redirectAttributes.addFlashAttribute("mensaje", "Tarea finalizada correctamente");
            return "redirect:/tareas/ver";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al finalizar la tarea: " + e.getMessage());
            return "redirect:/tareas/ver";
        }
    }


    // Eliminar tareas asignadas para el jefe
    @PostMapping("/tareas/eliminar")
    public String eliminarTarea(@RequestParam("tareaId") Long tareaId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verificar que el usuario es un jefe
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para realizar esta acción");
            return "redirect:/dashboard";
        }

        String nombreCompleto = auth.getName();

        try {
            // Obtener el ID del jefe actual por su nombre completo
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

            // Verificar que la tarea existe y está completada
            Optional<TareaDTO> tareaOpt = tareaService.findById(tareaId);
            if (tareaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar la tarea");
                return "redirect:/tareas/asignadas";
            }

            TareaDTO tarea = tareaOpt.get();

            // Verificar que la tarea fue asignada por este jefe
            if (!tarea.getJefeId().equals(jefeId)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar esta tarea");
                return "redirect:/tareas/asignadas";
            }

            // Verificar que la tarea está completada
            if (!tarea.isCompletada()) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden eliminar tareas completadas");
                return "redirect:/tareas/asignadas";
            }

            // Eliminar la tarea
            tareaService.eliminarTarea(tareaId);
            redirectAttributes.addFlashAttribute("mensaje", "Tarea eliminada correctamente");
            return "redirect:/tareas/asignadas";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la tarea: " + e.getMessage());
            return "redirect:/tareas/asignadas";
        }
    }

    @GetMapping("/finalizados")
    public String verFinalizados(Model model) {
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
            return "finalizados/index";
        }

        // Obtener las tareas completadas del empleado
        List<TareaDTO> tareasCompletadas = tareaService.findCompletadasByEmpleadoId(empleadoId);

        // Obtener las reuniones completadas del empleado
        List<ReunionDTO> reunionesCompletadas = reunionService.findCompletadasByParticipanteId(empleadoId);

        // Obtener los proyectos completados del empleado
        List<ProyectoDTO> proyectosCompletados = proyectoService.findCompletadosByEmpleadoId(empleadoId);

        // Obtener todos los usuarios para buscar los nombres de los jefes
        List<UsuarioDTO> usuarios = usuarioService.findAll();

        // Obtener todos los proyectos para añadir sus nombres a las tareas
        List<ProyectoDTO> proyectos = proyectoService.findAll();

        // Crear un mapa para acceder rápidamente a los usuarios por ID
        Map<Long, String> usuariosMap = new HashMap<>();
        for (UsuarioDTO usuario : usuarios) {
            usuariosMap.put(usuario.getId(), usuario.getNombreCompleto());
        }

        // Añadir el mapa de usuarios al modelo
        model.addAttribute("usuariosMap", usuariosMap);

        // Añadir el nombre del jefe y del proyecto a cada tarea
        for (TareaDTO tarea : tareasCompletadas) {
            // Buscar y asignar el nombre del jefe
            if (tarea.getJefeId() != null) {
                for (UsuarioDTO usuario : usuarios) {
                    if (usuario.getId().equals(tarea.getJefeId())) {
                        tarea.setJefeNombre(usuario.getNombreCompleto());
                        break;
                    }
                }
            }

            // Buscar y asignar el nombre del proyecto
            if (tarea.getProyectoId() != null) {
                for (ProyectoDTO proyecto : proyectos) {
                    if (proyecto.getId().equals(tarea.getProyectoId())) {
                        tarea.setNombreProyecto(proyecto.getNombre());
                        break;
                    }
                }
            }
        }

        // Añadir los nombres de los participantes a cada reunión
        for (ReunionDTO reunion : reunionesCompletadas) {
            // Asignar el nombre del organizador
            if (reunion.getOrganizadorId() != null) {
                String nombreOrganizador = usuariosMap.get(reunion.getOrganizadorId());
                if (nombreOrganizador != null) {
                    reunion.setOrganizadorNombre(nombreOrganizador);
                }
            }
        }

        // Añadir los nombres de los empleados a cada proyecto
        for (ProyectoDTO proyecto : proyectosCompletados) {
            if (proyecto.getEmpleadosIds() != null && !proyecto.getEmpleadosIds().isEmpty()) {
                for (Long empleadoId2 : proyecto.getEmpleadosIds()) {
                    String nombreEmpleado = usuariosMap.get(empleadoId2);
                    if (nombreEmpleado != null) {
                        proyecto.getEmpleadosNombres().add(nombreEmpleado);
                    }
                }
            }
        }

        model.addAttribute("tareasCompletadas", tareasCompletadas);
        model.addAttribute("reunionesCompletadas", reunionesCompletadas);
        model.addAttribute("proyectosCompletados", proyectosCompletados);

        return "finalizados/index";
    }

    // Proyectos finalizados
    @PostMapping("/proyectos/finalizar")
    public String finalizarProyecto(@RequestParam("proyectoId") Long proyectoId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        try {
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
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario");
                return "redirect:/proyectos/ver";
            }

            // Verificar que el proyecto existe
            Optional<ProyectoDTO> proyectoOpt = proyectoService.findById(proyectoId);
            if (proyectoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el proyecto");
                return "redirect:/proyectos/ver";
            }

            // Finalizar el proyecto
            proyectoService.finalizarProyecto(proyectoId);
            redirectAttributes.addFlashAttribute("mensaje", "Proyecto finalizado correctamente");
            return "redirect:/proyectos/ver";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al finalizar el proyecto: " + e.getMessage());
            return "redirect:/proyectos/ver";
        }
    }

    @GetMapping("/tareas/asignadas")
    public String verTareasAsignadas(Model model) {
        // Verificar que el usuario es un jefe
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        String nombreCompleto = auth.getName();

        // Obtener el ID del jefe actual por su nombre completo
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

        // Obtener las tareas asignadas por el jefe, agrupadas por proyecto
        Map<String, List<TareaDTO>> tareasAgrupadas = tareaService.findByJefeIdGroupByProyecto(jefeId);

        // Obtener todos los empleados para añadir sus nombres a las tareas
        List<EmpleadoDTO> empleados = empleadoService.findAll();

        // Añadir el nombre del empleado a cada tarea
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

        // Obtener todos los proyectos para mostrar información adicional
        List<ProyectoDTO> proyectos = proyectoService.findAll();
        model.addAttribute("proyectos", proyectos);

        return "tareas/asignadas";
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

        // No necesitamos establecer fechaHora aquí ya que ahora usamos campos separados
        model.addAttribute("reunion", reunion);

        // Establecer la fecha actual como valor por defecto para el campo de fecha
        model.addAttribute("fechaActual", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return "reuniones/crear";
    }

    @PostMapping("/reuniones/crear")
    public String crearReunion(
            @ModelAttribute ReunionDTO reunion,
            @RequestParam(value = "participantesIds", required = false) List<Long> participantesIds,
            @RequestParam("fechaReunionDate") String fechaReunionDate,
            @RequestParam("horaReunion") int horaReunion,
            @RequestParam("minutosReunion") int minutosReunion,
            RedirectAttributes redirectAttributes) {
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

            if (reunion.getSala() == null || reunion.getSala().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La sala de la reunión es obligatoria");
                return "redirect:/reuniones/crear";
            }

            // Verificar que hay al menos un participante seleccionado
            if (participantesIds == null || participantesIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar al menos un participante");
                return "redirect:/reuniones/crear";
            }

            // Convertir la fecha de string a LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fecha = LocalDate.parse(fechaReunionDate, formatter);
            LocalDateTime fechaHora = fecha.atTime(horaReunion, minutosReunion);
            reunion.setFechaHora(fechaHora);

            // Asignar los participantes seleccionados
            reunion.setParticipantesIds(new HashSet<>(participantesIds));

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

    // Modificar el método verReuniones para filtrar solo las reuniones no completadas
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

        // Filtrar para mostrar solo las reuniones no completadas
        reuniones = reuniones.stream()
                .filter(reunion -> !reunion.isCompletada())
                .collect(Collectors.toList());

        // Crear un mapa para acceder rápidamente a los usuarios por ID
        Map<Long, String> usuariosMap = new HashMap<>();
        for (UsuarioDTO usuario : usuarios) {
            usuariosMap.put(usuario.getId(), usuario.getNombreCompleto());
        }

        // Añadir los nombres de los participantes a cada reunión
        for (ReunionDTO reunion : reuniones) {
            Set<String> participantesNombres = new HashSet<>();
            if (reunion.getParticipantesIds() != null) {
                for (Long participanteId : reunion.getParticipantesIds()) {
                    String nombreParticipante = usuariosMap.get(participanteId);
                    if (nombreParticipante != null) {
                        participantesNombres.add(nombreParticipante);
                    }
                }
            }
            // Almacenar los nombres en un atributo temporal
            model.addAttribute("participantesNombres_" + reunion.getId(), participantesNombres);

            // Asignar el nombre del organizador
            if (reunion.getOrganizadorId() != null) {
                String nombreOrganizador = usuariosMap.get(reunion.getOrganizadorId());
                if (nombreOrganizador != null) {
                    reunion.setOrganizadorNombre(nombreOrganizador);
                }
            }
        }

        model.addAttribute("reuniones", reuniones);

        return "reuniones/ver";
    }

    @GetMapping("/reuniones/asignadas")
    public String verReunionesAsignadas(Model model) {
        // Verificar que el usuario es un jefe
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        String nombreCompleto = auth.getName();

        // Obtener el ID del jefe actual por su nombre completo
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
            return "reuniones/asignadas";
        }

        // Obtener las reuniones organizadas por el jefe
        List<ReunionDTO> reuniones = reunionService.findByOrganizadorId(jefeId);

        // Obtener todos los usuarios para añadir sus nombres a las reuniones
        List<UsuarioDTO> todosUsuarios = usuarioService.findAll();

        // Crear un mapa para acceder rápidamente a los usuarios por ID
        Map<Long, String> usuariosMap = new HashMap<>();
        for (UsuarioDTO usuario : todosUsuarios) {
            usuariosMap.put(usuario.getId(), usuario.getNombreCompleto());
        }

        // Añadir los nombres de los participantes a cada reunión
        for (ReunionDTO reunion : reuniones) {
            Set<String> participantesNombres = new HashSet<>();
            if (reunion.getParticipantesIds() != null) {
                for (Long participanteId : reunion.getParticipantesIds()) {
                    String nombreParticipante = usuariosMap.get(participanteId);
                    if (nombreParticipante != null) {
                        participantesNombres.add(nombreParticipante);
                    }
                }
            }
            // Almacenar los nombres en un atributo temporal
            reunion.setOrganizadorNombre(nombreCompleto);
            model.addAttribute("participantesNombres_" + reunion.getId(), participantesNombres);
        }

        model.addAttribute("reuniones", reuniones);

        return "reuniones/asignadas";
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
        boolean isRRHH = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RRHH"));

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

        // Si es RRHH, redirigir a la vista de RRHH para proyectos
        if (isRRHH) {
            return "redirect:/rrhh/proyectos";
        }
        // Si es jefe, mostrar sus proyectos creados
        else if (isJefe) {
            proyectos = proyectoService.findByJefeId(usuarioId);
        } else {
            // Si es empleado, mostrar los proyectos asignados
            proyectos = proyectoService.findByEmpleadoId(usuarioId);
            // Filtrar para mostrar solo los proyectos no completados
            proyectos = proyectos.stream()
                    .filter(proyecto -> !proyecto.isCompletado())
                    .collect(Collectors.toList());
        }

        // Obtener todos los empleados para añadir sus nombres a los proyectos
        List<EmpleadoDTO> empleados = empleadoService.findAll();

        // Crear un mapa para acceder rápidamente a los empleados por ID
        Map<Long, String> empleadosMap = new HashMap<>();
        for (EmpleadoDTO empleado : empleados) {
            empleadosMap.put(empleado.getId(), empleado.getNombreCompleto());
        }

        // Añadir los nombres de los empleados a cada proyecto
        for (ProyectoDTO proyecto : proyectos) {
            if (proyecto.getEmpleadosIds() != null && !proyecto.getEmpleadosIds().isEmpty()) {
                for (Long empleadoId : proyecto.getEmpleadosIds()) {
                    String nombreEmpleado = empleadosMap.get(empleadoId);
                    if (nombreEmpleado != null) {
                        proyecto.getEmpleadosNombres().add(nombreEmpleado);
                    }
                }
            }
        }

        model.addAttribute("proyectos", proyectos);
        model.addAttribute("isJefe", isJefe);

        return "proyectos/ver";
    }



    // Mostrar formulario para registrar tiempo
    @GetMapping("/registros-tiempo/crear")
    public String crearRegistroTiempoForm(Model model,
                                          @RequestParam(required = false) Long tareaId,
                                          @RequestParam(required = false) Long proyectoId,
                                          @RequestParam(required = false) Long reunionId) {
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

        // Crear un nuevo DTO para el registro de tiempo
        RegistroTiempoDTO registroTiempo = new RegistroTiempoDTO();

        // Determinar el tipo de actividad basado en los parámetros recibidos
        String tipoActividad = null;

        // Si se proporciona un ID de tarea
        if (tareaId != null) {
            registroTiempo.setTareaId(tareaId);
            tipoActividad = "tarea";
        }
        // Si se proporciona un ID de proyecto
        else if (proyectoId != null) {
            registroTiempo.setProyectoId(proyectoId);
            tipoActividad = "proyecto";
        }
        // Si se proporciona un ID de reunión
        else if (reunionId != null) {
            registroTiempo.setReunionId(reunionId);
            tipoActividad = "reunion";
        }

        // Añadir el tipo de actividad al modelo si se ha determinado
        if (tipoActividad != null) {
            model.addAttribute("tipoActividadSeleccionada", tipoActividad);
            model.addAttribute("mostrarSoloUnaOpcion", true);
        } else {
            model.addAttribute("mostrarSoloUnaOpcion", false);
        }

        model.addAttribute("registroTiempo", registroTiempo);

        // Establecer la fecha actual como valor por defecto
        model.addAttribute("fechaActual", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

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

            try {
                // Convertir las fechas de string a LocalDateTime
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Fecha de inicio
                LocalDate fechaInicio = LocalDate.parse(registroTiempo.getFechaInicioTemp(), formatter);
                LocalDateTime horaInicioDateTime = fechaInicio.atTime(
                        registroTiempo.getHoraInicioTemp(),
                        registroTiempo.getMinutosInicioTemp()
                );
                registroTiempo.setHoraInicio(horaInicioDateTime);

                // Fecha de fin
                LocalDate fechaFin = LocalDate.parse(registroTiempo.getFechaFinTemp(), formatter);
                LocalDateTime horaFinDateTime = fechaFin.atTime(
                        registroTiempo.getHoraFinTemp(),
                        registroTiempo.getMinutosFinTemp()
                );
                registroTiempo.setHoraFin(horaFinDateTime);

                // Establecer la fecha de registro (usamos la fecha de inicio)
                registroTiempo.setFechaRegistro(fechaInicio);

                // Calcular horas trabajadas
                double horasTrabajadas = calcularHorasTrabajadas(horaInicioDateTime, horaFinDateTime);
                registroTiempo.setHorasTrabajadas(horasTrabajadas);

                // Guardar el registro de tiempo
                RegistroTiempoDTO nuevoRegistro = registroTiempoService.saveForUsuario(registroTiempo, nombreUsuario);
                redirectAttributes.addFlashAttribute("mensaje", "Tiempo registrado correctamente");
                return "redirect:/registros-tiempo/confirmacion";
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Error al procesar las fechas: " + e.getMessage());
                return "redirect:/registros-tiempo/crear";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al registrar el tiempo: " + e.getMessage());
            return "redirect:/registros-tiempo/crear";
        }
    }

    // Método auxiliar para calcular las horas trabajadas entre dos fechas
    private double calcularHorasTrabajadas(LocalDateTime inicio, LocalDateTime fin) {
        long segundos = java.time.Duration.between(inicio, fin).getSeconds();
        return segundos / 3600.0; // Convertir segundos a horas
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

        // Obtener información del empleado
        Optional<EmpleadoDTO> empleadoOpt = empleadoService.findById(empleadoId);
        if (empleadoOpt.isEmpty()) {
            model.addAttribute("error", "Empleado no encontrado");
            return "registros-tiempo/informes";
        }

        model.addAttribute("empleado", empleadoOpt.get());

        // Obtener registros de tiempo del empleado
        List<RegistroTiempoDTO> registros = registroTiempoService.findByUsuarioId(empleadoId);
        model.addAttribute("registros", registros);

        return "registros-tiempo/informes-empleado";
    }

    // Obtener informes de tiempo por proyecto (para jefes y RRHH)
    @GetMapping("/registros-tiempo/informes/proyecto/{proyectoId}")
    public String verInformesRegistrosTiempoPorProyecto(@PathVariable Long proyectoId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verificar que el usuario es un jefe o RRHH
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE")) &&
                !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RRHH"))) {
            return "redirect:/dashboard";
        }

        // Obtener información del proyecto
        Optional<ProyectoDTO> proyectoOpt = proyectoService.findById(proyectoId);
        if (proyectoOpt.isEmpty()) {
            model.addAttribute("error", "Proyecto no encontrado");
            return "registros-tiempo/informes";
        }

        model.addAttribute("proyecto", proyectoOpt.get());

        try {
            // Obtener registros de tiempo del proyecto
            List<RegistroTiempoDTO> registros = registroTiempoService.findByProyectoId(proyectoId);

            // Cargar todos los usuarios una sola vez para mejorar el rendimiento
            List<UsuarioDTO> todosLosUsuarios = usuarioService.findAll();
            Map<Long, String> mapaUsuarios = new HashMap<>();
            for (UsuarioDTO usuario : todosLosUsuarios) {
                mapaUsuarios.put(usuario.getId(), usuario.getNombreCompleto());
            }

            // Enriquecer los registros con nombres de usuario
            for (RegistroTiempoDTO registro : registros) {
                if (registro.getUsuarioId() != null) {
                    String nombreUsuario = mapaUsuarios.get(registro.getUsuarioId());
                    if (nombreUsuario != null) {
                        registro.setUsuarioNombre(nombreUsuario);
                    } else {
                        registro.setUsuarioNombre("Usuario ID: " + registro.getUsuarioId());
                    }
                } else {
                    registro.setUsuarioNombre("No asignado");
                }

                // Añadir información de depuración al modelo
                System.out.println("Registro ID: " + registro.getId() +
                        ", Usuario ID: " + registro.getUsuarioId() +
                        ", Nombre asignado: " + registro.getUsuarioNombre());
            }

            model.addAttribute("registros", registros);

            // Añadir información de depuración al modelo
            model.addAttribute("totalRegistros", registros.size());
            model.addAttribute("totalUsuarios", todosLosUsuarios.size());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los registros de tiempo: " + e.getMessage());
        }

        return "registros-tiempo/informes-proyecto";
    }


    // Añadir este método al controlador
    @PostMapping("/reuniones/finalizar")
    public String finalizarReunion(@RequestParam("reunionId") Long reunionId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        try {
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
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario");
                return "redirect:/reuniones/ver";
            }

            // Verificar que la reunión existe
            Optional<ReunionDTO> reunionOpt = reunionService.findById(reunionId);
            if (reunionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar la reunión");
                return "redirect:/reuniones/ver";
            }

            // Finalizar la reunión
            reunionService.finalizarReunion(reunionId);
            redirectAttributes.addFlashAttribute("mensaje", "Reunión finalizada correctamente");
            return "redirect:/reuniones/ver";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al finalizar la reunión: " + e.getMessage());
            return "redirect:/reuniones/ver";
        }
    }


    // Calendario
    @GetMapping("/calendario")
    public String verCalendario(Model model) {
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
            return "calendario/calendario"; // Apunta al archivo en la subcarpeta
        }

        // 1. Obtener todas las tareas del empleado
        List<TareaDTO> tareas = tareaService.findByEmpleadoId(empleadoId);

        // 2. Obtener los proyectos en los que participa el empleado
        List<ProyectoDTO> proyectosDelEmpleado = proyectoService.findByEmpleadoId(empleadoId);

        // 3. Obtener las reuniones en las que participa el empleado
        List<ReunionDTO> reuniones = reunionService.findByParticipanteId(empleadoId);

        // Obtener todos los usuarios para buscar los nombres
        List<UsuarioDTO> usuarios = usuarioService.findAll();
        Map<Long, String> usuariosMap = new HashMap<>();
        for (UsuarioDTO usuario : usuarios) {
            usuariosMap.put(usuario.getId(), usuario.getNombreCompleto());
        }

        // Obtener todos los proyectos para añadir sus nombres a las tareas
        List<ProyectoDTO> proyectos = proyectoService.findAll();

        // Añadir el nombre del jefe y del proyecto a cada tarea
        for (TareaDTO tarea : tareas) {
            // Buscar y asignar el nombre del jefe
            if (tarea.getJefeId() != null) {
                tarea.setJefeNombre(usuariosMap.getOrDefault(tarea.getJefeId(), "No asignado"));
            }

            // Buscar y asignar el nombre del proyecto
            if (tarea.getProyectoId() != null) {
                for (ProyectoDTO proyecto : proyectos) {
                    if (proyecto.getId().equals(tarea.getProyectoId())) {
                        tarea.setNombreProyecto(proyecto.getNombre());
                        break;
                    }
                }
            }
        }

        // Enriquecer las reuniones con información adicional
        for (ReunionDTO reunion : reuniones) {
            // Asignar el nombre del organizador
            if (reunion.getOrganizadorId() != null) {
                reunion.setOrganizadorNombre(usuariosMap.getOrDefault(reunion.getOrganizadorId(), "No asignado"));
            }
        }

        // Añadir todos los datos al modelo
        model.addAttribute("tareas", tareas);
        model.addAttribute("proyectos", proyectosDelEmpleado);
        model.addAttribute("reuniones", reuniones);
        model.addAttribute("username", nombreCompleto); // Importante para el layout

        return "calendario/calendario"; // Apunta al archivo en la subcarpeta
    }
}
