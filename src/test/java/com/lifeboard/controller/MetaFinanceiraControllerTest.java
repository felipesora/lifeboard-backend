package com.lifeboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeboard.dto.meta.MetaFinanceiraResponseDTO;
import com.lifeboard.dto.meta.MetaFinanceiraSaveRequestDTO;
import com.lifeboard.dto.meta.MetaFinanceiraUpdateRequestDTO;
import com.lifeboard.dto.meta.SaldoRequest;
import com.lifeboard.model.enums.StatusMeta;
import com.lifeboard.security.SecurityFilter;
import com.lifeboard.service.MetaFinanceiraService;
import com.lifeboard.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetaFinanceiraController.class)
@AutoConfigureMockMvc(addFilters = false)
class MetaFinanceiraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetaFinanceiraService metaFinanceiraService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar página de metas financeiras")
    void listarTodos() throws Exception {
        var meta = new MetaFinanceiraResponseDTO(
                1L,
                "Viagem",
                new BigDecimal("5000.00"),
                new BigDecimal("1000.00"),
                LocalDate.of(2025, 12, 31),
                StatusMeta.EM_ANDAMENTO,
                3L
        );

        var page = new PageImpl<>(List.of(meta), PageRequest.of(0, 10), 1);

        Mockito.when(metaFinanceiraService.listarTodos(any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/metas?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id_meta").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Viagem"));
    }

    @Test
    @DisplayName("Deve buscar meta por ID")
    void buscarPorId() throws Exception {
        var meta = new MetaFinanceiraResponseDTO(
                1L,
                "Viagem",
                new BigDecimal("5000.00"),
                new BigDecimal("1000.00"),
                LocalDate.of(2025, 12, 31),
                StatusMeta.EM_ANDAMENTO,
                3L
        );

        Mockito.when(metaFinanceiraService.buscarDTOPorId(1L))
                .thenReturn(meta);

        mockMvc.perform(get("/api/metas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_meta").value(1))
                .andExpect(jsonPath("$.nome").value("Viagem"));
    }

    @Test
    @DisplayName("Deve salvar meta financeira")
    void salvar() throws Exception {
        var request = new MetaFinanceiraSaveRequestDTO(
                "Viagem",
                new BigDecimal("5000.00"),
                new BigDecimal("1000.00"),
                LocalDate.of(2025, 12, 31),
                10L
        );

        var response = new MetaFinanceiraResponseDTO(
                1L,
                "Viagem",
                new BigDecimal("5000.00"),
                new BigDecimal("1000.00"),
                LocalDate.of(2025, 12, 31),
                StatusMeta.EM_ANDAMENTO,
                3L
        );

        Mockito.when(metaFinanceiraService.salvar(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/metas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id_meta").value(1));
    }

    @Test
    @DisplayName("Deve atualizar meta financeira")
    void atualizar() throws Exception {
        var request = new MetaFinanceiraUpdateRequestDTO(
                "Viagem",
                new BigDecimal("5000.00"),
                LocalDate.of(2025, 12, 31),
                10L
        );

        var response = new MetaFinanceiraResponseDTO(
                1L,
                "Meta Atualizada",
                new BigDecimal("8000.00"),
                new BigDecimal("1000.00"),
                LocalDate.of(2025, 12, 31),
                StatusMeta.EM_ANDAMENTO,
                3L
        );


        Mockito.when(metaFinanceiraService.atualizar(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/metas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Meta Atualizada"));
    }

    @Test
    @DisplayName("Deve deletar meta financeira")
    void deletar() throws Exception {
        Mockito.doNothing().when(metaFinanceiraService).deletar(1L);

        mockMvc.perform(delete("/api/metas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve adicionar saldo na meta")
    void adicionarSaldo() throws Exception {
        var request = new SaldoRequest(new BigDecimal("500.00"));

        Mockito.doNothing().when(metaFinanceiraService)
                .adicionarSaldo(eq(1L), eq(new BigDecimal("500.00")));

        mockMvc.perform(post("/api/metas/1/adicionar-saldo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retirar saldo da meta")
    void retirarSaldo() throws Exception {
        var request = new SaldoRequest(new BigDecimal("300.00"));

        Mockito.doNothing().when(metaFinanceiraService)
                .retirarSaldo(eq(1L), eq(new BigDecimal("300.00")));

        mockMvc.perform(post("/api/metas/1/retirar-saldo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}