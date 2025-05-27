package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.presupuesto.PresupuestoDTO;
import com.yarel.gestion_empresarial.dto.presupuesto.PresupuestoMapper;
import com.yarel.gestion_empresarial.entidades.Presupuesto;
import com.yarel.gestion_empresarial.entidades.Proyecto;
import com.yarel.gestion_empresarial.entidades.RRHH;
import com.yarel.gestion_empresarial.entidades.Usuario;
import com.yarel.gestion_empresarial.entidades.Usuario.RolEnum;
import com.yarel.gestion_empresarial.repositorios.PresupuestoRepository;
import com.yarel.gestion_empresarial.repositorios.ProyectoRepository;
import com.yarel.gestion_empresarial.repositorios.RRHHRepository;
import com.yarel.gestion_empresarial.repositorios.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RRHHRepository rrhhRepository;
    private final PresupuestoMapper presupuestoMapper;

    // Constructor para inyección de dependencias
    public PresupuestoService(PresupuestoRepository presupuestoRepository,
                              ProyectoRepository proyectoRepository,
                              UsuarioRepository usuarioRepository,
                              RRHHRepository rrhhRepository,
                              PresupuestoMapper presupuestoMapper) {
        this.presupuestoRepository = presupuestoRepository;
        this.proyectoRepository = proyectoRepository;
        this.usuarioRepository = usuarioRepository;
        this.rrhhRepository = rrhhRepository;
        this.presupuestoMapper = presupuestoMapper;
    }

    // Obtiene todos los presupuestos del sistema
    public List<PresupuestoDTO> findAll() {
        return presupuestoMapper.toDtoList(presupuestoRepository.findAll());
    }

    // Busca un presupuesto por su ID
    public Optional<PresupuestoDTO> findById(Long id) {
        return presupuestoRepository.findById(id)
                .map(presupuestoMapper::toDto);
    }

    // Obtiene presupuestos asociados a un proyecto específico
    public List<PresupuestoDTO> findByProyectoId(Long proyectoId) {
        Optional<Proyecto> proyecto = proyectoRepository.findById(proyectoId);
        if (proyecto.isPresent()) {
            return presupuestoMapper.toDtoList(presupuestoRepository.findByProyecto(proyecto.get()));
        }
        return List.of();
    }

    // Crear un nuevo presupuesto
    @Transactional
    public PresupuestoDTO save(PresupuestoDTO presupuestoDTO, String nombreUsuario) {

        // Buscar el usuario por nombre completo primero (que es lo que Spring Security usa como username)
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreCompleto(nombreUsuario);

        // Si no se encuentra, intentar por nombre de usuario
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByNombreUsuario(nombreUsuario);
        }

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe: " + nombreUsuario);
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar si el usuario tiene rol RRHH
        if (usuario.getRol() != RolEnum.RRHH) {
            throw new IllegalArgumentException("El usuario no tiene permisos de RRHH");
        }

        // Buscar la entidad RRHH correspondiente
        Optional<RRHH> rrhhOpt = rrhhRepository.findById(usuario.getId());
        if (rrhhOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró la entidad RRHH para el usuario");
        }

        RRHH rrhh = rrhhOpt.get();

        // Crear una nueva entidad Presupuesto
        Presupuesto presupuesto = new Presupuesto();

        // Establecer los campos básicos
        presupuesto.setNombreCliente(presupuestoDTO.getNombreCliente());
        presupuesto.setDescripcion(presupuestoDTO.getDescripcion());
        presupuesto.setTarifaHora(presupuestoDTO.getTarifaHora());
        presupuesto.setHorasEstimadas(presupuestoDTO.getHorasEstimadas());
        presupuesto.setCostoTotal(presupuestoDTO.getCostoTotal());
        presupuesto.setEstado(presupuestoDTO.getEstado() != null ? presupuestoDTO.getEstado() : "BORRADOR");
        presupuesto.setFechaCreacion(LocalDate.now());

        // Establecer el proyecto si se especifica
        if (presupuestoDTO.getProyectoId() != null) {
            Optional<Proyecto> proyectoOpt = proyectoRepository.findById(presupuestoDTO.getProyectoId());
            if (proyectoOpt.isPresent()) {
                presupuesto.setProyecto(proyectoOpt.get());
            } else {
            }
        }

        // Establecer el creador (RRHH)
        presupuesto.setCreador(rrhh);

        // Calcular el costo total si no está establecido
        if (presupuesto.getCostoTotal() == null && presupuesto.getTarifaHora() != null && presupuesto.getHorasEstimadas() != null) {
            Double costoTotal = presupuesto.getTarifaHora() * presupuesto.getHorasEstimadas();
            presupuesto.setCostoTotal(costoTotal);
        }

        // Guardar el presupuesto
        try {
            presupuesto = presupuestoRepository.save(presupuesto);
            return presupuestoMapper.toDto(presupuesto);
        } catch (Exception e) {
            throw e;
        }
    }

    // Actualiza un presupuesto existente
    @Transactional
    public PresupuestoDTO update(PresupuestoDTO presupuestoDTO, String nombreUsuario) {
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoDTO.getId())
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado con ID: " + presupuestoDTO.getId()));

        // Actualizar los campos editables
        presupuesto.setNombreCliente(presupuestoDTO.getNombreCliente());
        presupuesto.setDescripcion(presupuestoDTO.getDescripcion());
        presupuesto.setTarifaHora(presupuestoDTO.getTarifaHora());
        presupuesto.setHorasEstimadas(presupuestoDTO.getHorasEstimadas());
        presupuesto.setCostoTotal(presupuestoDTO.getCostoTotal());

        // Guardar el presupuesto actualizado
        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuesto);

        return presupuestoMapper.toDto(presupuestoActualizado);
    }

    // Cambia el estado de un presupuesto (BORRADOR/ENVIADO)
    @Transactional
    public PresupuestoDTO cambiarEstado(Long id, String nuevoEstado, String nombreUsuario) {
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado con ID: " + id));

        // Cambiar el estado
        presupuesto.setEstado(nuevoEstado);

        // Guardar el presupuesto actualizado
        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuesto);

        // Registrar el cambio en el historial si existe esa funcionalidad
        // historialService.registrarCambio(presupuesto.getId(), nombreUsuario, "CAMBIO_ESTADO",
        //     "Estado cambiado a " + nuevoEstado);

        return presupuestoMapper.toDto(presupuestoActualizado);
    }

    // Elimina un presupuesto por su ID
    public void deleteById(Long id) {
        presupuestoRepository.deleteById(id);
    }
}
