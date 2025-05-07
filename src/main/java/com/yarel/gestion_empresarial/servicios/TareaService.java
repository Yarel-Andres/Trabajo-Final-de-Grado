package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.empleado.EmpleadoDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaMapper;
import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Tarea;
import com.yarel.gestion_empresarial.repositorios.EmpleadoRepository;
import com.yarel.gestion_empresarial.repositorios.JefeRepository;
import com.yarel.gestion_empresarial.repositorios.TareaRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final TareaRepository tareaRepository;
    private final TareaMapper tareaMapper;

    private final EmpleadoRepository empleadoRepository;
    private final JefeRepository jefeRepository;

    @Transactional(readOnly = true)
    public List<TareaDTO> findAll() {
        // obtiene una lista de entidades Empleado
        List<Tarea> empleados = tareaRepository.findAll();
        // Convierte esta lista en una lista de DTOs con empleadoMapper.toDtoList
        return tareaMapper.toDtoList(empleados);
    }

    @Transactional
    public TareaDTO save(TareaDTO tareaDTO) {
        Tarea tarea = tareaMapper.toEntity(tareaDTO);

        // Buscar el empleado y el jefe por ID (y lanzar excepción si no existen)
        Empleado empleado = empleadoRepository.findById(tareaDTO.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + tareaDTO.getEmpleadoId()));

        Jefe jefe = jefeRepository.findById(tareaDTO.getJefeId())
                .orElseThrow(() -> new RuntimeException("Jefe no encontrado con ID: " + tareaDTO.getJefeId()));

        // Asignar las referencias a la entidad
        tarea.setEmpleado(empleado);
        tarea.setJefe(jefe);

        tarea = tareaRepository.save(tarea);
        return tareaMapper.toDto(tarea);
    }
}
