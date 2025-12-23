package com.lifeboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeboard.dto.tarefa.TarefaRequestDTO;
import com.lifeboard.dto.tarefa.TarefaResponseDTO;
import com.lifeboard.model.enums.Prioridade;
import com.lifeboard.model.enums.StatusTarefa;
import com.lifeboard.security.SecurityFilter;
import com.lifeboard.service.TarefaService;
import com.lifeboard.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TarefaController.class)
@AutoConfigureMockMvc(addFilters = false)
class TarefaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TarefaService tarefaService;

    @Test
    @DisplayName("Deve retornar página de tarefas")
    void listarTodos() throws Exception {
        var tarefa = new TarefaResponseDTO(
                1L,
                "Estudar Spring",
                "Praticar testes",
                Prioridade.ALTA,
                StatusTarefa.EM_ANDAMENTO,
                LocalDate.now().plusDays(5),
                10L
        );

        Page<TarefaResponseDTO> page =
                new PageImpl<>(List.of(tarefa), PageRequest.of(0, 10), 1);

        Mockito.when(tarefaService.listarTodos(any())).thenReturn(page);

        mockMvc.perform(get("/api/tarefas?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id_tarefa").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Estudar Spring"))
                .andExpect(jsonPath("$.content[0].descricao").value("Praticar testes"))
                .andExpect(jsonPath("$.content[0].prioridade").value("ALTA"))
                .andExpect(jsonPath("$.content[0].status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.content[0].id_usuario").value(10));
    }

    @Test
    @DisplayName("Deve buscar tarefa por ID")
    void buscarPorId() throws Exception {
        var tarefa = new TarefaResponseDTO(
                1L,
                "Estudar Spring",
                "Praticar testes",
                Prioridade.MEDIA,
                StatusTarefa.CONCLUIDA,
                LocalDate.now(),
                5L
        );

        Mockito.when(tarefaService.buscarDTOPorId(1L)).thenReturn(tarefa);

        mockMvc.perform(get("/api/tarefas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_tarefa").value(1))
                .andExpect(jsonPath("$.titulo").value("Estudar Spring"))
                .andExpect(jsonPath("$.prioridade").value("MEDIA"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.id_usuario").value(5));
    }

    @Test
    @DisplayName("Deve criar uma nova tarefa")
    void salvar() throws Exception {

        var request = new TarefaRequestDTO(
                "Nova tarefa",
                "Fazer algo importante",
                Prioridade.BAIXA,
                StatusTarefa.EM_ANDAMENTO,
                LocalDate.now().plusDays(3),
                7L
        );

        var response = new TarefaResponseDTO(
                1L,
                request.getTitulo(),
                request.getDescricao(),
                request.getPrioridade(),
                request.getStatus(),
                request.getDataLimite(),
                request.getUsuarioId()
        );

        Mockito.when(tarefaService.salvar(any())).thenReturn(response);

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id_tarefa", is(1)))
                .andExpect(jsonPath("$.titulo").value("Nova tarefa"));
    }

    @Test
    @DisplayName("Deve atualizar uma tarefa")
    void atualizar() throws Exception {
        var request = new TarefaRequestDTO(
                "Atualizada",
                "Descrição atualizada",
                Prioridade.ALTA,
                StatusTarefa.EM_ANDAMENTO,
                LocalDate.now().plusDays(2),
                9L
        );

        var response = new TarefaResponseDTO(
                1L,
                "Atualizada",
                "Descrição atualizada",
                Prioridade.ALTA,
                StatusTarefa.EM_ANDAMENTO,
                request.getDataLimite(),
                9L
        );

        Mockito.when(tarefaService.atualizar(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/tarefas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_tarefa").value(1))
                .andExpect(jsonPath("$.titulo").value("Atualizada"))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    @DisplayName("Deve deletar tarefa")
    void deletar() throws Exception {
        mockMvc.perform(delete("/api/tarefas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar criar tarefa inválida")
    void salvarInvalido() throws Exception {
        var request = new TarefaRequestDTO(); // tudo null

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}