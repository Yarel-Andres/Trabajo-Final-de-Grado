// Controlador para gestión de proyectos y registros de tiempo: creación, visualización, finalización y reportes

package com.yarel.gestion_empresarial.controller.vistas;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.proyecto.ProyectoDTO;
import com.yarel.gestion_empresarial.dto.registroTiempo.RegistroTiempoDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProyectoViewController {

    // Inyección de servicios para manejo de datos de proyectos, empleados, usuarios, tareas, reuniones y registros de tiempo
    private final EmpleadoService empleadoService;
    private final ProyectoService proyectoService;
    private final UsuarioService usuarioService;
    private final TareaService tareaService;
    private final ReunionService reunionService;
    private final RegistroTiempoService registroTiempoService;

    @Autowired
    public ProyectoViewController(EmpleadoService empleadoService, ProyectoService proyectoService,
                                  UsuarioService usuarioService, TareaService tareaService,
                                  ReunionService reunionService, RegistroTiempoService registroTiempoService) {
        this.empleadoService = empleadoService;
        this.proyectoService = proyectoService;
        this.usuarioService = usuarioService;
        this.tareaService = tareaService;
        this.reunionService = reunionService;
        this.registroTiempoService = registroTiempoService;
    }

    // Formulario para crear proyectos: Solo jefes pueden crear proyectos, carga empleados disponibles
    @GetMapping("/proyectos/crear")
    public String crearProyectoForm(Model model) {
        // Verificación de rol: Solo jefes pueden crear proyectos
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        // Carga de datos: Lista de empleados para asignar al proyecto
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        model.addAttribute("empleados", empleados);

        // Valores por defecto: Fechas y estado inicial del proyecto
        ProyectoDTO proyecto = new ProyectoDTO();
        proyecto.setFechaInicio(LocalDate.now());
        proyecto.setFechaFinEstimada(LocalDate.now().plusMonths(3));
        proyecto.setEstado("PLANIFICACION");
        model.addAttribute("proyecto", proyecto);

        return "proyectos/crear";
    }

    // Creación de proyectos: Valida datos y asigna empleados al proyecto
    @PostMapping("/proyectos/crear")
    public String crearProyecto(@ModelAttribute ProyectoDTO proyecto,
                                @RequestParam(value = "empleadosIds", required = false) List<Long> empleadosIds,
                                RedirectAttributes redirectAttributes) {
        // Verificación de rol: Solo jefes pueden crear proyectos
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
                return "redirect:/proyectos/crear";
            }

            String nombreUsuario = usuarioOpt.get().getNombreUsuario();

            // Validación de datos: Verifica campos obligatorios del proyecto
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

            // Asignación de empleados: Convierte lista a Set para el proyecto
            if (empleadosIds != null && !empleadosIds.isEmpty()) {
                proyecto.setEmpleadosIds(new HashSet<>(empleadosIds));
            } else {
                proyecto.setEmpleadosIds(new HashSet<>());
            }

            // Creación de proyecto: Guarda en base de datos
            ProyectoDTO nuevoProyecto = proyectoService.saveProyectoForJefe(proyecto, nombreUsuario);
            redirectAttributes.addFlashAttribute("mensaje", "Proyecto creado correctamente");
            return "redirect:/proyectos/confirmacion";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al crear el proyecto: " + e.getMessage());
            return "redirect:/proyectos/crear";
        }
    }

    // Vista de proyectos: Muestra proyectos según el rol del usuario (jefe ve sus proyectos, empleado ve asignados)
    @GetMapping("/proyectos/ver")
    public String verProyectos(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();
        boolean isJefe = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"));

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
            return "proyectos/ver";
        }

        List<ProyectoDTO> proyectos;

        // Filtrado por rol: Diferentes vistas según el tipo de usuario
        if (isJefe) {
            proyectos = proyectoService.findByJefeId(usuarioId);
        } else {
            // Empleados: Solo proyectos asignados y no completados
            proyectos = proyectoService.findByEmpleadoId(usuarioId);
            proyectos = proyectos.stream()
                    .filter(proyecto -> !proyecto.isCompletado())
                    .collect(Collectors.toList());
        }

        // Enriquecimiento de datos: Añade nombres de empleados a los proyectos
        List<EmpleadoDTO> empleados = empleadoService.findAll();

        Map<Long, String> empleadosMap = new HashMap<>();
        for (EmpleadoDTO empleado : empleados) {
            empleadosMap.put(empleado.getId(), empleado.getNombreCompleto());
        }

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

    // Finalización de proyectos: Permite marcar proyectos como completados
    @PostMapping("/proyectos/finalizar")
    public String finalizarProyecto(@RequestParam("proyectoId") Long proyectoId, RedirectAttributes redirectAttributes) {
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
                return "redirect:/proyectos/ver";
            }

            // Validación de proyecto: Verifica que el proyecto existe
            Optional<ProyectoDTO> proyectoOpt = proyectoService.findById(proyectoId);
            if (proyectoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el proyecto");
                return "redirect:/proyectos/ver";
            }

            // Finalización: Marca el proyecto como completado
            proyectoService.finalizarProyecto(proyectoId);
            redirectAttributes.addFlashAttribute("mensaje", "Proyecto finalizado correctamente");
            return "redirect:/proyectos/ver";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al finalizar el proyecto: " + e.getMessage());
            return "redirect:/proyectos/ver";
        }
    }

    // Formulario para registrar tiempo: Permite registrar tiempo trabajado en tareas, proyectos o reuniones
    @GetMapping("/registros-tiempo/crear")
    public String crearRegistroTiempoForm(Model model,
                                          @RequestParam(required = false) Long tareaId,
                                          @RequestParam(required = false) Long proyectoId,
                                          @RequestParam(required = false) Long reunionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        // Búsqueda del usuario: Obtiene datos del usuario autenticado
        Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("error", "No se pudo encontrar el usuario");
            return "registros-tiempo/crear";
        }

        // Carga de datos según rol: Diferentes opciones para empleados y jefes
        List<TareaDTO> tareas = new ArrayList<>();
        List<ProyectoDTO> proyectos = new ArrayList<>();
        List<ReunionDTO> reuniones = new ArrayList<>();

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLEADO"))) {
            // Empleados: Solo sus tareas, proyectos y reuniones asignadas
            Long usuarioId = usuarioOpt.get().getId();
            tareas = tareaService.findByEmpleadoId(usuarioId);
            proyectos = proyectoService.findByEmpleadoId(usuarioId);
            reuniones = reunionService.findByParticipanteId(usuarioId);
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            // Jefes: Todos los elementos que han creado
            Long usuarioId = usuarioOpt.get().getId();
            tareas = tareaService.findAll();
            proyectos = proyectoService.findByJefeId(usuarioId);
            reuniones = reunionService.findAll();
        }

        model.addAttribute("tareas", tareas);
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("reuniones", reuniones);

        // Preselección de actividad: Si se pasa un ID específico, preselecciona esa actividad
        RegistroTiempoDTO registroTiempo = new RegistroTiempoDTO();

        String tipoActividad = null;

        if (tareaId != null) {
            registroTiempo.setTareaId(tareaId);
            tipoActividad = "tarea";
        } else if (proyectoId != null) {
            registroTiempo.setProyectoId(proyectoId);
            tipoActividad = "proyecto";
        } else if (reunionId != null) {
            registroTiempo.setReunionId(reunionId);
            tipoActividad = "reunion";
        }

        if (tipoActividad != null) {
            model.addAttribute("tipoActividadSeleccionada", tipoActividad);
            model.addAttribute("mostrarSoloUnaOpcion", true);
        } else {
            model.addAttribute("mostrarSoloUnaOpcion", false);
        }

        model.addAttribute("registroTiempo", registroTiempo);

        // Fecha por defecto: Establece fecha actual como valor inicial
        model.addAttribute("fechaActual", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return "registros-tiempo/crear";
    }

    // Procesamiento de registro de tiempo: Calcula horas trabajadas y guarda el registro
    @PostMapping("/registros-tiempo/crear")
    public String crearRegistroTiempo(@ModelAttribute RegistroTiempoDTO registroTiempo, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        try {
            // Búsqueda del usuario: Obtiene datos del usuario autenticado
            Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);

            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario");
                return "redirect:/registros-tiempo/crear";
            }

            String nombreUsuario = usuarioOpt.get().getNombreUsuario();

            try {
                // Conversión de fechas: Combina fechas y horas en LocalDateTime
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                LocalDate fechaInicio = LocalDate.parse(registroTiempo.getFechaInicioTemp(), formatter);
                LocalDateTime horaInicioDateTime = fechaInicio.atTime(
                        registroTiempo.getHoraInicioTemp(),
                        registroTiempo.getMinutosInicioTemp()
                );
                registroTiempo.setHoraInicio(horaInicioDateTime);

                LocalDate fechaFin = LocalDate.parse(registroTiempo.getFechaFinTemp(), formatter);
                LocalDateTime horaFinDateTime = fechaFin.atTime(
                        registroTiempo.getHoraFinTemp(),
                        registroTiempo.getMinutosFinTemp()
                );
                registroTiempo.setHoraFin(horaFinDateTime);

                registroTiempo.setFechaRegistro(fechaInicio);

                // Cálculo de horas: Calcula tiempo trabajado entre inicio y fin
                double horasTrabajadas = calcularHorasTrabajadas(horaInicioDateTime, horaFinDateTime);
                registroTiempo.setHorasTrabajadas(horasTrabajadas);

                // Guardado del registro: Almacena en base de datos
                RegistroTiempoDTO nuevoRegistro = registroTiempoService.saveForUsuario(registroTiempo, nombreUsuario);
                redirectAttributes.addFlashAttribute("mensaje", "Tiempo registrado correctamente");
                return "redirect:/registros-tiempo/ver";
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

    // Cálculo de horas trabajadas: Convierte duración entre dos fechas a horas decimales
    private double calcularHorasTrabajadas(LocalDateTime inicio, LocalDateTime fin) {
        long segundos = java.time.Duration.between(inicio, fin).getSeconds();
        return segundos / 3600.0;
    }

    // Vista de registros de tiempo: Muestra registros de tiempo del usuario autenticado
    @GetMapping("/registros-tiempo/ver")
    public String verRegistrosTiempo(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreCompleto = auth.getName();

        // Búsqueda del usuario: Obtiene ID del usuario autenticado
        Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("error", "No se pudo encontrar el usuario");
            return "registros-tiempo/ver";
        }

        // Obtención de registros: Todos los registros de tiempo del usuario
        Long usuarioId = usuarioOpt.get().getId();
        List<RegistroTiempoDTO> registros = registroTiempoService.findByUsuarioId(usuarioId);
        model.addAttribute("registros", registros);

        return "registros-tiempo/ver";
    }

    // Vista principal de informes: Permite seleccionar empleado o proyecto para ver informes de tiempo
    @GetMapping("/registros-tiempo/informes")
    public String verInformesRegistrosTiempo(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verificación de rol: Solo jefes y RRHH pueden ver informes
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE")) &&
                !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RRHH"))) {
            return "redirect:/dashboard";
        }

        // Carga de datos: Lista de empleados y proyectos para los selectores
        List<EmpleadoDTO> empleados = empleadoService.findAll();
        List<ProyectoDTO> proyectos = proyectoService.findAll();

        model.addAttribute("empleados", empleados);
        model.addAttribute("proyectos", proyectos);

        return "registros-tiempo/informes";
    }

    // Vista de informes por empleado: Muestra registros de tiempo de un empleado específico (solo jefes y RRHH)
    @GetMapping("/registros-tiempo/informes/empleado/{empleadoId}")
    public String verInformesRegistrosTiempoPorEmpleado(@PathVariable Long empleadoId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verificación de rol: Solo jefes y RRHH pueden ver informes
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE")) &&
                !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RRHH"))) {
            return "redirect:/dashboard";
        }

        // Búsqueda del empleado: Obtiene información del empleado para el informe
        Optional<EmpleadoDTO> empleadoOpt = empleadoService.findById(empleadoId);
        if (empleadoOpt.isEmpty()) {
            model.addAttribute("error", "Empleado no encontrado");
            return "registros-tiempo/informes";
        }

        model.addAttribute("empleado", empleadoOpt.get());

        // Obtención de registros: Todos los registros de tiempo del empleado
        List<RegistroTiempoDTO> registros = registroTiempoService.findByUsuarioId(empleadoId);

        // Filtrado adicional para jefes: Solo registros de tareas asignadas por el jefe
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            String nombreCompleto = auth.getName();
            Optional<UsuarioDTO> usuarioOpt = usuarioService.findByNombreCompleto(nombreCompleto);

            if (usuarioOpt.isPresent()) {
                Long jefeId = usuarioOpt.get().getId();

                List<RegistroTiempoDTO> registrosJefe = registroTiempoService.findByJefeId(jefeId);

                List<RegistroTiempoDTO> registrosJefeEmpleado = registrosJefe.stream()
                        .filter(r -> r.getUsuarioId() != null && r.getUsuarioId().equals(empleadoId))
                        .collect(Collectors.toList());

                // Combinación de registros: Evita duplicados
                for (RegistroTiempoDTO registro : registrosJefeEmpleado) {
                    if (registros.stream().noneMatch(r -> r.getId().equals(registro.getId()))) {
                        registros.add(registro);
                    }
                }
            }
        }

        model.addAttribute("registros", registros);

        return "registros-tiempo/informes-empleado";
    }

    // Vista de informes por proyecto: Muestra registros de tiempo de un proyecto específico (solo jefes y RRHH)
    @GetMapping("/registros-tiempo/informes/proyecto/{proyectoId}")
    public String verInformesRegistrosTiempoPorProyecto(@PathVariable Long proyectoId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verificación de rol: Solo jefes y RRHH pueden ver informes
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE")) &&
                !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RRHH"))) {
            return "redirect:/dashboard";
        }

        // Búsqueda del proyecto: Obtiene información del proyecto para el informe
        Optional<ProyectoDTO> proyectoOpt = proyectoService.findById(proyectoId);
        if (proyectoOpt.isEmpty()) {
            model.addAttribute("error", "Proyecto no encontrado");
            return "registros-tiempo/informes";
        }

        model.addAttribute("proyecto", proyectoOpt.get());

        try {
            // Obtención de registros: Todos los registros de tiempo del proyecto
            List<RegistroTiempoDTO> registros = registroTiempoService.findByProyectoId(proyectoId);

            // Mapeo de usuarios: Para mostrar nombres en lugar de IDs
            List<UsuarioDTO> todosLosUsuarios = usuarioService.findAll();
            Map<Long, String> mapaUsuarios = new HashMap<>();
            for (UsuarioDTO usuario : todosLosUsuarios) {
                mapaUsuarios.put(usuario.getId(), usuario.getNombreCompleto());
            }

            // Enriquecimiento de registros: Añade nombres de usuarios
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

                // Debug: Información para depuración
                System.out.println("Registro ID: " + registro.getId() +
                        ", Usuario ID: " + registro.getUsuarioId() +
                        ", Nombre asignado: " + registro.getUsuarioNombre());
            }

            model.addAttribute("registros", registros);

            // Información de depuración: Para troubleshooting
            model.addAttribute("totalRegistros", registros.size());
            model.addAttribute("totalUsuarios", todosLosUsuarios.size());

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los registros de tiempo: " + e.getMessage());
        }

        return "registros-tiempo/informes-proyecto";
    }

    // Página de confirmación para proyectos: Muestra confirmación después de crear un proyecto
    @GetMapping("/proyectos/confirmacion")
    public String confirmacionProyecto(Model model) {
        // Verificación de rol: Solo jefes pueden ver confirmación de proyectos
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JEFE"))) {
            return "redirect:/dashboard";
        }

        return "proyectos/confirmacion";
    }
}
