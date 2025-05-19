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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PresupuestoService {

    private static final Logger logger = LoggerFactory.getLogger(PresupuestoService.class);

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RRHHRepository rrhhRepository;

    @Autowired
    private PresupuestoMapper presupuestoMapper;

    public List<PresupuestoDTO> findAll() {
        return presupuestoMapper.toDtoList(presupuestoRepository.findAll());
    }

    public Optional<PresupuestoDTO> findById(Long id) {
        return presupuestoRepository.findById(id)
                .map(presupuestoMapper::toDto);
    }

    public List<PresupuestoDTO> findByProyectoId(Long proyectoId) {
        Optional<Proyecto> proyecto = proyectoRepository.findById(proyectoId);
        if (proyecto.isPresent()) {
            return presupuestoMapper.toDtoList(presupuestoRepository.findByProyecto(proyecto.get()));
        }
        return List.of();
    }

    @Transactional
    public PresupuestoDTO save(PresupuestoDTO presupuestoDTO, String nombreUsuario) {
        logger.info("Iniciando guardado de presupuesto para usuario: {}", nombreUsuario);

        // Buscar el usuario por nombre completo primero (que es lo que Spring Security usa como username)
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreCompleto(nombreUsuario);

        // Si no se encuentra, intentar por nombre de usuario
        if (usuarioOpt.isEmpty()) {
            logger.info("Usuario no encontrado por nombreCompleto, intentando por nombreUsuario: {}", nombreUsuario);
            usuarioOpt = usuarioRepository.findByNombreUsuario(nombreUsuario);
        }

        if (usuarioOpt.isEmpty()) {
            logger.error("Usuario no encontrado: {}", nombreUsuario);
            throw new IllegalArgumentException("El usuario no existe: " + nombreUsuario);
        }

        Usuario usuario = usuarioOpt.get();
        logger.info("Usuario encontrado: {} (Rol: {})", usuario.getNombreCompleto(), usuario.getRol());

        // Verificar si el usuario tiene rol RRHH
        if (usuario.getRol() != RolEnum.RRHH) {
            logger.error("El usuario no tiene rol RRHH: {} (Rol actual: {})", nombreUsuario, usuario.getRol());
            throw new IllegalArgumentException("El usuario no tiene permisos de RRHH");
        }

        // Buscar la entidad RRHH correspondiente
        Optional<RRHH> rrhhOpt = rrhhRepository.findById(usuario.getId());
        if (rrhhOpt.isEmpty()) {
            logger.error("No se encontró la entidad RRHH para el usuario: {}", nombreUsuario);
            throw new IllegalArgumentException("No se encontró la entidad RRHH para el usuario");
        }

        RRHH rrhh = rrhhOpt.get();
        logger.info("Entidad RRHH encontrada: {} (ID: {})", rrhh.getNombreCompleto(), rrhh.getId());

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

        // Establecer el proyecto
        if (presupuestoDTO.getProyectoId() != null) {
            logger.info("Buscando proyecto con ID: {}", presupuestoDTO.getProyectoId());
            Optional<Proyecto> proyectoOpt = proyectoRepository.findById(presupuestoDTO.getProyectoId());
            if (proyectoOpt.isPresent()) {
                presupuesto.setProyecto(proyectoOpt.get());
                logger.info("Proyecto encontrado y asignado: {}", proyectoOpt.get().getNombre());
            } else {
                logger.warn("No se encontró el proyecto con ID: {}", presupuestoDTO.getProyectoId());
            }
        }

        // Establecer el creador (RRHH)
        presupuesto.setCreador(rrhh);
        logger.info("Creador asignado: {} (ID: {})", rrhh.getNombreCompleto(), rrhh.getId());

        // Calcular el costo total si no está establecido
        if (presupuesto.getCostoTotal() == null && presupuesto.getTarifaHora() != null && presupuesto.getHorasEstimadas() != null) {
            Double costoTotal = presupuesto.getTarifaHora() * presupuesto.getHorasEstimadas();
            presupuesto.setCostoTotal(costoTotal);
            logger.info("Costo total calculado: {}", costoTotal);
        }

        // Guardar el presupuesto
        try {
            presupuesto = presupuestoRepository.save(presupuesto);
            logger.info("Presupuesto guardado correctamente con ID: {}", presupuesto.getId());
            return presupuestoMapper.toDto(presupuesto);
        } catch (Exception e) {
            logger.error("Error al guardar el presupuesto: ", e);
            throw e;
        }
    }

    public void deleteById(Long id) {
        presupuestoRepository.deleteById(id);
    }
}
