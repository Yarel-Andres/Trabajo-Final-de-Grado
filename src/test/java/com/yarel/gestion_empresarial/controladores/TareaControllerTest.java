package com.yarel.gestion_empresarial.controladores;

import com.yarel.gestion_empresarial.controller.TareaController;
import com.yarel.gestion_empresarial.dto.tarea.TareaDTO;
import com.yarel.gestion_empresarial.servicios.TareaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TareaController.class)
public class TareaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TareaService tareaService;

    private TareaDTO tareaDTO;
    private List<TareaDTO> tareas;

    @BeforeEach
    void setUp() {
        tareaDTO = new TareaDTO();
        tareaDTO.setId(1L);
        tareaDTO.setTitulo("Tarea de prueba");
        tareaDTO.setDescripcion("Descripción de prueba");
        tareaDTO.setEmpleadoId(1L);
        tareaDTO.setJefeId(2L);
        tareaDTO.setPrioridad("ALTA");
        tareaDTO.setFechaVencimiento(LocalDate.now().plusDays(7));

        tareas = Arrays.asList(tareaDTO);
    }

    @Test
    @WithMockUser(username = "jefe1", roles = {"JEFE"})
    void getAllTareas_DebeRetornarListaDeTareas() throws Exception {
        when(tareaService.findAll()).thenReturn(tareas);

        mockMvc.perform(get("/api/tareas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Tarea de prueba"))
                .andExpect(jsonPath("$[0].empleadoId").value(1));
    }

    @Test
    @WithMockUser(username = "jefe1", roles = {"JEFE"})
    void crearTareaAsignada_DebeCrearTareaCorrectamente() throws Exception {
        when(tareaService.saveTareaForJefe(any(TareaDTO.class), eq("jefe1"))).thenReturn(tareaDTO);

        mockMvc.perform(post("/api/tareas/jefe")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Tarea de prueba\",\"descripcion\":\"Descripción de prueba\",\"empleadoId\":1,\"prioridad\":\"ALTA\",\"fechaVencimiento\":\"" + LocalDate.now().plusDays(7) + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Tarea de prueba"))
                .andExpect(jsonPath("$.empleadoId").value(1));
    }
}