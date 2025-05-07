package com.yarel.gestion_empresarial.servicios;

import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.dto.tarea.TareaMapper;
import com.yarel.gestion_empresarial.entidades.Empleado;
import com.yarel.gestion_empresarial.entidades.Jefe;
import com.yarel.gestion_empresarial.entidades.Tarea;
import com.yarel.gestion_empresarial.repositorios.EmpleadoRepository;
import com.yarel.gestion_empresarial.repositorios.JefeRepository;
import com.yarel.gestion_empresarial.repositorios.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private JefeRepository jefeRepository;

    @Mock
    private TareaMapper tareaMapper;

    @InjectMocks
    private TareaService tareaService;

    private TareaDTO tareaDTO;
    private Tarea tarea;
    private Empleado empleado;
    private Jefe jefe;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        empleado = new Empleado();
        empleado.setId(1L);
        empleado.setNombreUsuario("empleado1");

        jefe = new Jefe();
        jefe.setId(2L);
        jefe.setNombreUsuario("jefe1");

        tareaDTO = new TareaDTO();
        tareaDTO.setTitulo("Tarea de prueba");
        tareaDTO.setDescripcion("Descripción de prueba");
        tareaDTO.setEmpleadoId(1L);
        tareaDTO.setPrioridad("ALTA");
        tareaDTO.setFechaVencimiento(LocalDate.now().plusDays(7));

        tarea = new Tarea();
        tarea.setId(1L);
        tarea.setTitulo("Tarea de prueba");
        tarea.setDescripcion("Descripción de prueba");
        tarea.setEmpleado(empleado);
        tarea.setJefe(jefe);
        tarea.setPrioridad("ALTA");
    }

    @Test
    void saveTareaForJefe_DebeCrearTareaCorrectamente() {

        // Configurar comportamiento de los mocks
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(jefeRepository.findByNombreUsuario("jefe1")).thenReturn(Optional.of(jefe));
        when(tareaMapper.toEntity(tareaDTO)).thenReturn(tarea);
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);
        when(tareaMapper.toDto(tarea)).thenReturn(tareaDTO);

        // Ejecutar el metodo a probar
        TareaDTO resultado = tareaService.saveTareaForJefe(tareaDTO, "jefe1");

        // Verificar resultados
        assertNotNull(resultado);
        assertEquals("Tarea de prueba", resultado.getTitulo());
        assertEquals(1L, resultado.getEmpleadoId());

        // Verificar que los métodos mock fueron llamados
        verify(empleadoRepository).findById(1L);
        verify(jefeRepository).findByNombreUsuario("jefe1");
        verify(tareaRepository).save(any(Tarea.class));
        verify(tareaMapper).toDto(any(Tarea.class));
    }

    @Test
    void saveTareaForJefe_DebeArrojarExcepcion_CuandoEmpleadoNoExiste() {

        // Configurar comportamiento de los mocks
        when(empleadoRepository.findById(1L)).thenReturn(Optional.empty());

        // Verificar que se arroja la excepción esperada
        Exception exception = assertThrows(RuntimeException.class, () -> {
            tareaService.saveTareaForJefe(tareaDTO, "jefe1");
        });

        // Verificar el mensaje de la excepción
        assertTrue(exception.getMessage().contains("Empleado no encontrado"));

        // Verificar que ciertos métodos no fueron llamados
        verify(tareaRepository, never()).save(any(Tarea.class));
    }
}
