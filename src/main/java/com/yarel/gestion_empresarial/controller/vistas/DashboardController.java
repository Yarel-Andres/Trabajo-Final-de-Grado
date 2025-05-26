// Controlador para dashboard principal, calendario y elementos finalizados

package com.yarel.gestion_empresarial.controller.vistas;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.proyecto.ProyectoDTO;
import com.yarel.gestion_empresarial.dto.reunion.ReunionDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.dto.usuario.UsuarioDTO;
import com.yarel.gestion_empresarial.servicios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    // Inyección de servicios para manejo de datos
    private final EmpleadoService empleadoService;
    private final TareaService tareaService;
    private final ReunionService reunionService;
    private final UsuarioService usuarioService;
    private final ProyectoService proyectoService;

    @Autowired
    public DashboardController(EmpleadoService empleadoService, TareaService tareaService,
                               ReunionService reunionService, UsuarioService usuarioService,
                               ProyectoService proyectoService) {
        this.empleadoService = empleadoService;
        this.tareaService = tareaService;
        this.reunionService = reunionService;
        this.usuarioService = usuarioService;
        this.proyectoService = proyectoService;
    }

    // Página principal: Redirige al dashboard si está autenticado, sino muestra login
    @GetMapping("/")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() &&
                !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    // Dashboard principal: Muestra información del usuario autenticado y determina roles para navegación
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

    // Vista de elementos finalizados: Muestra tareas, reuniones y proyectos completados del empleado
    @GetMapping("/finalizados")
    public String verFinalizados(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        // Búsqueda del empleado: Obtiene ID para filtrar elementos completados
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

        // Obtención de elementos completados: Tareas, reuniones y proyectos finalizados
        List<TareaDTO> tareasCompletadas = tareaService.findCompletadasByEmpleadoId(empleadoId);
        List<ReunionDTO> reunionesCompletadas = reunionService.findCompletadasByParticipanteId(empleadoId);
        List<ProyectoDTO> proyectosCompletados = proyectoService.findCompletadosByEmpleadoId(empleadoId);

        // Mapeo de usuarios: Crea mapa para acceso rápido a nombres por ID
        List<UsuarioDTO> usuarios = usuarioService.findAll();
        List<ProyectoDTO> proyectos = proyectoService.findAll();

        Map<Long, String> usuariosMap = new HashMap<>();
        for (UsuarioDTO usuario : usuarios) {
            usuariosMap.put(usuario.getId(), usuario.getNombreCompleto());
        }

        model.addAttribute("usuariosMap", usuariosMap);

        // Enriquecimiento de tareas: Añade nombres de jefes y proyectos
        for (TareaDTO tarea : tareasCompletadas) {
            if (tarea.getJefeId() != null) {
                for (UsuarioDTO usuario : usuarios) {
                    if (usuario.getId().equals(tarea.getJefeId())) {
                        tarea.setJefeNombre(usuario.getNombreCompleto());
                        break;
                    }
                }
            }

            if (tarea.getProyectoId() != null) {
                for (ProyectoDTO proyecto : proyectos) {
                    if (proyecto.getId().equals(tarea.getProyectoId())) {
                        tarea.setNombreProyecto(proyecto.getNombre());
                        break;
                    }
                }
            }
        }

        // Enriquecimiento de reuniones: Añade nombres de organizadores
        for (ReunionDTO reunion : reunionesCompletadas) {
            if (reunion.getOrganizadorId() != null) {
                String nombreOrganizador = usuariosMap.get(reunion.getOrganizadorId());
                if (nombreOrganizador != null) {
                    reunion.setOrganizadorNombre(nombreOrganizador);
                }
            }
        }

        // Enriquecimiento de proyectos: Añade nombres de empleados participantes
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

    // Vista de calendario: Muestra todas las actividades del empleado (tareas, proyectos, reuniones) en formato calendario
    @GetMapping("/calendario")
    public String verCalendario(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        // Búsqueda del empleado: Obtiene ID del empleado autenticado
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
            return "calendario/calendario";
        }

        // Obtención de actividades: Todas las tareas, proyectos y reuniones del empleado
        List<TareaDTO> tareas = tareaService.findByEmpleadoId(empleadoId);
        List<ProyectoDTO> proyectosDelEmpleado = proyectoService.findByEmpleadoId(empleadoId);
        List<ReunionDTO> reuniones = reunionService.findByParticipanteId(empleadoId);

        // Mapeo de usuarios: Para enriquecimiento de datos
        List<UsuarioDTO> usuarios = usuarioService.findAll();
        Map<Long, String> usuariosMap = new HashMap<>();
        for (UsuarioDTO usuario : usuarios) {
            usuariosMap.put(usuario.getId(), usuario.getNombreCompleto());
        }

        List<ProyectoDTO> proyectos = proyectoService.findAll();

        // Enriquecimiento de tareas: Añade nombres de jefes y proyectos
        for (TareaDTO tarea : tareas) {
            if (tarea.getJefeId() != null) {
                tarea.setJefeNombre(usuariosMap.getOrDefault(tarea.getJefeId(), "No asignado"));
            }

            if (tarea.getProyectoId() != null) {
                for (ProyectoDTO proyecto : proyectos) {
                    if (proyecto.getId().equals(tarea.getProyectoId())) {
                        tarea.setNombreProyecto(proyecto.getNombre());
                        break;
                    }
                }
            }
        }

        // Enriquecimiento de reuniones: Añade nombres de organizadores
        for (ReunionDTO reunion : reuniones) {
            if (reunion.getOrganizadorId() != null) {
                reunion.setOrganizadorNombre(usuariosMap.getOrDefault(reunion.getOrganizadorId(), "No asignado"));
            }
        }

        // Datos para el calendario: Todas las actividades del empleado
        model.addAttribute("tareas", tareas);
        model.addAttribute("proyectos", proyectosDelEmpleado);
        model.addAttribute("reuniones", reuniones);
        model.addAttribute("username", nombreCompleto);

        return "calendario/calendario";
    }
}
