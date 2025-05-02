package com.yarel.gestion_empresarial.config;

import com.yarel.gestion_empresarial.entidades.*;
import com.yarel.gestion_empresarial.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

// Marca esta clase para que Spring la registre como un componente del contexto de la aplicación
@Component
// Es parte de Lombok y genera un constructor para todas las propiedades declaradas como final
@RequiredArgsConstructor
public class InitData {

    // Declara los repositorios
    private final EmpleadoRepository empleadoRepository;
    private final JefeRepository jefeRepository;
    private final RRHHRepository rrhhRepository;
    private final ProyectoRepository proyectoRepository;
    private final TareaRepository tareaRepository;
    private final ReunionRepository reunionRepository;
    private final UsuarioRepository usuarioRepository;

    // Mapas para almacenar las referencias a los usuarios creados
    private Map<String, Jefe> jefesMap = new HashMap<>();
    private Map<String, Empleado> empleadosMap = new HashMap<>();

    @Transactional
    @EventListener
    // ApplicationReadyEvent hace que se cargue después de mostrar
    // el mensaje de "Started Application in xxx seconds"
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Solo inicializa datos si no hay usuarios en la base de datos
        if (usuarioRepository.count() == 0) {
            initUsuarios();
            initProyectos();
            initTareas();
            initReuniones();
        }
    }

    private void initUsuarios() {

        // Crear jefes
        Jefe jefe1 = new Jefe();
        jefe1.setNombreUsuario("jefe1");
        jefe1.setContraseña("password");
        jefe1.setCorreo("jefe1@empresa.com");
        jefe1.setNombreCompleto("Antonio García");
        jefe1.setActivo(true);
        jefe1.setRol(Usuario.RolEnum.JEFE);
        jefe1.setFechaNombramiento(LocalDate.of(2022, 1, 15));
        jefe1.setNivelResponsabilidad("Senior");

        Jefe jefe2 = new Jefe();
        jefe2.setNombreUsuario("jefe2");
        jefe2.setContraseña("password"); // Sin encriptación
        jefe2.setCorreo("jefe2@empresa.com");
        jefe2.setNombreCompleto("María López");
        jefe2.setActivo(true);
        jefe2.setRol(Usuario.RolEnum.JEFE);
        jefe2.setFechaNombramiento(LocalDate.of(2023, 3, 10));
        jefe2.setNivelResponsabilidad("Medio");

        // Guardar los jefes y almacenarlos en el mapa
        jefe1 = jefeRepository.save(jefe1);
        jefe2 = jefeRepository.save(jefe2);

        jefesMap.put("jefe1", jefe1);
        jefesMap.put("jefe2", jefe2);


        // Crear empleados
        Empleado empleado1 = new Empleado();
        empleado1.setNombreUsuario("empleado1");
        empleado1.setContraseña("password");
        empleado1.setCorreo("empleado1@empresa.com");
        empleado1.setNombreCompleto("Carlos Martínez");
        empleado1.setActivo(true);
        empleado1.setRol(Usuario.RolEnum.EMPLEADO);
        empleado1.setFechaContratacion(LocalDate.of(2023, 2, 20));
        empleado1.setPuesto("Desarrollador");
        empleado1.setTelefono("666111222");
        empleado1.setSalario(32000.0);
        empleado1.setTareas(new HashSet<>());

        Empleado empleado2 = new Empleado();
        empleado2.setNombreUsuario("empleado2");
        empleado2.setContraseña("password"); // Sin encriptación
        empleado2.setCorreo("empleado2@empresa.com");
        empleado2.setNombreCompleto("Laura Sánchez");
        empleado2.setActivo(true);
        empleado2.setRol(Usuario.RolEnum.EMPLEADO);
        empleado2.setFechaContratacion(LocalDate.of(2022, 11, 5));
        empleado2.setPuesto("Diseñador UX");
        empleado2.setTelefono("666333444");
        empleado2.setSalario(29000.0);
        empleado2.setTareas(new HashSet<>());

        Empleado empleado3 = new Empleado();
        empleado3.setNombreUsuario("empleado3");
        empleado3.setContraseña("password"); // Sin encriptación
        empleado3.setCorreo("empleado3@empresa.com");
        empleado3.setNombreCompleto("David Fernández");
        empleado3.setActivo(true);
        empleado3.setRol(Usuario.RolEnum.EMPLEADO);
        empleado3.setFechaContratacion(LocalDate.of(2023, 6, 15));
        empleado3.setPuesto("Analista");
        empleado3.setTelefono("666555666");
        empleado3.setSalario(27500.0);
        empleado3.setTareas(new HashSet<>());

        // Guardar los empleados y almacenarlos en el mapa
        empleado1 = empleadoRepository.save(empleado1);
        empleado2 = empleadoRepository.save(empleado2);
        empleado3 = empleadoRepository.save(empleado3);

        empleadosMap.put("empleado1", empleado1);
        empleadosMap.put("empleado2", empleado2);
        empleadosMap.put("empleado3", empleado3);


        // Crear personal de RRHH
        RRHH rrhh1 = new RRHH();
        rrhh1.setNombreUsuario("rrhh1");
        rrhh1.setContraseña("password");
        rrhh1.setCorreo("rrhh1@empresa.com");
        rrhh1.setNombreCompleto("Elena Ruiz");
        rrhh1.setActivo(true);
        rrhh1.setRol(Usuario.RolEnum.RRHH);
        rrhh1.setAreaEspecializacion("Contratación");
        rrhh1.setCertificaciones("CIPD Level 5");
        rrhh1.setFechaIncorporacionRRHH(LocalDate.of(2022, 5, 12));

        rrhhRepository.save(rrhh1);
    }

    private void initProyectos() {
        Jefe jefe1 = jefesMap.get("jefe1");
        Jefe jefe2 = jefesMap.get("jefe2");

        Proyecto proyecto1 = new Proyecto();
        proyecto1.setNombre("Sistema de Gestión");
        proyecto1.setDescripcion("Sistema para gestión empresarial");
        proyecto1.setJefe(jefe1);
        proyecto1.setFechaInicio(LocalDate.of(2023, 1, 20));
        proyecto1.setFechaFinEstimada(LocalDate.of(2023, 12, 31));
        proyecto1.setEstado("EN_PROGRESO");

        Proyecto proyecto2 = new Proyecto();
        proyecto2.setNombre("App Móvil");
        proyecto2.setDescripcion("Aplicación móvil para clientes");
        proyecto2.setJefe(jefe2);
        proyecto2.setFechaInicio(LocalDate.of(2023, 5, 15));
        proyecto2.setFechaFinEstimada(LocalDate.of(2023, 11, 30));
        proyecto2.setEstado("EN_PROGRESO");

        proyectoRepository.saveAll(Arrays.asList(proyecto1, proyecto2));
    }

    private void initTareas() {
        Empleado empleado1 = empleadosMap.get("empleado1");
        Empleado empleado2 = empleadosMap.get("empleado2");
        Empleado empleado3 = empleadosMap.get("empleado3");

        Jefe jefe1 = jefesMap.get("jefe1");
        Jefe jefe2 = jefesMap.get("jefe2");

        Tarea tarea1 = new Tarea();
        tarea1.setTitulo("Desarrollar API");
        tarea1.setDescripcion("Implementar endpoints de la API REST");
        tarea1.setEmpleado(empleado1);
        tarea1.setJefe(jefe1);
        tarea1.setFechaCreacion(LocalDateTime.now().minusDays(5));
        tarea1.setFechaVencimiento(LocalDate.now().plusDays(10));
        tarea1.setCompletada(false);
        tarea1.setPrioridad("MEDIA");

        Tarea tarea2 = new Tarea();
        tarea2.setTitulo("Diseñar interfaz");
        tarea2.setDescripcion("Diseñar la interfaz principal del sistema");
        tarea2.setEmpleado(empleado2);
        tarea2.setJefe(jefe1);
        tarea2.setFechaCreacion(LocalDateTime.now().minusDays(7));
        tarea2.setFechaVencimiento(LocalDate.now().plusDays(5));
        tarea2.setCompletada(false);
        tarea2.setPrioridad("ALTA");

        Tarea tarea3 = new Tarea();
        tarea3.setTitulo("Análisis de requisitos");
        tarea3.setDescripcion("Analizar requisitos del cliente");
        tarea3.setEmpleado(empleado3);
        tarea3.setJefe(jefe2);
        tarea3.setFechaCreacion(LocalDateTime.now().minusDays(10));
        tarea3.setFechaVencimiento(LocalDate.now().minusDays(3));
        tarea3.setCompletada(true);
        tarea3.setFechaCompletada(LocalDateTime.now().minusDays(1));
        tarea3.setPrioridad("BAJA");

        tareaRepository.saveAll(Arrays.asList(tarea1, tarea2, tarea3));
    }

    private void initReuniones() {
        Jefe jefe1 = jefesMap.get("jefe1");
        Jefe jefe2 = jefesMap.get("jefe2");

        Empleado empleado1 = empleadosMap.get("empleado1");
        Empleado empleado2 = empleadosMap.get("empleado2");
        Empleado empleado3 = empleadosMap.get("empleado3");

        // Crear reuniones
        Reunion reunion1 = new Reunion();
        reunion1.setTitulo("Kickoff proyecto");
        reunion1.setDescripcion("Reunión inicial del proyecto");
        reunion1.setFechaHora(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0));
        reunion1.setSala("Sala A");
        reunion1.setOrganizador(jefe1);
        reunion1.setParticipantes(new HashSet<>(Arrays.asList(jefe1, empleado1, empleado2)));

        Reunion reunion2 = new Reunion();
        reunion2.setTitulo("Revisión de avances");
        reunion2.setDescripcion("Revisión semanal de avances");
        reunion2.setFechaHora(LocalDateTime.now().plusDays(7).withHour(11).withMinute(0));
        reunion2.setSala("Sala B");
        reunion2.setOrganizador(jefe2);
        reunion2.setParticipantes(new HashSet<>(Arrays.asList(jefe2, empleado3)));

        reunionRepository.saveAll(Arrays.asList(reunion1, reunion2));
    }
}