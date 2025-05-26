// Controlador para gestión completa de reuniones: creación, visualización, asignación y finalización

package com.yarel.gestion_empresarial.controller.vistas;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.reunion.ReunionDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ReunionViewController {

    // Inyección de servicios para manejo de datos de reuniones, empleados y usuarios
    private final EmpleadoService empleadoService;
    private final ReunionService reunionService;
    private final UsuarioService usuarioService;

    @Autowired
    public ReunionViewController(EmpleadoService empleadoService, ReunionService reunionService,
                                 UsuarioService usuarioService) {
        this.empleadoService = empleadoService;
        this.reunionService = reunionService;
        this.usuarioService = usuarioService;
    }

    // Formulario para crear reuniones: Solo jefes pueden crear reuniones, carga empleados disponibles
    @GetMapping("/reuniones/crear")
    public String crearReunionForm(Model model) {
        // Verificación de rol: Solo jefes pueden crear reuniones
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        // Carga de datos: Lista de empleados para seleccionar participantes
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        model.addAttribute("empleados", empleados);

        ReunionDTO reunion = new ReunionDTO();
        model.addAttribute("reunion", reunion);

        // Fecha por defecto: Establece fecha actual como valor inicial
        model.addAttribute("fechaActual", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return "reuniones/crear";
    }

    // Procesamiento de creación de reuniones: Valida datos, convierte fechas y programa reunión
    @PostMapping("/reuniones/crear")
    public String crearReunion(
            @ModelAttribute ReunionDTO reunion,
            @RequestParam(value = "participantesIds", required = false) List<Long> participantesIds,
            @RequestParam("fechaReunionDate") String fechaReunionDate,
            @RequestParam("horaReunion") int horaReunion,
            @RequestParam("minutosReunion") int minutosReunion,
            RedirectAttributes redirectAttributes) {
        // Verificación de rol: Solo jefes pueden crear reuniones
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        try {
            // Búsqueda del jefe: Obtiene datos del usuario autenticado
            String nombreCompleto = auth.getName();
            Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);

            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario");
                return "redirect:/reuniones/crear";
            }

            String nombreUsuario = usuarioOpt.get().getNombreUsuario();

            // Validación de datos: Verifica campos obligatorios
            if (reunion.getTitulo() == null || reunion.getTitulo().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El título de la reunión es obligatorio");
                return "redirect:/reuniones/crear";
            }

            if (reunion.getSala() == null || reunion.getSala().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La sala de la reunión es obligatoria");
                return "redirect:/reuniones/crear";
            }

            if (participantesIds == null || participantesIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar al menos un participante");
                return "redirect:/reuniones/crear";
            }

            // Conversión de fecha: Combina fecha, hora y minutos en LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fecha = LocalDate.parse(fechaReunionDate, formatter);
            LocalDateTime fechaHora = fecha.atTime(horaReunion, minutosReunion);
            reunion.setFechaHora(fechaHora);

            // Asignación de participantes: Convierte lista a Set para la reunión
            reunion.setParticipantesIds(new HashSet<>(participantesIds));

            // Creación de reunión: Guarda en base de datos
            ReunionDTO nuevaReunion = reunionService.saveReunionForJefe(reunion, nombreUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "Reunión programada correctamente");
            return "redirect:/reuniones/confirmacion";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al programar la reunión: " + e.getMessage());
            return "redirect:/reuniones/crear";
        }
    }

    // Vista de reuniones del usuario: Muestra reuniones no completadas donde participa el usuario
    @GetMapping("/reuniones/ver")
    public String verReuniones(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        // Búsqueda del usuario: Obtiene ID del usuario autenticado
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

        // Filtrado de reuniones: Solo reuniones no completadas donde participa el usuario
        List<ReunionDTO> reuniones = reunionService.findByParticipanteId(usuarioId);

        reuniones = reuniones.stream()
                .filter(reunion -> !reunion.isCompletada())
                .collect(Collectors.toList());

        // Mapeo de usuarios: Para acceso rápido a nombres por ID
        Map<Long, String> usuariosMap = new HashMap<>();
        for (UsuarioDTO usuario : usuarios) {
            usuariosMap.put(usuario.getId(), usuario.getNombreCompleto());
        }

        // Enriquecimiento de reuniones: Añade nombres de participantes y organizadores
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
            reunion.setParticipantesNombres(participantesNombres); // CORREGIDO: Establecer directamente en el DTO

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

    // Vista de reuniones asignadas por jefe: Muestra reuniones organizadas por el jefe
    @GetMapping("/reuniones/asignadas")
    public String verReunionesAsignadas(Model model) {
        // Verificación de rol: Solo jefes pueden ver reuniones asignadas
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
            return "reuniones/asignadas";
        }

        // Filtrado de reuniones: Solo reuniones no completadas organizadas por el jefe
        List<ReunionDTO> reuniones = reunionService.findByOrganizadorId(jefeId);

        reuniones = reuniones.stream()
                .filter(reunion -> !reunion.isCompletada())
                .collect(Collectors.toList());

        // Mapeo de usuarios: Para enriquecimiento de datos de participantes
        List<UsuarioDTO> todosUsuarios = usuarioService.findAll();

        Map<Long, String> usuariosMap = new HashMap<>();
        for (UsuarioDTO usuario : todosUsuarios) {
            usuariosMap.put(usuario.getId(), usuario.getNombreCompleto());
        }

        // Enriquecimiento de reuniones: Añade nombres de participantes
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
            reunion.setOrganizadorNombre(nombreCompleto);
            reunion.setParticipantesNombres(participantesNombres); // CORREGIDO: Establecer directamente en el DTO
        }

        model.addAttribute("reuniones", reuniones);

        return "reuniones/asignadas";
    }

    // Finalización de reuniones: Permite marcar reuniones como completadas
    @PostMapping("/reuniones/finalizar")
    public String finalizarReunion(@RequestParam("reunionId") Long reunionId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        try {
            // Búsqueda del usuario: Obtiene ID del usuario autenticado
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

            // Validación de reunión: Verifica que la reunión existe
            Optional<ReunionDTO> reunionOpt = reunionService.findById(reunionId);
            if (reunionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar la reunión");
                return "redirect:/reuniones/ver";
            }

            // Finalización: Marca la reunión como completada
            reunionService.finalizarReunion(reunionId);
            redirectAttributes.addFlashAttribute("mensaje", "Reunión finalizada correctamente");
            return "redirect:/reuniones/ver";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al finalizar la reunión: " + e.getMessage());
            return "redirect:/reuniones/ver";
        }
    }

    // Página de confirmación para reuniones: Muestra confirmación después de crear una reunión
    @GetMapping("/reuniones/confirmacion")
    public String confirmacionReunion(Model model) {
        // Verificación de rol: Solo jefes pueden ver confirmación de reuniones
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        return "reuniones/confirmacion";
    }
}
