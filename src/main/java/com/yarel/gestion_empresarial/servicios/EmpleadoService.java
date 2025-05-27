// Servicio para gestionar empleados, maneja operaciones CRUD y conversiones DTO
package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.empleado.EmpleadoMapper;
import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.repositorios.EmpleadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final EmpleadoMapper empleadoMapper;

    public EmpleadoService(EmpleadoRepository empleadoRepository, EmpleadoMapper empleadoMapper) {
        this.empleadoRepository = empleadoRepository;
        this.empleadoMapper = empleadoMapper;
    }

    // Obtiene todos los empleados para listas desplegables y asignaciones
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> findAll() {
        return empleadoMapper.toDtoList(empleadoRepository.findAll());
    }

    // Busca empleado por ID para validaciones en otros servicios
    @Transactional(readOnly = true)
    public Optional<EmpleadoDTO> findById(Long id) {
        return empleadoRepository.findById(id).map(empleadoMapper::toDto);
    }
}
