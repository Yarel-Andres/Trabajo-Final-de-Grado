package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.empleado.EmpleadoMapper;
import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.repositorios.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Indica que esta clase es un bean de Spring que actúa como servicio
@Service
public class EmpleadoService {

    // Interactúa con la base de datos (mediante CRUD) para manejar los datos de empleados
    private final EmpleadoRepository empleadoRepository;
    // Convierte entre Empleado (entidad) y EmpleadoDTO (DTO)
    private final EmpleadoMapper empleadoMapper;

    // Inyecta las dependencias automáticamente en el constructor. Con esto los asignamos como parámetro
    // a este constructor
    @Autowired
    public EmpleadoService(EmpleadoRepository empleadoRepository, EmpleadoMapper empleadoMapper) {
        // Los valores de los parámetros se asignan a las variables de instancia para que estén disponibles en los
        // métodos de la clase EmpleadoService
        this.empleadoRepository = empleadoRepository;
        this.empleadoMapper = empleadoMapper;
    }


    // Recupera todos los empleados de la base de datos y los convierte a EmpleadoDTO usando el mapper
    // Marca el metodo como transaccional en modo solo lectura, lo cual mejora el rendimiento y garantiza que
    // no se realicen cambios en la base de datos
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> findAll() {
                                // obtiene una lista de entidades Empleado
        List<Empleado> empleados = empleadoRepository.findAll();
        // Convierte esta lista en una lista de DTOs con empleadoMapper.toDtoList
        return empleadoMapper.toDtoList(empleados);
    }


    // Busca un empleado por su ID y lo convierte a un DTO, si existe
    @Transactional(readOnly = true)
    public Optional<EmpleadoDTO> findById(Long id) {
        // Si encuentra un empleado, usa empleadoMapper.toDto para convertirlo a EmpleadoDTO
        return empleadoRepository.findById(id)
                .map(empleadoMapper::toDto);
    }


    // Guarda un nuevo empleado en la base de datos. Convierte un DTO en entidad antes de guardarlo y luego de guardar,
    // lo convierte nuevamente en DTO para devolverlo
    @Transactional
    public EmpleadoDTO save(EmpleadoDTO empleadoDTO) {
        Empleado empleado = empleadoMapper.toEntity(empleadoDTO);
        empleado = empleadoRepository.save(empleado);
        return empleadoMapper.toDto(empleado);
    }


    // Actualiza un empleado existente
    @Transactional
    public Optional<EmpleadoDTO> update(Long id, EmpleadoDTO empleadoDTO) {
        // Verifica si el empleado existe
        if (!empleadoRepository.existsById(id)) {
            // Si no existe devuelve el Optional vacio
            return Optional.empty();
        }

        // Si existe, actualiza el ID en el DTO y lo convierte en una entidad
        // Esto asegura que, al convertir el DTO en entidad, la operación afecte al empleado adecuado
        empleadoDTO.setId(id);
        Empleado empleado = empleadoMapper.toEntity(empleadoDTO);
        // Guarda la entidad actualizada y la convierte nuevamente en DTO para devolverla
        empleado = empleadoRepository.save(empleado);
        return Optional.of(empleadoMapper.toDto(empleado));
    }


    // Elimina un empleado por su ID
    @Transactional
    public boolean deleteById(Long id) {
        // Verifica si el empleado existe
        if (!empleadoRepository.existsById(id)) {
            return false;
        }
        // Si existe, lo elimina
        empleadoRepository.deleteById(id);
        // Retorna true si la operación fue exitosa, o false si no existía el empleado
        return true;
    }
}